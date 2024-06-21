package com.haidev.kanibis.ui.searchList.model

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.network.XBAPIConfig
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.util.UriHelper
import java.util.AbstractMap
import java.util.Date
import java.util.TreeMap
import java.util.TreeSet

class XBAttributeSearchParameters {
    protected var id = 0

    /**
     * list of attribute entry ids selected for AND search.
     */
    protected lateinit var attributeIds: XBAttributeIdMap

    /**
     * list of attribute entry ids selected for OR search. key: attribute id.
     * value: list with selected attribute entry ids. Use sorted to have the
     * values in the URL always in the same order. Avoids multiple URLs for
     * identical pages.
     */
    protected lateinit var attributeOrIds: XBAttributeOrIdMap

    /**
     * list containing selected attribute id and text.
     */
    protected lateinit var attributeTexts: XBAttributeTextList

    /**
     * list containing attribute id with from/to selected range. Set from/to to
     * 0 to ignore.
     */
    protected lateinit var attributeRanges: XBAttributeRangeList

    /**
     * creates a [XBAttributeSearchParameters].
     */
    constructor() {
        attributeIds = XBAttributeIdMap()
        attributeOrIds = XBAttributeOrIdMap()
        attributeTexts = XBAttributeTextList()
        attributeRanges = XBAttributeRangeList()
    }

    constructor(attSearchParams: XBAttributeSearchParameters) {
        attributeIds = XBAttributeIdMap(attSearchParams.attributeIds)
        attributeOrIds = XBAttributeOrIdMap(attSearchParams.attributeOrIds)
        attributeTexts = XBAttributeTextList(attSearchParams.attributeTexts)
        attributeRanges = XBAttributeRangeList(attSearchParams.attributeRanges)
    }

    fun clearAll() {
        attributeIds!!.clear()
        attributeOrIds!!.clear()
        attributeTexts!!.clear()
        attributeRanges!!.clear()
    }

    /**
     * clears an attribute.
     *
     * @param attribute
     * the attribute to clear.
     */
    fun clearAttribute(attribute: AttributeListItem) {
        if (attribute.isInputType()) {
            if (attribute.isNumericInputType()) removeAttributeRange(attribute.numericRangeId) else removeAttributeText(
                attribute.id
            )
        } else if (attribute.isSelectType()) {
            if (attribute.type == AttributeListItem.XBAttributeType.SelectSingleSearchMulti.ordinal) attributeOrIds!!.remove(
                attribute.id
            ) else attributeIds!!.remove(attribute.id)
        }
    }

    val isEmpty: Boolean
        /**
         * indicates if no attribute search parameters exist.
         *
         * @return true if no attribute search parameters exist, otherwise false.
         */
        get() = (attributeIds!!.isEmpty() && attributeOrIds!!.isEmpty()
                && attributeRanges!!.isEmpty()
                && attributeTexts!!.isEmpty())

    /**
     * sets the text for the attribute. Replaces an existing text for the same
     * attribute.
     *
     * @param attribute
     * the attribute.
     * @param text
     * the text.
     * @return true if the text has been added, otherwise false.
     */
    fun setAttributeText(attribute: AttributeListItem?, text: String?): Boolean {
        if (attribute == null) return false
        return if (attribute.isInputType() && !attribute.isNumericInputType()) {
            Log.v("", "0513 setAttributeText IF ");
            this.setAttributeText(attribute.id, text)
        } else false
    }

    /**
     * sets a date as text for the attribute. Replaces an existing date for the
     * same attribute.
     *
     * @param attribute
     * the attribute.
     * @param date
     * the date.
     * @return true if the date text has been added, otherwise false.
     */
    fun setAttributeDate(attribute: AttributeListItem?, date: Date?): Boolean {
        if (attribute == null) return false
        if (attribute.isInputType() && !attribute.isNumericInputType()) {
            val dateString: String? = if (date != null) AnibisUtils
                .getISO8601TimeStamp(date) else null
            return this.setAttributeText(attribute.id, dateString)
        }
        return false
    }

    /**
     * adds the attribute entry for the attribute.
     *
     * @param attributeEntry
     * the attribute entry to set.
     * @return true when the attribute entry has been added, otherwise false.
     */
    fun addAttributeEntry(attributeEntry: AttributeEntryListItem): Boolean {
        if (attributeEntry == null) {
            return false
        }
        val attribute: AttributeListItem = attributeEntry.attribute!!
        return if (attribute.isSelectType()) {
            if (attribute.type == AttributeListItem.XBAttributeType.SelectSingleSearchMulti.ordinal) addAttributeOrId(
                attribute.id,
                attributeEntry.id
            ) else this.addAttributeId(attributeEntry)
        } else false
    }

