package ru.sozvezdie.recoding.handler

import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.domain.recode.RecodeHandlerEnum

abstract class RecodeHandler<T: Recodeable> {

    lateinit var handler: RecodeHandlerEnum

    fun recode(element: T): List<T>? {
        @Suppress("UNCHECKED_CAST") // cast is potentially unsafe, will break if schema handlers aren't consistent with element type
        val accessor = handler.accessor as RecodeHandlerEnum.Accessor<T>

        val sourceValue = accessor.getter(element) ?: return null
        val targetValues = mapValues(sourceValue)?.takeIf(List<*>::isNotEmpty) ?: return null
        return targetValues.mapIndexed { i, targetValue ->
            @Suppress("UNCHECKED_CAST") // cast is safe, if element class respects standard conventions of clone method implementation
            (if (i == 0) element else element.clone() as T).also {
                accessor.setter(it, targetValue)
                it.successfulHandlers.add(handler)
            }
        }
    }

    protected abstract fun mapValues(sourceValue: Any): List<Long>?
}
