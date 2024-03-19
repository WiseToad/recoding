package ru.sozvezdie.recoding.domain.recode

import kotlin.properties.Delegates

class RecodeSchema: RecodeSchemaTable {
    var id: Long by Delegates.notNull()

    override var recodeSchemaId: Long
        get() = id
        set(value) { id = value }
}
