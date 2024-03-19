package ru.sozvezdie.recoding.service

import org.springframework.stereotype.Service
import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.domain.recode.RecodeHandlerEnum
import ru.sozvezdie.recoding.domain.recode.RecodeSchemaContent
import ru.sozvezdie.recoding.domain.recode.RecodeScopeEnum
import ru.sozvezdie.recoding.exception.NoMatchFoundException
import ru.sozvezdie.recoding.handler.RecodeHandler

@Service
class RecodeService(
    private val recodeSchemaService: RecodeSchemaService,
    private val recodeHandlerService: RecodeHandlerService
) {
    fun <T: Recodeable> recode(element: T, scope: RecodeScopeEnum): List<T> {
        return recodeWithSchema(element, scope.initialSchema).flatMap {
            recodeWithSchema(it, recodeSchemaService.resolveSchema(it.schemaSubjectId, scope))
        }
    }

    private fun <T: Recodeable> recodeWithSchema(element: T, schema: RecodeSchemaContent): List<T> {
        var result = listOf(element)
        schema.forEach { (attribute, handlers) ->
            result = result.flatMap {
                handlers.firstNotNullOfOrNull { handler -> recodeWithHandler(it, handler) } ?: throw NoMatchFoundException(attribute)
            }
        }
        return result
    }

    private fun <T: Recodeable> recodeWithHandler(element: T, handler: RecodeHandlerEnum): List<T>? {
        @Suppress("UNCHECKED_CAST") // cast is potentially unsafe, will break if schema handlers aren't consistent with element type
        val handlerInstance = recodeHandlerService.resolveHandlerInstance(handler) as RecodeHandler<T>
        return handlerInstance.recode(element)
    }
}
