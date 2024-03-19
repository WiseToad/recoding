package ru.sozvezdie.recoding.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.sozvezdie.recoding.domain.recode.RecodeAttributeEnum
import ru.sozvezdie.recoding.domain.recode.RecodeHandlerEnum

abstract class Recodeable: Cloneable {

    @get:JsonIgnore
    abstract val schemaSubjectId: Long

    var errorType: String? = null
    var failedAttribute: RecodeAttributeEnum? = null

    var successfulHandlers = mutableListOf<RecodeHandlerEnum>()

    public override fun clone(): Any = super.clone()
}