    /**
     * removes the attribute entry for the attribute.
     *
     * @param entry
     * the attribute entry to remove.
     * @return true when the attribute entry has been removed, otherwise false.
     */
    fun removeAttributeEntry(entry: AttributeEntryListItem?): Boolean {
        if (entry != null) {
            val set: MutableSet<AttributeEntryListItem> = HashSet()
            set.add(entry)
            return removeAttributeEntry(set)
        }
        return false
    }

    protected fun removeAttributeEntry(attributeEntryIds: Set<AttributeEntryListItem>): Boolean {
        if (attributeEntryIds == null || attributeEntryIds.isEmpty()) {
            return false
        }
        for (entry in attributeEntryIds) {
//            var entry: AttributeEntryListItem
//            entry = try {
//                XBDatabaseManager.instance().getAttributeEntryById(
//                    entryId
//                )
//            } catch (e: SQLException) {
//                return false
//            }
            if (entry != null && entry.attribute != null) {
      /*          val attributes: List<AttributeListItem> = entry.attribute.children ?: listOf() //refactor
                val it: Iterator<AttributeListItem> = attributes.iterator()

                while (it.hasNext()) {
                    val attribute: AttributeListItem = it.next()
                    val selectedEntries = getAttributeIds()[attribute.id]
                    if (selectedEntries != null && !selectedEntries.isEmpty()) {
                        removeAttributeEntry(selectedEntries)
                    }
                }*/

                if (entry.attribute.isSelectType()) {
                    var entrySet: TreeSet<Int>
                    entrySet =
                        if (entry.attribute.type == AttributeListItem.XBAttributeType.SelectSingleSearchMulti.ordinal) {
                            attributeOrIds[entry.attribute.id]!!
                        } else {
                            attributeIds[entry.attribute.id]!!
                        }

                    // remove whole set if no more entry exist for this
                    // attribute
                    if (entrySet != null && !entrySet.isEmpty()) {
                        entrySet.remove(entry.id)
                        if (entrySet.isEmpty()) {
                            if (entry.attribute.type == AttributeListItem.XBAttributeType.SelectSingleSearchMulti.ordinal) {
                                attributeOrIds.remove(entry.attribute.id)
                            } else {
                                attributeIds.remove(entry.attribute.id)
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    /**
     * sets a range for the attribute. If a range value is not defined (open),
     * set it to null. Replaces an existing range for the same attribute.
     *
     * @param attribute
     * the attribute.
     * @param rangeFrom
     * the range from value.
     * @param rangeTo
     * the range to value.
     * @return true if the range has been added, otherwise false.
     */
    fun setAttributeRange(
        attribute: AttributeListItem?, rangeFrom: Double?,
        rangeTo: Double?
    ): Boolean {
        if (attribute == null) return false
        return if (attribute.isNumericInputType()) {
            if (attribute.numericRangeId != null) {
                // if (rangeFrom != null
                // && (rangeFrom < getNumericRange.minValue || rangeFrom >
                // getNumericRange.maxValue))
                // return false;
                //
                // if (rangeTo != null
                // && (rangeTo < getNumericRange.minValue || rangeTo >
                // getNumericRange.maxValue))
                // return false;

                // set numeric range id, not the attribute id
                addAttributeRange(
                    attribute.id,
                    rangeFrom, rangeTo
                )
            } else false
        } else false
    }

    /**
     * gets the attribute texts.
     *
     * @return the list of attribute texts.
     */
    fun getAttributeTexts(): List<AbstractMap.SimpleEntry<Int, String>> {
        val list: MutableList<AbstractMap.SimpleEntry<Int, String>> = ArrayList()
        if (attributeTexts != null) list.addAll(attributeTexts)
        return list
    }

    /**
     * gets the attribute ids.
     *
     * @return the list of attribute ids.
     */
    fun getAttributeIds(): TreeMap<Int, TreeSet<Int>> {
        val map = TreeMap<Int, TreeSet<Int>>()
        if (attributeIds != null) map.putAll(attributeIds)
        if (attributeOrIds != null) map.putAll(attributeOrIds)
        return map
    }

    /**
     * gets the attribute ranges.
     *
     * @return the list of attribute ranges.
     */
    fun getAttributeRanges(): List<AbstractMap.SimpleEntry<Int, AbstractMap.SimpleEntry<Double, Double>>> {
        val list: MutableList<AbstractMap.SimpleEntry<Int, AbstractMap.SimpleEntry<Double, Double>>> =
            ArrayList()
        if (attributeRanges != null) list.addAll(attributeRanges)
        return list
    }

    /**
     * adds the attribute search parameters as query parameters.
     *
     * @param uriBuilder
     * the URI builder the query parameters are append.
     */
    fun addToParameters(uriBuilder: Uri.Builder) {
        var params: MutableList<String?>
        if (attributeTexts != null && attributeTexts!!.size > 0) {
            params = ArrayList()
            for ((key, value) in attributeTexts!!) params.add(key.toString() + "_" + value)
            UriHelper.appendListQueryParameter(
                uriBuilder,
                XBAPIConfig.Parameters.ATTRIBUTE_TEXT_LIST, params, ","
            )
        }
        if (attributeOrIds != null && attributeOrIds!!.size > 0) {
            params = ArrayList()
            for ((key, value) in attributeOrIds!!) {
                params.add(
                    key.toString() + "_"
                            + TextUtils.join("_", value)
                )
            }
            UriHelper.appendListQueryParameter(
                uriBuilder,
                XBAPIConfig.Parameters.ATTRIBUTE_MULTIID_LIST, params, ","
            )
        }
        if (attributeRanges != null && attributeRanges!!.size > 0) {
            params = ArrayList()
            for ((key, value) in attributeRanges!!) {
                if (value.key != null && value
                        .key != 0.0 || value.value != null && value.value != 0.0
                ) {
                    params.add(toArgumentString(key, value.key, value.value))
                }
            }
            Log.v("", "0513 addToParameters attributeRanges ${attributeRanges}");
            UriHelper.appendListQueryParameter(
                uriBuilder,
                XBAPIConfig.Parameters.ATTRIBUTE_RANGE_LIST, params, ","
            )
        }
        if (attributeIds != null && attributeIds!!.size > 0) {
            params = ArrayList()
            for ((key, value) in attributeIds!!) {
                for (entryId in value!!) params.add(toArgumentString(key, entryId))
            }
            Log.v("", "0513 addToParameters params ${params}");
            UriHelper.appendListQueryParameter(
                uriBuilder,
                XBAPIConfig.Parameters.ATTRIBUTE_ID_LIST, params, ","
            )
        }
    }

    /**
     * adds the attribute search parameters from the query string parameter by
     * trying to match and extract the data.
     *
     * @param key
     * the parameter key.
     * @param value
     * the parameter value.
     * @return true if matching and extracting was successfully, otherwise
     * false.
     */
    fun fromString(attributeEntries: List<AttributeEntryListItem>?, key: String, value: String): Boolean {
        return try {
            if (key == XBAPIConfig.Parameters.ATTRIBUTE_ID_LIST) {
                val idArray =
                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                // get attribute entries for lookup
                val attributeEntryMap: SparseArray<AttributeEntryListItem> =
                    SparseArray<AttributeEntryListItem>()

                if (attributeEntries != null) {
                    for (entry in attributeEntries) {
                        attributeEntryMap.put(entry.id, entry)
                    }
                }

                for (id in idArray) {
                    val attEntryId = id.toInt()
                    if (attEntryId > 0) {
                        val attributeEntry: AttributeEntryListItem = attributeEntryMap
                            .get(attEntryId)
                        if (attributeEntry != null
                        ) this.addAttributeId(
                            attributeEntry.attributeId,
                            attEntryId
                        ) else return false
                    } else return false
                }
            } else if (key == XBAPIConfig.Parameters.ATTRIBUTE_TEXT_LIST) {
                val entries =
                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (entry in entries) {
                    val idTexts = entry.split("_".toRegex()).toTypedArray()
                    if (idTexts.size == 2) {
                        val attId = idTexts[0].toInt()
                        val text = idTexts[1].trim { it <= ' ' }
                        if (attId > 0 && !TextUtils.isEmpty(text)) this.setAttributeText(
                            attId,
                            text
                        ) else return false
                    } else return false
                }
            } else if (key
                == XBAPIConfig.Parameters.ATTRIBUTE_MULTIID_LIST
            ) {
                val entries =
                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (entry in entries) {
                    val attIds = entry.split("_".toRegex()).toTypedArray()
                    if (attIds.size > 1) {
                        val attId = attIds[0].toInt()
                        for (i in 1 until attIds.size) {
                            val id = attIds[i].toInt()
                            addAttributeOrId(attId, id)
                        }
                    } else return false
                }
            } else if (key == XBAPIConfig.Parameters.ATTRIBUTE_RANGE_LIST) {
                val rangeArray =
                    value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (range in rangeArray) {
                    val idRange = range.split("_".toRegex()).toTypedArray()
                    if (idRange.size == 3) {
                        val attId = idRange[0].toInt()
                        if (attId > 0) {
                            var from: Double? = null
                            var to: Double? = null
                            if (!TextUtils.isEmpty(idRange[1])) from = idRange[1].toDouble()
                            if (!TextUtils.isEmpty(idRange[2])) to = idRange[2].toDouble()
                            addAttributeRange(attId, from, to)
                        } else return false
                    } else return false
                }
            } else return false // unknown key
            true
        } catch (e: Exception) {
            false
        }
    }

    protected fun setAttributeText(attributeId: Int, text: String?): Boolean {
        // remove old existing text
        removeAttributeText(attributeId)

        // don't set text if empty
        return if (TextUtils.isEmpty(text)) true else attributeTexts!!.add(
            AbstractMap.SimpleEntry<Int, String>(
                attributeId, text
            )
        )
    }

    protected fun addAttributeOrId(
        attributeId: Int,
        attributeEntryId: Int
    ): Boolean {
        if (!attributeOrIds!!.containsKey(attributeId)) attributeOrIds!![attributeId] = TreeSet()
        return attributeOrIds!![attributeId]!!.add(attributeEntryId)
    }

    protected fun addAttributeId(
        attributeId: Int,
        attributeEntryId: Int
    ): Boolean {
        if (!attributeIds!!.containsKey(attributeId)) attributeIds!![attributeId] = TreeSet()
        return attributeIds!![attributeId]!!.add(attributeEntryId)
    }

    protected fun addAttributeRange(
        numericRangeId: Int,
        rangeFrom: Double?, rangeTo: Double?
    ): Boolean {

        // remove existing range
        removeAttributeRange(numericRangeId)

        // don't add range if both are null
        return if (rangeFrom == null && rangeTo == null || rangeFrom != null && rangeFrom == 0.0 && rangeTo != null && rangeTo == 0.0) true else attributeRanges
            .add(
                AbstractMap.SimpleEntry(
                    numericRangeId, AbstractMap.SimpleEntry(
                        rangeFrom, rangeTo
                    )
                )
            )
    }

    private fun removeAttributeText(attributeId: Int) {
        val it = attributeTexts
            .iterator()
        while (it.hasNext()) {
            val entry = it.next()
            if (entry!!.key == attributeId) it.remove()
        }
    }

    private fun addAttributeId(attributeEntry: AttributeEntryListItem): Boolean {
        val attribute: AttributeListItem = attributeEntry.attribute!!

        // remove existing entry for attribute if its a single selection type
        // (because its an AND relation)
        if (attribute.isSingleSelectionForSearch()) attributeIds!!.remove(attribute.id)
        return this.addAttributeId(attribute.id, attributeEntry.id)
    }

    private fun toArgumentString(
        attributeId: Int, fromValue: Double?,
        toValue: Double?
    ): String {
        // attributeID_from_to
        var `val` = attributeId.toString() + "_"
        if (fromValue != null && fromValue > 0) `val` += fromValue
        `val` += "_"
        if (toValue != null && toValue > 0) `val` += toValue
        return `val`
    }

    private fun toArgumentString(
        attributeId: Int, entry: Int?
    ): String {
        // attributeID_from_to
        var `val` = attributeId.toString() + "_"
        if (entry != null && entry > 0) `val` += entry
        return `val`
    }

    private fun removeAttributeRange(numericRangeId: Int?) {
        if(numericRangeId != null) {
            val it = attributeRanges!!
                .iterator()
            while (it.hasNext()) {
                val entry = it.next()
                if (entry!!.key == numericRangeId) it.remove()
            }
        }
    }

    /**
     * the list containing texts for attributes.
     *
     * @author marsta
     */
    class XBAttributeTextList : ArrayList<AbstractMap.SimpleEntry<Int, String>> {
        constructor() : super()
        constructor(list: XBAttributeTextList?) : super(list)

        companion object {
            /**
             *
             */
            private const val serialVersionUID = -8352557154444591235L
        }
    }

    /**
     * the map containing the lists with selected attribute entry ids for OR
     * search. The lists self and the attribute entry ids within a list are
     * naturally ordered.
     *
     * @author marsta
     */
    class XBAttributeOrIdMap : TreeMap<Int, TreeSet<Int>> {
        constructor() : super()
        constructor(map: XBAttributeOrIdMap?) : super(map)

        companion object {
            /**
             *
             */
            private const val serialVersionUID = 5132848546385099325L
        }
    }

    /**
     * the map containing the lists with selected attribute entry ids for AND
     * search. The lists self and the attribute entry ids within a list are
     * naturally ordered.
     *
     * @author marsta
     */
    class XBAttributeIdMap : TreeMap<Int, TreeSet<Int>> {
        constructor() : super()
        constructor(map: XBAttributeIdMap?) : super(map)

        companion object {
            /**
             *
             */
            private const val serialVersionUID = 5132848546385099326L
        }
    }

    /**
     * the list containing ranges for attributes.
     *
     * @author marsta
     */
    class XBAttributeRangeList :
        ArrayList<AbstractMap.SimpleEntry<Int, AbstractMap.SimpleEntry<Double, Double>>> {
        constructor() : super()
        constructor(list: XBAttributeRangeList?) : super(list)

        companion object {
            /**
             *
             */
            private const val serialVersionUID = -3453001396240991492L
        }
    }
}