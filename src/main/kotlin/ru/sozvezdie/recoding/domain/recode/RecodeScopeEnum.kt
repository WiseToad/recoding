package ru.sozvezdie.recoding.domain.recode

import com.fasterxml.jackson.core.type.TypeReference
import ru.sozvezdie.recoding.domain.star.Distribution
import ru.sozvezdie.recoding.domain.star.Remnant

enum class RecodeScopeEnum(
    initialHandlers: List<RecodeHandlerEnum> = listOf(),
    defaultHandlers: List<RecodeHandlerEnum> = listOf(),
    val pipelines: List<RecodePipeline> = listOf()
) {
    STAR(
        initialHandlers = listOf(RecodeHandlerEnum.STAR_SUBJECT),
        defaultHandlers = listOf(RecodeHandlerEnum.STAR_NOMENCLATURE_PHARM_ETALON),
        pipelines = listOf(
            RecodePipeline("distribution", "distribution-recoded", "distribution-recode-failed") { objectMapper ->
                org.springframework.kafka.support.serializer.JsonSerde(object: TypeReference<Distribution>() {}, objectMapper)
            },
            RecodePipeline("remnant", "remnant-recoded", "remnant-recode-failed") { objectMapper ->
                org.springframework.kafka.support.serializer.JsonSerde(object: TypeReference<Remnant>() {}, objectMapper)
            },
        )
    ),
    PULSAR,
    PRICING;

    val initialSchema: RecodeSchemaContent = initialHandlers.groupBy(RecodeHandlerEnum::attribute)
    val defaultSchema: RecodeSchemaContent = defaultHandlers.groupBy(RecodeHandlerEnum::attribute)
}
