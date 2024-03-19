package ru.sozvezdie.recoding.domain.recode

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.properties.Delegates

class RecodeMappingValue {
    @set:JsonProperty(value = "recode_mapping_id")
    var recodeMappingId: Long by Delegates.notNull()

    @set:JsonProperty(value = "source_value")
    lateinit var sourceValue: String

    @set:JsonProperty(value = "target_values")
    var targetValues: List<Long>? = null

    var mappingType: RecodeMappingTypeEnum? = null
}
