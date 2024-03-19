package ru.sozvezdie.recoding.handler

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StoreQueryParameters
import org.apache.kafka.streams.state.QueryableStoreTypes
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import ru.sozvezdie.recoding.common.composeKey
import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.exception.InvalidSourceTypeException

@Component
@Scope("prototype")
class RecodeStandardHandler<T: Recodeable>(
    kafkaStreams: KafkaStreams
): RecodeHandler<T>() {

    var activeMappingId: Long? = null

    private val store: ReadOnlyKeyValueStore<String, List<Long>> by lazy {
        val storeName = handler.mappingType?.storeName ?: throw IllegalArgumentException("Undefined store name for standard handler")
        kafkaStreams.store(StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore()))
    }

    override fun mapValues(sourceValue: Any): List<Long>? {
        return if (activeMappingId == null) null
        else when (sourceValue) {
            is String, is Number -> store.get(composeKey(sourceValue, "$activeMappingId"))
            else -> throw InvalidSourceTypeException(handler.attribute)
        }
    }
}
