package ru.sozvezdie.recoding.domain.recode

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import kotlin.properties.Delegates

class RecodeMapping {
    var id: Long by Delegates.notNull()

    @set:JsonSetter(nulls = Nulls.SKIP)
    lateinit var type: RecodeMappingTypeEnum

    @set:JsonSetter(nulls = Nulls.SKIP)
    var active: Boolean by Delegates.notNull()
}
