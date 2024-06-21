package ch.xmedia.mobile.xbasesdk.model

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.network.XBAPIConfig
import com.haidev.kanibis.shared.util.UriHelper
import com.haidev.kanibis.ui.searchList.model.XBAttributeSearchParameters
import java.util.Collections
import java.util.Date


class XBSearchParameters constructor(resultRows: Int = DEFAULT_RESULT_ROWS) {

    private var _tempIdList: MutableList<Int>? = null

    protected var resultAdvertIds: ArrayList<Int>? = null
    /**
     * gets the total count of adverts corresponding the search. Is -1 if no
     * search has been executed.
     *
     * @return the total count of adverts.
     */
    /**
     * the number of total results contained. Is -1 if no object ids has been
     * ever fetched.
     */

    protected var totalCount: Int = -1
        private set

    /**
     * the number of results (advert ids). Default is 1000.
     */

    private var _resultRows: Int = DEFAULT_RESULT_ROWS

    /**
     * offset in result list (paging).
     */

    private var _resultStart: Int = 0

    var category: CategoryListItem? = null

    /**
     * the language of the advert.
     */

    lateinit var language: String

    /**
     * true to return with image only.
     */

    var withImagesOnly: Boolean? = false

    /**
     * the fulltext-search text.
     */

    var searchText: String? = null

    /**
     * the selected search location.
     */

    var searchLocation: String? = null

    /**
     * selected search distance in km used in conjunction with
     * [XBSearchParameters.searchLocation].
     */

    var searchDistance: Int? = null

    /**
     * Field sorted in the result list
     */

    var sortField: String? = null

    /**
     * the sort order of the [XBSearchParameters.sortField]. True: asc
     * False: desc
     */

    var sortOrder: Boolean? = true // ascending order per default

    var attributeFilter: XBAttributeSearchParameters

    /**
     * the ID of the member an advert belongs to.
     */

    var memberId: Int? = null

    /**
     * the name of the user the advert belongs to.
     */

    var username: String? = null

    /**
     * the ID of the advert type.
     */

    var advertType: Int? = null

    /**
     * the state of the adver. Its a short code, eg. "be" for Bern.
     */

    var stateCode: String? = null

    /**
     * indicates if only advert for the specified language should considered.
     */

    var useLanguageFilter: Boolean? = null

    /**
     * the ETag used for detecting if result id list is changed on server
     */

    var eTag: String? = null
        private set

    /**
     * the time in Milliseconds when id list is returned from server
     */

    var dateMillis: Long = -1
        private set

    /**
     * the search counts (per category).

    var searchCounts: XBSearchCounts?
     */

    /**
     * other source for advert IDs than search API (eg. Gallery adverts).

    private var _advertIDsProvider: AdvertIDsProvider? = null

    interface AdvertIDsProvider {
        fun process(
            parameters: XBSearchParameters?,
            apiOperationListener: XBModelListener?
        ): XBOperationWrapper?
    }
     */
    /**
     * set advert IDs provider.
     *
     * @param provider
     * the provider.

    fun setAdvertIDsProvider(provider: AdvertIDsProvider?) {
        _advertIDsProvider = provider
    }
     */
    fun init() {
        if (sortField == null) sortField = CATEGORY_DEFAULT_SORT_FIELD
        if (sortOrder == null) sortOrder = CATEGORY_DEFAULT_SORT_ORDER
    }

    /**
     * indicates if attribute filters are defined.
     *
     * @return true if any attribute filters are defined, otherwise false.
     */
    fun hasAttributeFilters(): Boolean {
        return attributeFilter != null && !attributeFilter!!.isEmpty
    }

    fun hasAttributeFilters(includeCommonAtt: Boolean): Boolean {
        return if (includeCommonAtt) hasAttributeFilters() || hasCommonFilters(true) else hasAttributeFilters()
    }

