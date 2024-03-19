package ru.sozvezdie.recoding.exception

import ru.sozvezdie.recoding.domain.recode.RecodeAttributeEnum

class InvalidSourceTypeException(
    override var attribute: RecodeAttributeEnum
): RecodeException("Invalid source type for attribute $attribute")
