package ru.sozvezdie.recoding.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import ru.sozvezdie.recoding.domain.recode.RecodeSchemaHandler

@Mapper
interface RecodeSchemaHandlerMapper {
    fun select(
        @Param("schemaId") schemaId: Long? = null,
        @Param("active") active: Boolean? = null
    ): List<RecodeSchemaHandler>
}
