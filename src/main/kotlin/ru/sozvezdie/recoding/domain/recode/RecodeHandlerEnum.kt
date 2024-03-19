package ru.sozvezdie.recoding.domain.recode

import ru.sozvezdie.recoding.domain.Recodeable
import ru.sozvezdie.recoding.domain.star.StarElement
import ru.sozvezdie.recoding.handler.RecodeHandler
import ru.sozvezdie.recoding.handler.RecodeStandardHandler

enum class RecodeHandlerEnum(
    val attribute: RecodeAttributeEnum,
    val mappingType: RecodeMappingTypeEnum?,
    val accessor: Accessor<*>,
    val clazz: Class<out RecodeHandler<*>> = RecodeStandardHandler::class.java
) {
    STAR_SUBJECT(
        RecodeAttributeEnum.STAR_SUBJECT, RecodeMappingTypeEnum.STAR_SUBJECT,
        Accessor(StarElement::getSubjectCode, StarElement::subjectId.setter)
    ),
    STAR_NOMENCLATURE_PHARM_ETALON(
        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_NOMENCLATURE_PHARM_ETALON,
        Accessor(StarElement::getNomenclaturePharmEtalonCode, StarElement::nomenclatureId.setter)
    ),
//    STAR_NOMENCLATURE_PULS(
//        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_NOMENCLATURE_PULS,
//        Accessor(StarElement::getNomenclaturePulsCode, StarElement::nomenclatureId.setter)
//    ),
//    STAR_NOMENCLATURE_SIA(
//        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_NOMENCLATURE_SIA,
//        Accessor(StarElement::getNomenclatureSiaCode, StarElement::nomenclatureId.setter)
//    ),
//    STAR_NOMENCLATURE_PROTEK(
//        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_NOMENCLATURE_PROTEK,
//        Accessor(StarElement::getNomenclatureProtekCode, StarElement::nomenclatureId.setter)
//    ),
//    STAR_NOMENCLATURE_PHARMACY(
//        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_NOMENCLATURE_PHARMACY,
//        Accessor(StarElement::getNomenclaturePharmacyCode, StarElement::nomenclatureId.setter)
//    ),
//    STAR_BARCODE(
//        RecodeAttributeEnum.STAR_NOMENCLATURE, RecodeMappingTypeEnum.STAR_BARCODE,
//        Accessor(StarElement::barcode, StarElement::nomenclatureId.setter)
//    ),
//    STAR_SUPPLIER(
//        RecodeAttributeEnum.STAR_SUPPLIER, RecodeMappingTypeEnum.STAR_SUPPLIER,
//        Accessor(StarElement::mapSupplierTin, StarElement::supplierId.setter)
//    ),
//    STAR_PRODUCT(
//        RecodeAttributeEnum.STAR_PRODUCT, RecodeMappingTypeEnum.STAR_PRODUCT,
//        Accessor(StarElement::nomenclatureId, StarElement::productId.setter)
//    )
    ;

    class Accessor<T: Recodeable>(
        val getter: (element: T) -> Any?,
        val setter: (element: T, value: Long?) -> Unit
    )
}
