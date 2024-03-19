package ru.sozvezdie.recoding.domain.star

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.sozvezdie.recoding.domain.Recodeable

abstract class StarElement: Recodeable() {
    abstract val clientId: Long
    abstract val mapSupplierTin: String?
    abstract val barcode: Long?

    // recoded values
    abstract var subjectId: Long?
    abstract var nomenclatureId: Long?
    abstract var supplierId: Long?
    abstract var productId: Long?

    // recode source values
    @JsonIgnore
    abstract fun getSubjectCode(): String?

    @JsonIgnore
    abstract fun getNomenclaturePharmEtalonCode(): String?

    @JsonIgnore
    abstract fun getNomenclaturePulsCode(): String?

    @JsonIgnore
    abstract fun getNomenclatureSiaCode(): String?

    @JsonIgnore
    abstract fun getNomenclatureProtekCode(): String?

    @JsonIgnore
    abstract fun getNomenclaturePharmacyCode(): String?

    override val schemaSubjectId: Long
        get() = subjectId ?: throw IllegalArgumentException("Failed to resolve recode schema because subjectId is null")
}
