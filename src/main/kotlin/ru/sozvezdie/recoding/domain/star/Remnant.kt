package ru.sozvezdie.recoding.domain.star

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import ru.sozvezdie.recoding.common.composeKey
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.properties.Delegates

@JsonPropertyOrder("clientId", "mapPharmacyId", "batchId", "date")
class Remnant: StarElement() {
    override var clientId: Long by Delegates.notNull()
    lateinit var mapPharmacyId: String
    lateinit var batchId: String // primary key component
    lateinit var date: LocalDate

    lateinit var openingBalance: BigDecimal
    lateinit var closingBalance: BigDecimal
    lateinit var inputPurchasingPriceBalance: BigDecimal
    lateinit var outputPurchasingPriceBalance: BigDecimal
    lateinit var inputRetailPriceBalance: BigDecimal
    lateinit var outputRetailPriceBalance: BigDecimal

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
    var internetZakaz: String? = null
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
        clone as Remnant

        clone.clientId = clientId
        clone.mapPharmacyId = mapPharmacyId
        clone.batchId = batchId
        clone.date = date

        clone.openingBalance = openingBalance
        clone.closingBalance = closingBalance
        clone.inputPurchasingPriceBalance = inputPurchasingPriceBalance
        clone.outputPurchasingPriceBalance = outputPurchasingPriceBalance
        clone.inputRetailPriceBalance = inputRetailPriceBalance
        clone.outputRetailPriceBalance = outputRetailPriceBalance

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
        clone.internetZakaz = internetZakaz
        clone.nomenclatureCodes = nomenclatureCodes?.copy() // mutable object, so copy it

        // recoded values
        clone.subjectId = subjectId
        clone.nomenclatureId = nomenclatureId
        clone.supplierId = supplierId
        clone.productId = productId
    }
}
