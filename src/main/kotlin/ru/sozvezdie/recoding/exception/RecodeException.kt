package ru.sozvezdie.recoding.exception

import ru.sozvezdie.recoding.domain.recode.RecodeAttributeEnum

open class RecodeException(
    message: String? = null,
    cause: Throwable? = null
): RuntimeException(message, cause) {

    constructor(cause: Throwable?): this(cause?.toString(), cause)

    open val attribute: RecodeAttributeEnum? = null
}
