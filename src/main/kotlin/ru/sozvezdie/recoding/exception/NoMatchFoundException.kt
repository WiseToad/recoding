package ru.sozvezdie.recoding.exception

import ru.sozvezdie.recoding.domain.recode.RecodeAttributeEnum

class NoMatchFoundException(
    override var attribute: RecodeAttributeEnum
): RecodeException("No match found for attribute $attribute")
