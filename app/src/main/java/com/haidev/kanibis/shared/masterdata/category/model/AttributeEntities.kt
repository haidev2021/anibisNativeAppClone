package com.haidev.kanibis.shared.masterdata.category.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.rxjava3.core.Single

@Entity(indices = [Index(value = ["categoryId"], unique = false, orders = [Index.Order.ASC]),
    Index(value = ["attributeId"], unique = false, orders = [Index.Order.ASC])])
data class XBCategoryAttributes (
    @PrimaryKey val id: Int,
    val attributeId: Int,
    val categoryId: Int,
    val isInSummary: Int?,
    val isMainSearch: Int?,
    val isMandatory: Int?,
    val isSearchable: Int?,
    val sortOrder: Int?,
)

@Entity(indices = [Index(value = ["parentId"], unique = false, orders = [Index.Order.ASC]),
    Index(value = ["numericRangeId"], unique = false, orders = [Index.Order.ASC]),
    Index(value = ["defaultSelectItemId"], unique = false, orders = [Index.Order.ASC])])
data class XBAttributes (
    @PrimaryKey val id: Int,
    val parentId: Int?,
    val type: Int?,
    val nameDe: String?,
    val nameFr: String?,
    val nameIt: String?,
    val nameEn: String?,
    val defaultSelectItemId: Int?,
    val unitDe: String?,
    val unitFr: String?,
    val unitIt: String?,
    val unitEn: String?,
    val numericRangeId: Int?,
)


@Entity(indices = [Index(value = ["attributeId"], unique = false, orders = [Index.Order.ASC]),
    Index(value = ["parentId"], unique = false, orders = [Index.Order.ASC])])
data class XBAttributeEntries (
    @PrimaryKey val id: Int,
    val parentId: Int?,
    val attributeId: Int,
    val sortOrder: Int,
    val nameDe: String?,
    val nameFr: String?,
    val nameIt: String?,
    val nameEn: String?
)

data class AttributeEntryListItem (val id: Int, val attributeId: Int, val parentId: Int?,val sortOrder: Int,val name: String?,val attribute: AttributeListItem?) {
    companion object {
        fun fromAttributeEntry(entry: XBAttributeEntry, attribute: AttributeListItem?): AttributeEntryListItem {
            return AttributeEntryListItem(entry.id, entry.attributeId, entry.parentId, entry.sortOrder, entry.name, attribute)
        }
        fun getOrCreateAttributeEntry(id: Int):AttributeEntryListItem? {
            return null//XBAttributeEntry(id, "")
        }

        // from attribute price type 207
        const val ATTRIBUTE_ENTRY_ID_FIXED_PRICE = 15218
        const val ATTRIBUTE_ENTRY_ID_ON_NEGOTIATION = 15219
        const val ATTRIBUTE_ENTRY_ID_ON_DEMAND = 15220
        const val ATTRIBUTE_ENTRY_ID_FREE = 15221

        // from attribute advert type 208
        const val ATTRIBUTE_ENTRY_ID_OFFER = 15222
        const val ATTRIBUTE_ENTRY_ID_SCOUT = 15223

        // from attribute availability 209
        const val ATTRIBUTE_ENTRY_ID_FROM_DATE = 15224
        const val ATTRIBUTE_ENTRY_ID_FROM_NOW = 15225
        const val ATTRIBUTE_ENTRY_ID_FROM_DEMAND = 15226
    }

}

data class XBAttributeEntry (val id: Int,val parentId: Int?,val attributeId: Int,val sortOrder: Int,val name: String?) {

}
data class XBAttribute(val id: Int, val isInSummary: Int?, val isMainSearch: Int?,
                       val isMandatory: Int?, val isSearchable: Int?,
                       val sortOrder: Int?, val parentId: Int?,
                       val type: Int?, val name: String?,
                       val defaultSelectItemId: Int?, val unit: String?, val numericRangeId: Int?
) {

    constructor() : this(1, 0, 0, 1, 1,
        20, null, 6, "Pries", 834, "CHF", 834) {
    }
}

