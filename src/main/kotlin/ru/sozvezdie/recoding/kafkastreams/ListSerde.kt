package ru.sozvezdie.recoding.kafkastreams

import org.apache.kafka.common.serialization.ListDeserializer
import org.apache.kafka.common.serialization.ListSerializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.WrapperSerde

// workaround for Kotlin limitation of not distinguishing the ListSerde class c-tor from utility method with the same name
class ListSerde<E>(listClass: Class<out List<*>>, elementSerde: Serde<E>):
    WrapperSerde<List<E>>(
        ListSerializer(elementSerde.serializer()),
        @Suppress("UNCHECKED_CAST")
        ListDeserializer(listClass as Class<out List<E>>, elementSerde.deserializer())
    )
{
    constructor(elementSerde: Serde<E>): this(ArrayList::class.java, elementSerde)
}
