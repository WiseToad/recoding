package ru.sozvezdie.recoding.service

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Service
import ru.sozvezdie.recoding.common.composeKey
import ru.sozvezdie.recoding.config.Constant
import ru.sozvezdie.recoding.domain.recode.*
import ru.sozvezdie.recoding.mapper.RecodeSchemaHandlerMapper
import java.util.concurrent.ConcurrentHashMap

@Service
class RecodeSchemaService(
    private val recodeSchemaHandlerMapper: RecodeSchemaHandlerMapper
): ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext

    private val schemaLinkStore: ReadOnlyKeyValueStore<String, Long> by lazy {
        val kafkaStreams = applicationContext.getBean(KafkaStreams::class.java)
        kafkaStreams.store(StoreQueryParameters.fromNameAndType(Constant.StoreName.RECODE_SCHEMA_LINK, QueryableStoreTypes.keyValueStore()))
    }

    private var schemaMap: ConcurrentHashMap<Long, RecodeSchemaContent> = ConcurrentHashMap(selectAllSchemas())

    // done so to avoid bean circular dependency
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    fun resolveSchema(schemaSubjectId: Long, scope: RecodeScopeEnum): RecodeSchemaContent {
        val linkKey = composeKey(schemaSubjectId, scope.ordinal)
        val schemaId = schemaLinkStore.get(linkKey) ?: return scope.defaultSchema
        return schemaMap.computeIfAbsent(schemaId) {
            selectSchema(schemaId) ?: throw RuntimeException("No active schema found or schema is empty")
        }
    }

    fun refreshSchema(schemaId: Long) {
        val schema = selectSchemaHandlers(schemaId)?.let(::buildSchema)
        if (schema != null) schemaMap[schemaId] = schema else schemaMap.remove(schemaId)
    }

    private fun selectSchema(schemaId: Long): RecodeSchemaContent? {
        val schemaHandlers = selectSchemaHandlers(schemaId) ?: return null
        return schemaHandlers.first().scope.defaultSchema + buildSchema(schemaHandlers)
    }

    private fun selectSchemaHandlers(schemaId: Long) = recodeSchemaHandlerMapper.select(schemaId, active = true).takeIf(List<*>::isNotEmpty)

    private fun selectAllSchemas(): Map<Long, RecodeSchemaContent> {
        return recodeSchemaHandlerMapper.select(active = true)
            .groupBy(RecodeSchemaHandler::recodeSchemaId)
            .mapValues { (_, schemaHandlers) -> schemaHandlers.first().scope.defaultSchema + buildSchema(schemaHandlers) }
    }

    private fun buildSchema(schemaHandlers: List<RecodeSchemaHandler>): RecodeSchemaContent {
        return schemaHandlers
            .groupBy { it.handler.attribute }
            .mapValues { (_, schemaHandlers) ->
                schemaHandlers
                    .sortedBy(RecodeSchemaHandler::priority)
                    .map(RecodeSchemaHandler::handler)
            }
    }
}
