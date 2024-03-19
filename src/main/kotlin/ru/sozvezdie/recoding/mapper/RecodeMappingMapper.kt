package ru.sozvezdie.recoding.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import ru.sozvezdie.recoding.domain.recode.RecodeMapping
import ru.sozvezdie.recoding.domain.recode.RecodeMappingTypeEnum

@Mapper
interface RecodeMappingMapper {

    fun select(
        @Param("id") id: Long? = null,
        @Param("type") type: RecodeMappingTypeEnum? = null,
        @Param("active") active: Boolean? = null
    ): List<RecodeMapping>
}