    private fun hasCommonFilters(includeUnsearchable: Boolean): Boolean {
        return (!TextUtils.isEmpty(searchLocation)
                || !TextUtils.isEmpty(stateCode)
                || (includeUnsearchable && (searchDistance != null) && (searchDistance!! > 0)
                ) || (includeUnsearchable && (useLanguageFilter != null) && useLanguageFilter as Boolean))
    }
    /**
     * creates a [XBSearchParameters]. Uses the current application
     * language.
     *
     * @param resultRows
     * the number of advert ids fetched per request.
     */
    /**
     * creates a [XBSearchParameters]. Uses the current application
     * language.
     */
    init {
        _resultRows = resultRows
       /* language = LocalizationManager.instance()
            .getCurrentLanguageIsoCode()*/
        attributeFilter = XBAttributeSearchParameters()
        /*searchCounts = XBSearchCounts()*/
        resultAdvertIds = ArrayList()
        _tempIdList = ArrayList()
        init()
    }

    private fun hasPrice(): Boolean {
        return this.toString().contains(PRICE_RANGE_ATT_PREFIX)
    }

    /**
     * converts the search parameters to a query string.
     *
     * @return the query string of the search parameters or null if something
     * went wrong.
     */
    override fun toString(): String {
        try {
            val uriBuilder: Uri.Builder = Uri.Builder()
            if (this.category != null) UriHelper.appendIntQueryParameter(
                uriBuilder,
                XBAPIConfig.Parameters.CATEGORY_ID, this.category!!.id
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.ADVERT_LANGUAGE, language
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.WITH_IMAGES_ONLY,
                withImagesOnly
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.SEARCH_TERM, searchText
            )
            UriHelper
                .appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.SEARCH_LOCATION,
                    searchLocation
                )
            UriHelper
                .appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.SEARCH_DISTANCE,
                    searchDistance
                )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.SEARCH_CANTON, stateCode
            )
            if (ALLOW_MEMBER_ID_SEARCH) UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.MEMBER_ID, memberId
            )
            if (ALLOW_USER_NAME_SEARCH) UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.USERNAME, username
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.ADVERT_TYPE, advertType
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.USE_ADVERT_LANGUAGE_FILTER,
                useLanguageFilter
            )
            UriHelper
                .appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.RESULT_PAGE_INDEX,
                    _resultStart
                )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.RESULT_PAGE_SIZE, _resultRows
            )
            UriHelper.appendQueryParameterIfNotEmpty(
                uriBuilder,
                XBAPIConfig.Parameters.SORT_FIELD, sortField
            )
            uriBuilder.appendQueryParameter(
                XBAPIConfig.Parameters.SORT_ORDER,
                if ((sortOrder == null || sortOrder!!)) "a" else "d"
            )
            if (this.hasAttributeFilters()) attributeFilter?.addToParameters(uriBuilder)
            var url: String? = UriHelper.getQueryString(uriBuilder)
            if (url != null) // replace URL encoded commas with decoded commas
                url = url.replace("%2C", ",")
            return url ?: ""
        } catch (e: Exception) {
            return ""
        }
    }

    val attributeFilterString: String?
        get() {
            try {
                val uriBuilder: Uri.Builder = Uri.Builder()
                //            if (this.category != null)
                //                UriHelper.appendIntQueryParameter(uriBuilder,
                //                        XBAPIConfig.Parameters.CATEGORY_ID, this.category.id);
                //            UriHelper.appendQueryParameterIfNotEmpty(uriBuilder,
                //                    XBAPIConfig.Parameters.ADVERT_LANGUAGE, this.language);
                UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.WITH_IMAGES_ONLY,
                    withImagesOnly
                )
                //            UriHelper.appendQueryParameterIfNotEmpty(uriBuilder,
                //                    XBAPIConfig.Parameters.SEARCH_TERM, this.searchText);
                UriHelper
                    .appendQueryParameterIfNotEmpty(
                        uriBuilder,
                        XBAPIConfig.Parameters.SEARCH_LOCATION,
                        searchLocation
                    )
                UriHelper
                    .appendQueryParameterIfNotEmpty(
                        uriBuilder,
                        XBAPIConfig.Parameters.SEARCH_DISTANCE,
                        searchDistance
                    )
                UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.SEARCH_CANTON, stateCode
                )
                if (ALLOW_MEMBER_ID_SEARCH) UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.MEMBER_ID, memberId
                )
                if (ALLOW_USER_NAME_SEARCH) UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.USERNAME, username
                )
                UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.ADVERT_TYPE, advertType
                )
                UriHelper.appendQueryParameterIfNotEmpty(
                    uriBuilder,
                    XBAPIConfig.Parameters.USE_ADVERT_LANGUAGE_FILTER,
                    useLanguageFilter
                )
                //            UriHelper
                //                    .appendQueryParameterIfNotEmpty(uriBuilder,
                //                            XBAPIConfig.Parameters.RESULT_PAGE_INDEX,
                //                            this._resultStart);
                //            UriHelper.appendQueryParameterIfNotEmpty(uriBuilder,
                //                    XBAPIConfig.Parameters.RESULT_PAGE_SIZE, this._resultRows);
                //            UriHelper.appendQueryParameterIfNotEmpty(uriBuilder,
                //                    XBAPIConfig.Parameters.SORT_FIELD, this.sortField);
                //
                //            uriBuilder.appendQueryParameter(XBAPIConfig.Parameters.SORT_ORDER,
                //                    (this.sortOrder == null || this.sortOrder) ? "a" : "d");
                if (this.hasAttributeFilters()) attributeFilter?.addToParameters(uriBuilder)
                var url: String? = UriHelper.getQueryString(uriBuilder)
                if (url != null) // replace URL encoded commas with decoded commas
                    url = url.replace("%2C", ",")
                return url
            } catch (e: Exception) {
                return null
            }
        }

    fun toString(normalized: Boolean): String {
        if (normalized) {
            var result: String = ""
            val list: ArrayList<String> = ArrayList()
            for (s: String in toString().split("&".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()) {
                if ((!s.startsWith(XBAPIConfig.Parameters.RESULT_PAGE_SIZE + "=") &&
                            !s.startsWith(XBAPIConfig.Parameters.RESULT_PAGE_INDEX + "=") &&
                            !s.startsWith(XBAPIConfig.Parameters.SORT_FIELD + "=") &&
                            !s.startsWith(XBAPIConfig.Parameters.SORT_ORDER + "="))
                ) list.add(s)
            }
            if (list.size > 0) {
                Collections.sort(list, object : Comparator<String> {
                    override fun compare(text1: String, text2: String): Int {
                        return text1.compareTo(text2, ignoreCase = true)
                    }
                })
                for (s: String in list) result += ("$s ")
                result = result.trim { it <= ' ' }.replace(" ", "&")
            }
            Log.v("", " toString: " + " | toString. = " + (toString()) + " | result. = " + (result))
            return result
        } else {
            return toString()
        }
    }

    fun toStringPreserveSort(): String {
        var result: String = ""
        val list: ArrayList<String> = ArrayList()
        for (s: String in toString().split("&".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()) {
            if (!s.startsWith(XBAPIConfig.Parameters.RESULT_PAGE_SIZE + "=") &&
                !s.startsWith(XBAPIConfig.Parameters.RESULT_PAGE_INDEX + "=")
            ) list.add(s)
        }
        if (list.size > 0) {
            Collections.sort(list, object : Comparator<String> {
                override fun compare(text1: String, text2: String): Int {
                    return text1.compareTo(text2, ignoreCase = true)
                }
            })
            for (s: String in list) result += ("$s ")
            result = result.trim { it <= ' ' }.replace(" ", "&")
        }
        Log.v("", " toString: " + " | toString. = " + (toString()) + " | result. = " + (result))
        return result
    }

    fun fromString(category: CategoryListItem?, attributeEntries: List<AttributeEntryListItem>?, params: String?, keepAdvertIds: Boolean): Boolean {
        if (TextUtils.isEmpty(params)) return false
        var extracted: Boolean = true
        try {
            val parameters: MutableList<NameValuePair>? = UriHelper
                .getQueryStringParameters(params)
            if (!keepAdvertIds) // reset all parameters and related internals
                reset() else  // just clear assigned search parameters
                this.clear()
            if (parameters != null) {
                for (param: NameValuePair in parameters) {
                    if (!extracted) // return early if something went wrong
                        return false
                    val key: String = param.getName().trim()
                    val value: String = param.getValue().trim()
                    // ignore result rows and result start parameters
                    if ((key == XBAPIConfig.Parameters.CATEGORY_ID)) {
                        this.category = category
                        Log.v("", "0513 fromString this.category ${this.category}");
                    } else if ((key == XBAPIConfig.Parameters.ADVERT_LANGUAGE)) {
                        language = value
                    } else if ((key == XBAPIConfig.Parameters.WITH_IMAGES_ONLY)) {
                        withImagesOnly = value.toBoolean()
                    } else if ((key == XBAPIConfig.Parameters.SEARCH_TERM)) {
                        searchText = value
                    } else if ((key == XBAPIConfig.Parameters.SEARCH_LOCATION)) {
                        searchLocation = value
                    } else if ((key == XBAPIConfig.Parameters.SEARCH_DISTANCE)) {
                        searchDistance = value.toInt()
                    } else if ((key == XBAPIConfig.Parameters.SEARCH_CANTON)) {
                        stateCode = value
                    } else if ((key == XBAPIConfig.Parameters.MEMBER_ID) && ALLOW_MEMBER_ID_SEARCH) {
                        memberId = value.toInt()
                    } else if ((key == XBAPIConfig.Parameters.USERNAME) && ALLOW_USER_NAME_SEARCH) {
                        username = value
                    } else if ((key == XBAPIConfig.Parameters.ADVERT_TYPE)) {
                        advertType = value.toInt()
                    } else if ((key
                                == XBAPIConfig.Parameters.USE_ADVERT_LANGUAGE_FILTER)
                    ) {
                        useLanguageFilter = value.toBoolean()
                    } else if ((key == XBAPIConfig.Parameters.SORT_FIELD)) {
                        sortField = value
                    } else if ((key == XBAPIConfig.Parameters.SORT_ORDER)) {
                        sortOrder = if ((TextUtils.isEmpty(value) || ("a"
                                    == value))
                        ) true else false
                    } else if (((key == XBAPIConfig.Parameters.ATTRIBUTE_ID_LIST)
                                || (key == XBAPIConfig.Parameters.ATTRIBUTE_MULTIID_LIST)
                                || (key == XBAPIConfig.Parameters.ATTRIBUTE_RANGE_LIST)
                                || (key == XBAPIConfig.Parameters.ATTRIBUTE_TEXT_LIST))) {
                        if (attributeFilter == null) attributeFilter = XBAttributeSearchParameters()
                        extracted = attributeFilter!!.fromString(attributeEntries, key, value)
                    }
                }
            }
        } catch (e: Exception) {
            return false
        }
        return extracted
    }

    /**
     * fetches the search counts for this search parameters.
     *
     * @return the operation wrapper corresponding to this fetch operation.

    fun fetchSearchCounts(): XBOperationWrapper {
        return searchCounts.fetchSearchCounts(this)
    }
     */
    /**
     * gets the advert id at the given index in the advert id list.
     *
     * @param index
     * the index of the advert id list.
     * @return the advert id if one exists at the given index, otherwise null.
     */
    @Synchronized
    protected fun getAdvertIdAtIndex(index: Int): Int? {
        if (resultAdvertIdsCount == 0) return null
        if (index < 0 || index >= resultAdvertIds!!.size) return null
        return resultAdvertIds!!.get(index)
    }

    /**
     * resets the search parameters.
     */
    fun reset() {
        clear()
        rewind()
    }

    /**
     * clears the search parameters, but keeps the result advert list.
     */
    fun clear() {
        withImagesOnly = null
        this.category = null
        searchDistance = null
       /* if (attributeFilter != null) attributeFilter.clearAll()*/
        searchLocation = null
        searchText = null
        memberId = null
        stateCode = null
        username = null
        advertType = null
        useLanguageFilter = null
        language = "de"
        sortField = CATEGORY_DEFAULT_SORT_FIELD
        sortOrder = CATEGORY_DEFAULT_SORT_ORDER
    }

    val isSearchable: Boolean
        get() = (!TextUtils.isEmpty(searchText) || (category != null && category!!.id > 0) ||
                !TextUtils.isEmpty(searchLocation) || !TextUtils.isEmpty(stateCode) || (
                memberId != null) || (username != null) || (advertType != null) || hasPrice())
    val isSearchableWithoutCategory: Boolean
        get() {
            return (!TextUtils.isEmpty(searchText) ||
                    !TextUtils.isEmpty(searchLocation) || !TextUtils.isEmpty(stateCode) || (
                    memberId != null) || (username != null) || (advertType != null) || hasPrice())
        }

    /**
     * rewinds the search parameters internals
     */
    protected fun rewind() {
        _resultStart = 0
        totalCount = -1
        if (resultAdvertIds != null) resultAdvertIds!!.clear()
        if (_tempIdList != null) _tempIdList!!.clear()
        /*if (searchCounts != null) searchCounts.clear()*/
    }

    @get:Synchronized
    protected val resultAdvertIdsCount: Int
        /**
         * gets the count of advert ids already fetched.
         *
         * @return the count of already fetched advert ids.
         */
        protected get() {
            if (resultAdvertIds == null) return 0
            return resultAdvertIds!!.size
        }

    @Synchronized
    protected fun needToFetchNextObjectIds(offset: Int, pageSize: Int): Boolean {
        if (resultAdvertIds == null || totalCount == -1) return true
        return (offset + pageSize) > resultAdvertIdsCount && hasMoreObjectIds()
    }

    @Synchronized
    protected fun getObjectIdsFromOffset(offset: Int, pageSize: Int): List<Int>? {
        if (resultAdvertIds == null) return null
        var to: Int = offset + pageSize
        val idsCount: Int = resultAdvertIdsCount
        if (to > idsCount) to = idsCount
        return resultAdvertIds!!.subList(offset, to)
    }

    @Synchronized
    private fun hasMoreObjectIds(): Boolean {
        if (resultAdvertIds == null || totalCount == -1) return true
        return resultAdvertIdsCount < totalCount
    }

    val lastModifiedDate: Date?
        get() {
            return null
        }
    val expireDate: Date?
        get() {
            return null
        }

    fun setDates(statusCode: Int, lastModifiedDate: Date?, expireDate: Date?) {}
    fun hasExpired(): Boolean {
        return true
    }

    fun setETag(statusCode: Int, ETag: String) {
        if (eTag == null || !(eTag == ETag)) dateMillis = System.currentTimeMillis()
        eTag = ETag
    }

    fun addAdvertIds(advertIds: List<Int>?) {
        resultAdvertIds!!.addAll((advertIds)!!)
    }

    companion object {
        val CATEGORY_SORT_FIELD_RELEVANCE: String = "ftw"
        val CATEGORY_SORT_FIELD_DATE: String = "dpo"
        val CATEGORY_DEFAULT_SORT_FIELD: String = CATEGORY_SORT_FIELD_DATE
        val CATEGORY_DEFAULT_SORT_ORDER: Boolean = false
        private val DEFAULT_RESULT_ROWS: Int = 1000
        private val ALLOW_USER_NAME_SEARCH: Boolean = false
        private val ALLOW_MEMBER_ID_SEARCH: Boolean = true
        private val PRICE_RANGE_ATT_PREFIX: String = "834_"
    }
}