data class AttributeListItem(val id: Int, val isInSummary: Int?, val isMainSearch: Int?,
                             val isMandatory: Int?, val isSearchable: Int?,
                             val sortOrder: Int?, val parentId: Int?,
                             val type: Int?, val name: String?,
                             val defaultSelectItemId: Int?, val unit: String?, val numericRangeId: Int?,
                             var children: List<AttributeListItem>?, var entries: List<AttributeEntryListItem>?) {

    override fun toString(): String {
        return "id ${id} " +
                "isInSummary ${isInSummary} " +
                "isMainSearch ${isMainSearch} " +
                "isMandatory ${isMandatory} " +
                "isSearchable ${isSearchable} " +
                "sortOrder ${sortOrder} " +
                "parentId ${parentId} " +
                "type ${type} " +
                "name ${name} " +
                "defaultSelectItemId ${defaultSelectItemId} " +
                "unit ${unit} " +
                "numericRangeId ${numericRangeId} " +
                "entries size ${entries?.size}"
    }
    fun getAttributeEntries(): List<XBAttributeEntry> {
        return listOf(XBAttributeEntry(1, 2, 3, 4, "mock"))
    }
    fun getAttributeEntries(hasParams: Boolean): List<XBAttributeEntry> {
        return listOf(XBAttributeEntry(1, 2, 3, 4, "mock"))
    }
    fun isPriceAttribute(): Boolean {
        return id == ATTRIBUTE_ID_PRICE
    }
    fun isPriceTypeAttribute(): Boolean {
        return id == ATTRIBUTE_ID_PRICE_TYPE
    }
    fun getParent(): XBAttribute? {
        return null
    }

    fun getDefaultNumericRange(): XBNumericRange {
        return XBNumericRange(0, 0.0, 999999999.0)
    }

    fun getDefaultSelectItemId(): Int {
        return 0
    }

    fun isRoot(): Boolean {
        return false;
    }

    fun isInputType(): Boolean {
        return type == XBAttributeType.InputDate.ordinal
                || type == XBAttributeType.InputDecimal.ordinal
                || type == XBAttributeType.InputInt.ordinal
                || type == XBAttributeType.InputText.ordinal
                || type == XBAttributeType.InputTextSuggest.ordinal
    }
    fun isSelectType(): Boolean {
        return !isInputType()
    }
    fun isNumericInputType(): Boolean {
        return (type == XBAttributeType.InputDecimal.ordinal
                || type == XBAttributeType.InputInt.ordinal)
    }
    fun isIntInputType(): Boolean {
        return type == XBAttributeType.InputInt.ordinal
    }
    fun isDecimalInputType(): Boolean {
        return type == XBAttributeType.InputDecimal.ordinal
    }
    fun isTextInputType(): Boolean {
        return type == XBAttributeType.InputText.ordinal
    }
    fun isDateInputType(): Boolean {
        return type == XBAttributeType.InputDate.ordinal
    }
    fun isMultiSelectType(): Boolean {
        return type == XBAttributeType.SelectMulti.ordinal
    }
    fun isCheckmarkType(): Boolean {
        return type == XBAttributeType.Checkmark.ordinal
    }
    fun isMultiSelectionForSearch(): Boolean {1
        return (type == XBAttributeType.SelectMulti.ordinal
                || type == XBAttributeType.SelectSingleSearchMulti.ordinal)
    }
    fun isSingleSelectionForSearch(): Boolean {
        return type == XBAttributeType.SelectSingle.ordinal
                || type == XBAttributeType.SelectMultiSearchSingle.ordinal
                || type == XBAttributeType.SelectSingleExt.ordinal
    }

    enum class XBAttributeType {
        Undefined,
        SelectSingle,

        // AND search
        SelectMulti,
        SelectMultiSearchSingle,
        InputText,
        InputTextSuggest,
        InputInt,
        InputDecimal,
        InputDate,
        Checkmark,
        SelectSingleExt,

        // OR search
        SelectSingleSearchMulti
    }

    companion object {
        fun fromAttribute(attribute: XBAttribute): AttributeListItem {
            return AttributeListItem(attribute.id, attribute.isInSummary, attribute.isMainSearch,
                attribute.isMandatory, attribute.isSearchable,
                attribute.sortOrder, attribute.parentId,
                attribute.type, attribute.name,
                attribute.defaultSelectItemId, attribute.unit, attribute.numericRangeId, null, null)
        }
        val ATTRIBUTE_ID_PRICE = 1
        val ATTRIBUTE_ID_OBJEKTART = 29
        val ATTRIBUTE_ID_ANIMAL_SKG = 183
        val ATTRIBUTE_ID_PRICE_TYPE = 207
        val ATTRIBUTE_ID_ADVERT_TYPE = 208
        val ATTRIBUTE_ID_AVAILABILITY = 209

        fun getOrCreateAttribute(id: Int):AttributeListItem {
            return AttributeListItem.fromAttribute(XBAttribute())
        }
        fun getPriceTypeAttributeId(): Int {
            return ATTRIBUTE_ID_ADVERT_TYPE;
        }
    }
}


data class XBNumericRange(val id: Int, val  minValue: Double, val  maxValue: Double ) {

}

const val UNIT_COLUMN = "CASE WHEN :language = 'de' THEN unitDe " +
        "WHEN :language = 'fr' THEN unitFr " +
        "WHEN :language = 'it' THEN unitIt " +
        "WHEN :language = 'en' THEN unitEn " +
        "ELSE 'noUnit' END as unit"

@Dao
interface AttributeDao {
    @Transaction
    @Query(
        "select XBAttributes.id, isInSummary, " +
                "isMainSearch, isMandatory, " +
                "isSearchable, sortOrder, " +
                "parentId, type, " + NAME_COLUMN + ",  " +
                "defaultSelectItemId, " + UNIT_COLUMN + ", numericRangeId " +
                "from XBCategoryAttributes, XBAttributes " +
                "where XBCategoryAttributes.categoryId = :categoryId and XBCategoryAttributes.attributeId = XBAttributes.id"
    )
    fun getCategoryAttributes(categoryId: Int?, language: String): Single<List<XBAttribute>>

    @Transaction
    @Query(
        "select id, parentId, attributeId, sortOrder, " + NAME_COLUMN + " " +
                "from XBAttributeEntries " +
                "where attributeId = :attributeId"
    )
    fun getAttributeEntries(attributeId: Int?, language: String): Single<List<XBAttributeEntry>>


    @Transaction
    @Query(
        "select id, parentId, attributeId, sortOrder, " + NAME_COLUMN + " " +
                "from XBAttributeEntries " +
                "where id in (:ids)"
    )
    fun getEntries(ids: List<Int?>, language: String): Single<List<XBAttributeEntry>>
}