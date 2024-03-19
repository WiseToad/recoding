package ru.sozvezdie.recoding.domain.recode

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.support.serializer.JsonSerde
import ru.sozvezdie.recoding.domain.Recodeable

class RecodePipeline (
    val inputTopic: String,
    val outputTopic: String,
    val errorTopic: String,
    val serdeSupplier: (objectMapper: ObjectMapper) -> JsonSerde<out Recodeable>
)
