package ru.sozvezdie.recoding.handler

import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.exception.InvalidSourceTypeException

@Component
@Scope("prototype")
class RecodeIdentityHandler<T: Recodeable>: RecodeHandler<T>() {

    override fun mapValues(sourceValue: Any): List<Long>? {
        return when(sourceValue) {
            is String -> listOf(sourceValue.toLong())
            is Number -> listOf(sourceValue.toLong())
            else -> throw InvalidSourceTypeException(handler.attribute)
        }
    }
}
