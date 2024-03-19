package ru.sozvezdie.recoding.service

import org.springframework.beans.factory.BeanFactory
import org.springframework.stereotype.Service
import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.domain.recode.RecodeHandlerEnum
import ru.sozvezdie.recoding.domain.recode.RecodeMappingTypeEnum
import ru.sozvezdie.recoding.handler.RecodeHandler
import ru.sozvezdie.recoding.handler.RecodeStandardHandler
import ru.sozvezdie.recoding.mapper.RecodeMappingMapper
import java.util.concurrent.ConcurrentHashMap

@Service
class RecodeHandlerService(
    private val beanFactory: BeanFactory,
    private val recodeMappingMapper: RecodeMappingMapper
) {
    private val mappingTypeMap = ConcurrentHashMap(
        recodeMappingMapper.select().associate { it.id to it.type }
    )

    private val handlerInstanceMap = ConcurrentHashMap<RecodeHandlerEnum, RecodeHandler<out Recodeable>>()

    fun resolveMappingType(mappingId: Long): RecodeMappingTypeEnum? {
        return mappingTypeMap.computeIfAbsent(mappingId) {
            recodeMappingMapper.select(mappingId).firstOrNull()?.type
        }
    }

    fun resolveHandlerInstance(handler: RecodeHandlerEnum): RecodeHandler<*> {
        return handlerInstanceMap.computeIfAbsent(handler) {
            beanFactory.getBean(handler.clazz).also { instance ->
                instance.handler = handler
                if (instance is RecodeStandardHandler && handler.mappingType != null) {
                    instance.activeMappingId = recodeMappingMapper.select(type = handler.mappingType, active = true).firstOrNull()?.id
                }
            }
        }
    }

    fun refreshMapping(mappingId: Long) {
        mappingTypeMap[mappingId]?.let { mappingType ->
            val activeMappingId = recodeMappingMapper.select(type = mappingType, active = true).firstOrNull()?.id
            RecodeHandlerEnum.entries
                .filter { it.mappingType == mappingType }
                .mapNotNull(handlerInstanceMap::get)
                .filterIsInstance<RecodeStandardHandler<*>>()
                .forEach { it.activeMappingId = activeMappingId }
        }
        mappingTypeMap[mappingId] = recodeMappingMapper.select(mappingId).firstOrNull()?.type
    }
}
