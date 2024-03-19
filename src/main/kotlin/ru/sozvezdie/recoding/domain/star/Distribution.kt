package ru.sozvezdie.recoding.domain.star

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import ru.sozvezdie.recoding.common.composeKey
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.properties.Delegates

@JsonPropertyOrder("clientId", "mapPharmacyId", "distributionId", "docDate")
class Distribution: StarElement() {
    override var clientId: Long by Delegates.notNull()
    lateinit var mapPharmacyId: String
    lateinit var distributionId: String
    lateinit var docDate: LocalDate

    var docType: Int? = null
    var docNumber: Int? = null
    var posNumber: String? = null
    var checkNumber: Int? = null
    var checkUniqueNumber: String? = null
    lateinit var quantity: BigDecimal
    var purchaseSumNds: BigDecimal? = null
    var retailSumNds: BigDecimal? = null
    var discountSum: BigDecimal? = null
    var cashierId: String? = null
    var cashierFullName: String? = null
    var cashierTin: String? = null
    var resaleSign: Boolean? = null
    var fnDocNumber: String? = null
    var fnDocSign: String? = null
    var fnNumber: String? = null
    var fnDocDate: LocalDateTime? = null
    var internetZakaz: String? = null

    // from batches
    var pharmEtalonId: Long? = null
    var mapNomenclatureCode: String? = null
    var mapNomenclatureName: String? = null
    var mapProducerCode: String? = null
    var mapProducerName: String? = null
    var mapProducerCountryCode: String? = null
    var mapProducerCountryName: String? = null
    var mapSupplierCode: String? = null
    override var mapSupplierTin: String? = null
    var supplierName: String? = null
    var batchDocDate: LocalDate? = null
    var batchDocNumber: String? = null
    var purchasePriceNds: BigDecimal? = null
    var purchaseNds: Int? = null
    var retailPriceNds: BigDecimal? = null
    var retailNds: Int? = null
    override var barcode: Long? = null
    var signCommission: Boolean? = null
    var nomenclatureCodes: NomenclatureCode? = null

    // recoded values
    override var subjectId: Long? = null
    override var nomenclatureId: Long? = null
    override var supplierId: Long? = null
    override var productId: Long? = null

    // recode source values
    override fun getSubjectCode(): String = composeKey(clientId, mapPharmacyId)
    override fun getNomenclaturePharmEtalonCode(): String? = nomenclatureCodes?.pharmEtalon?.toString()
    override fun getNomenclaturePulsCode(): String? = nomenclatureCodes?.puls
    override fun getNomenclatureSiaCode(): String? = nomenclatureCodes?.sia
    override fun getNomenclatureProtekCode(): String? = nomenclatureCodes?.protek
    override fun getNomenclaturePharmacyCode(): String? = if (subjectId == null || mapNomenclatureCode == null) null else composeKey("$subjectId", "$mapNomenclatureCode")

    override fun clone() = super.clone().also { clone ->
        clone as Distribution

        clone.clientId = clientId
        clone.mapPharmacyId = mapPharmacyId
        clone.distributionId = distributionId
        clone.docDate = docDate

        clone.docType = docType
        clone.docNumber = docNumber
        clone.posNumber = posNumber
        clone.checkNumber = checkNumber
        clone.checkUniqueNumber = checkUniqueNumber
        clone.quantity = quantity
        clone.purchaseSumNds = purchaseSumNds
        clone.retailSumNds = retailSumNds
        clone.discountSum = discountSum
        clone.cashierId = cashierId
        clone.cashierFullName = cashierFullName
        clone.cashierTin = cashierTin
        clone.resaleSign = resaleSign
        clone.fnDocNumber = fnDocNumber
        clone.fnDocSign = fnDocSign
        clone.fnNumber = fnNumber
        clone.fnDocDate = fnDocDate
        clone.internetZakaz = internetZakaz

        // from batches
        clone.pharmEtalonId = pharmEtalonId
        clone.mapNomenclatureCode = mapNomenclatureCode
        clone.mapNomenclatureName = mapNomenclatureName
        clone.mapProducerCode = mapProducerCode
        clone.mapProducerName = mapProducerName
        clone.mapProducerCountryCode = mapProducerCountryCode
        clone.mapProducerCountryName = mapProducerCountryName
        clone.mapSupplierCode = mapSupplierCode
        clone.mapSupplierTin = mapSupplierTin
        clone.supplierName = supplierName
        clone.batchDocDate = batchDocDate
        clone.batchDocNumber = batchDocNumber
        clone.purchasePriceNds = purchasePriceNds
        clone.purchaseNds = purchaseNds
        clone.retailPriceNds = retailPriceNds
        clone.retailNds = retailNds
        clone.barcode = barcode
        clone.signCommission = signCommission
        clone.nomenclatureCodes = nomenclatureCodes?.copy() // mutable object, so copy it

        // recoded values
        clone.subjectId = subjectId
        clone.nomenclatureId = nomenclatureId
        clone.supplierId = supplierId
        clone.productId = productId
    }
}
