package ru.sozvezdie.recoding.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.kafka.KafkaStreamsMetrics
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.metrics.Sensor
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.errors.DeserializationExceptionHandler
import org.apache.kafka.streams.kstream.*
import org.apache.kafka.streams.state.KeyValueStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.cloud.bus.BusConstants
import org.springframework.cloud.stream.binder.kafka.support.ConsumerConfigCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.kafka.support.serializer.JsonSerde
import org.springframework.util.StringUtils
import ru.sozvezdie.recoding.common.composeKey
import ru.sozvezdie.recoding.domain.*
import ru.sozvezdie.recoding.domain.recode.*
import ru.sozvezdie.recoding.kafkastreams.ListSerde
import ru.sozvezdie.recoding.kafkastreams.errorhandler.LogAndContinueErrorHandler
import ru.sozvezdie.recoding.kafkastreams.errorhandler.LogAndFailErrorHandler
import ru.sozvezdie.recoding.meter.Meter
import ru.sozvezdie.recoding.exception.RecodeException
import ru.sozvezdie.recoding.service.RecodeHandlerService
import ru.sozvezdie.recoding.service.RecodeSchemaService
import ru.sozvezdie.recoding.service.RecodeService

//FIXME: Revise overall code to manage configurable kafka topic prefixes (and postfixes), taking in account that there recode scopes are present

