package ru.sozvezdie.recoding.domain.recode

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import kotlin.properties.Delegates

class RecodeSchemaLink {
    @set:JsonProperty(value = "scope")
    lateinit var scope: RecodeScopeEnum

    @set:JsonProperty(value = "schema_subject_id")
    var schemaSubjectId: Long by Delegates.notNull()

    @set:JsonProperty(value = "recode_schema_id")
    @set:JsonSetter(nulls = Nulls.SKIP)
    var recodeSchemaId: Long by Delegates.notNull()
}
