package ru.sozvezdie.recoding.domain.recode

import kotlin.properties.Delegates

class RecodeSchemaHandler: RecodeSchemaTable {
    override var recodeSchemaId: Long by Delegates.notNull()
    lateinit var handler: RecodeHandlerEnum
    var priority: Int by Delegates.notNull()
    lateinit var scope: RecodeScopeEnum
}