@Configuration
class KafkaConfiguration(
    private val env: Environment,
    private val applicationProperties: ApplicationProperties,
    private val kafkaProperties: KafkaProperties,
    private val objectMapper: ObjectMapper,
    private val recodeService: RecodeService,
    private val recodeHandlerService: RecodeHandlerService,
    private val recodeSchemaService: RecodeSchemaService
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(KafkaConfiguration::class.java)
    }

    private val byteArraySerde = Serdes.ByteArray()
    private val stringSerde = Serdes.String()
    private val longSerde = Serdes.Long()
    private val longListSerde = ListSerde(longSerde)

    private val recodeMappingCdcDataSerde = JsonSerde(object: TypeReference<CdcData<RecodeMapping>>() {}, objectMapper).ignoreTypeHeaders().noTypeInfo()
    private val recodeMappingValueCdcDataSerde = JsonSerde(object: TypeReference<CdcData<RecodeMappingValue>>() {}, objectMapper).ignoreTypeHeaders().noTypeInfo()
    private val recodeSchemaTableCdcDataSerde = JsonSerde(object: TypeReference<CdcData<RecodeSchemaTable>>() {}, objectMapper).ignoreTypeHeaders().noTypeInfo()
    private val recodeSchemaLinkCdcDataSerde = JsonSerde(object: TypeReference<CdcData<RecodeSchemaLink>>() {}, objectMapper).ignoreTypeHeaders().noTypeInfo()

    private val allowedCdcOps = setOf("c", "u", "d")

    @Bean
    fun consumerConfigCustomizer(): ConsumerConfigCustomizer {
        return ConsumerConfigCustomizer { consumerProperties: MutableMap<String?, Any?>, bindingName: String, _: String? ->
            if (BusConstants.INPUT == bindingName) { // the SpringCloudBus consumer
                consumerProperties[ConsumerConfig.GROUP_ID_CONFIG] = getConsumerGroup(Constant.ConsumerGroup.BUS_POSTFIX, true)
            }
        }
    }

    @Bean
    fun kafkaStreamsMetricsBinder(meterRegistry: MeterRegistry, kafkaStreams: KafkaStreams): KafkaStreamsMetrics {
        createCustomKafkaStreamsMetrics(meterRegistry, kafkaStreams)
        val kafkaStreamsMetrics = KafkaStreamsMetrics(kafkaStreams, listOf(Tag.of("spring.id", "null")))
        kafkaStreamsMetrics.bindTo(meterRegistry)
        return kafkaStreamsMetrics
    }

    private fun createCustomKafkaStreamsMetrics(meterRegistry: MeterRegistry, kafkaStreams: KafkaStreams) {
        Meter.Gauge.KAFKA_STREAM_TASK_COUNT.register(meterRegistry, arrayOf("active")) {
            kafkaStreams.metadataForLocalThreads().sumOf { it.activeTasks().size }
        }
        Meter.Gauge.KAFKA_STREAM_TASK_COUNT.register(meterRegistry, arrayOf("standby")) {
            kafkaStreams.metadataForLocalThreads().sumOf { it.standbyTasks().size }
        }
    }

    @Bean
    fun kafkaStreams(): KafkaStreams {
        val properties = mutableMapOf(
            StreamsConfig.APPLICATION_ID_CONFIG to getConsumerGroup(),
            StreamsConfig.PROCESSING_GUARANTEE_CONFIG to StreamsConfig.EXACTLY_ONCE_V2,
            StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG to getDeserializationHandler(),
            ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG to Sensor.RecordingLevel.DEBUG.name
        )
        return createStreams(properties, buildStreamsTopology()).apply { start() }
    }

    private fun buildStreamsTopology(): Topology {
        val builder = StreamsBuilder()

        createMappingRefreshStream(builder)
        createMappingValueTables(builder)

        createSchemaRefreshStream(builder)
        createSchemaLinkTable(builder)

        //TODO: Scale recode streams created below irrespective of mapping and schema sub-topologies
        RecodeScopeEnum.entries.forEach { scope ->
            scope.pipelines.forEach { pipeline ->
                createRecodeStream(builder, pipeline, scope)
            }
        }

        return builder.build()
    }

    private fun createMappingRefreshStream(builder: StreamsBuilder) {
        builder.stream(Constant.Topic.RECODE_MAPPING_CDC_DATA, Consumed.with(byteArraySerde, recodeMappingCdcDataSerde))
            .filter { _, cdcData -> cdcData?.op in allowedCdcOps }
            .foreach { _, cdcData -> recodeHandlerService.refreshMapping(cdcData.data.id) }
    }

    private fun createMappingValueTables(builder: StreamsBuilder) {
        val branchedStream = builder.stream(Constant.Topic.RECODE_MAPPING_VALUE_CDC_DATA, Consumed.with(byteArraySerde, recodeMappingValueCdcDataSerde))
            .filter { _, cdcData -> cdcData?.op in allowedCdcOps }
            .mapValues { _, cdcData -> cdcData.apply { data.mappingType = recodeHandlerService.resolveMappingType(data.recodeMappingId) } }
            .split()

        RecodeMappingTypeEnum.entries.forEach { mappingType ->
            branchMappingValueToTable(builder, branchedStream, mappingType)
        }
    }

    private fun branchMappingValueToTable(
        builder: StreamsBuilder, branchedStream: BranchedKStream<ByteArray, CdcData<RecodeMappingValue>>, mappingType: RecodeMappingTypeEnum
    ) {
        branchedStream.branch(
            { _, cdcData -> cdcData.data.mappingType == mappingType },
            Branched.withConsumer { stream -> streamMappingValueToChangelog(stream, mappingType) }
        )
        createGlobalTable(builder, mappingType.storeName, stringSerde, longListSerde)
    }

    private fun streamMappingValueToChangelog(stream: KStream<ByteArray, CdcData<RecodeMappingValue>>, mappingType: RecodeMappingTypeEnum) {
        stream
            .map { _, cdcData -> KeyValue.pair(composeKey(cdcData.data.sourceValue, cdcData.data.recodeMappingId), cdcData.after?.targetValues) }
            .to(mappingType.storeName + Constant.Topic.CHANGELOG_POSTFIX, Produced.with(stringSerde, longListSerde))
    }

    private fun <K, V> createGlobalTable(builder: StreamsBuilder, storeName: String, keySerde: Serde<K>, valueSerde: Serde<V>): GlobalKTable<K, V> {
        val changelogTopic = storeName + Constant.Topic.CHANGELOG_POSTFIX
        val materializeConfig = Materialized
            .`as`<K, V, KeyValueStore<Bytes, ByteArray>>(storeName)
            .withKeySerde(keySerde)
            .withValueSerde(valueSerde)

        return builder.globalTable(changelogTopic, materializeConfig)
    }

    private fun createSchemaRefreshStream(builder: StreamsBuilder) {
        val topics = listOf(Constant.Topic.RECODE_SCHEMA_CDC_DATA, Constant.Topic.RECODE_SCHEMA_HANDLER_CDC_DATA)
        builder.stream(topics, Consumed.with(byteArraySerde, recodeSchemaTableCdcDataSerde))
            .filter { _, cdcData -> cdcData?.op in allowedCdcOps }
            .foreach { _, cdcData -> recodeSchemaService.refreshSchema(cdcData.data.recodeSchemaId) }
    }

    private fun createSchemaLinkTable(builder: StreamsBuilder) {
        builder.stream(Constant.Topic.RECODE_SCHEMA_LINK_CDC_DATA, Consumed.with(byteArraySerde, recodeSchemaLinkCdcDataSerde))
            .filter { _, cdcData -> cdcData?.op in allowedCdcOps }
            .map { _, cdcData -> KeyValue.pair(composeKey(cdcData.data.schemaSubjectId, cdcData.data.scope.ordinal), cdcData.after?.recodeSchemaId) }
            .to(Constant.StoreName.RECODE_SCHEMA_LINK + Constant.Topic.CHANGELOG_POSTFIX, Produced.with(stringSerde, longSerde))

        createGlobalTable(builder, Constant.StoreName.RECODE_SCHEMA_LINK, stringSerde, longSerde)
    }

    private fun createRecodeStream(builder: StreamsBuilder, pipeline: RecodePipeline, scope: RecodeScopeEnum) {
        @Suppress("UNCHECKED_CAST") // cast is safe, since we're casting to supertype
        val elementSerde = pipeline.serdeSupplier(objectMapper).ignoreTypeHeaders().noTypeInfo() as Serde<Recodeable>

        val inputTopic = getTopic(pipeline.inputTopic)
        val inputConsumeConfig = Consumed.with(stringSerde, elementSerde)

        val outputTopic = getTopic(pipeline.outputTopic)
        val outputProduceConfig: Produced<String, Recodeable> = Produced.with(stringSerde, elementSerde)

        val errorTopic = getTopic(pipeline.errorTopic)
        val errorProduceConfig = Produced.with(stringSerde, elementSerde)

        builder.stream(inputTopic, inputConsumeConfig)
            .flatMapValues { element -> processElement(element, scope) }
            .split()
            .branch({ _, element: Recodeable -> element.errorType == null }, Branched.withConsumer { stream -> stream.to(outputTopic, outputProduceConfig) })
            .defaultBranch(Branched.withConsumer { stream -> stream.to(errorTopic, errorProduceConfig) })
    }

    private fun <T: Recodeable> processElement(element: T, scope: RecodeScopeEnum): List<T> {
        return try {
            Meter.Count.INCOMING_ELEMENT_COUNT.increment(element)
            recodeService.recode(element, scope)
        } catch (e: RecodeException) {
            Meter.Count.RECODE_ERROR_COUNT.increment(element)
            listOf(element.apply {
                errorType = e.javaClass.simpleName
                failedAttribute = e.attribute
            })
        } catch (e: Exception) {
            log.error("Error processing element", e)
            Meter.Count.OTHER_ERROR_COUNT.increment(element)
            listOf(element.apply {
                errorType = e.javaClass.simpleName
            })
        }
    }

    fun getConsumerGroup(postfix: String? = null, isIndividual: Boolean? = null): String {
        var consumerGroup: String
        if (isIndividual == true || (isIndividual == null && applicationProperties.isIndividualConsumerGroup)) {
            consumerGroup = ApplicationProperties.hostName.lowercase()
            // add app name prefix to distinguish different apps running on single developer machine
            var appNamePrefix = env.getProperty(Constant.APPLICATION_NAME_PROPERTY)
            if (appNamePrefix != null) {
                appNamePrefix += Constant.ConsumerGroup.SEPARATOR_CHAR
                if (!StringUtils.startsWithIgnoreCase(consumerGroup, appNamePrefix)) {
                    consumerGroup = appNamePrefix + consumerGroup
                }
            }
        } else {
            consumerGroup = env.getRequiredProperty(Constant.APPLICATION_NAME_PROPERTY)
        }
        if (StringUtils.hasText(postfix)) {
            consumerGroup += Constant.ConsumerGroup.SEPARATOR_CHAR + postfix
        }
        return applicationProperties.kafkaPrefix + consumerGroup + applicationProperties.kafkaPostfix
    }

    fun getTopic(topic: String): String = applicationProperties.kafkaPrefix + topic + applicationProperties.kafkaPostfix

    fun createStreams(customProperties: Map<String, Any>, topology: Topology): KafkaStreams {
        val properties = customProperties + kafkaProperties.buildStreamsProperties()
        return KafkaStreams(topology, StreamsConfig(properties))
    }

    fun <K, V> createProducer(customProperties: Map<String, Any>, keySerializer: Serializer<K>, valueSerializer: Serializer<V>): KafkaProducer<K, V> {
        val properties = customProperties + kafkaProperties.buildProducerProperties()
        return KafkaProducer(properties, keySerializer, valueSerializer)
    }

    private fun getDeserializationHandler(): Class<out DeserializationExceptionHandler> {
        return if (applicationProperties.skipKafkaDeserializationError) LogAndContinueErrorHandler::class.java else LogAndFailErrorHandler::class.java
    }
}
