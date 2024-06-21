package com.haidev.kanibis.ui.searchList.viewmodel
import android.annotation.SuppressLint
import android.text.TextUtils
import android.util.Log
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.NameValuePair
import com.haidev.kanibis.shared.category.model.CategoryList
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.network.XBAPIConfig
import com.haidev.kanibis.shared.parameter.model.LightAdvertDetail
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.parameter.service.IParamService
import com.haidev.kanibis.shared.userData.service.IUserDataService
import com.haidev.kanibis.shared.util.UriHelper
import com.haidev.kanibis.ui.homeCategoryList.viewmodels.HomeCategoryListViewModel
import com.haidev.kanibis.ui.searchList.model.SearchMaskInfo
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class SearchListViewModel(
    private val paramService: IParamService, override val localizationService: ILocalizationService,
    private val masterdataService: IMasterdataService, private val userDataService: IUserDataService,

) :
    ISearchListViewModel, HomeCategoryListViewModel(paramService, localizationService, masterdataService, userDataService)
{
    val PAGE_SIZE = 20
    override var _searchMaskInfo: SearchMaskInfo = SearchMaskInfo()
    override var _searchMaskParams: XBSearchParameters = XBSearchParameters()
    override var _searchResultParamsString = ""

    override var _onAttributeListChanged: PublishSubject<List<AttributeListItem>> = PublishSubject.create()

    override var _onLoadingIdsStateChanged: PublishSubject<Boolean> = PublishSubject.create()
    override var _onNewAdvertReceived: PublishSubject<List<LightAdvertDetail>> = PublishSubject.create()
    override var _ids:List<String> = listOf()
    override var _currentPage: Int = 0

    override fun getCategoryList(selectedId: Int?, updateHistory :Boolean): Single<CategoryList> {
        return super.getCategoryList(selectedId, updateHistory)
    }

    override fun getAttributeList(categoryId: Int?) {
        val d = masterdataService.getCategoryAttribute(categoryId).toObservable()
            .flatMapIterable { attributes -> attributes }
            .flatMapSingle{att -> getAttributeEntries(att)}
            .toList()
            .map(::addChildrenInfo)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe ({list ->
                         for (catAtt in list) {
                             if (catAtt.isSearchable == 1 || !_searchMaskInfo.isSearch) {
                                 if (catAtt.isPriceAttribute()) {
                                     _searchMaskInfo._hasPrice = true
                                 } else if (!_searchMaskInfo.isSearch && catAtt.isPriceTypeAttribute()) {
                                     _searchMaskInfo._hasPriceType = true
                                 }
                             }
                         }
                    _onAttributeListChanged.onNext(list)
                 },
                {error -> Log.v("", "getAttributeList error $error");})
    }

    fun getAttributeEntries(att: XBAttribute): Single<AttributeListItem> {
        return masterdataService.getAttributeEntries(att.id)
            .map{list ->
                val attItem = AttributeListItem.fromAttribute(att)
                val entryList = list.map {item -> AttributeEntryListItem.fromAttributeEntry(item, attItem)}
                attItem.entries = entryList
                return@map attItem
            }
    }

    fun addChildrenInfo(list: MutableList<AttributeListItem>): MutableList<AttributeListItem> {
        val parentMap: MutableMap<Int, List<AttributeListItem>> = mutableMapOf()

        for (catAtt in list) {
            if (catAtt.parentId != null) {
                if (parentMap[catAtt.parentId] == null)
                    parentMap[catAtt.parentId] = listOf(catAtt)
                else
                    parentMap[catAtt.parentId] = parentMap[catAtt.parentId]!! + listOf(catAtt)
            }
        }
        for (catAtt in list) {
            catAtt.children = parentMap[catAtt.parentId]
        }
        return list
    }

    override fun setCategory(category: CategoryListItem?) {
        _searchMaskInfo.category = category
    }

    override fun initSearchQueries(isEdit: Boolean, lostQuery: String?) {
        _searchMaskInfo = SearchMaskInfo()

        _searchMaskInfo._isEdit = isEdit
        _searchMaskInfo._lostQuery = lostQuery
        if (_searchMaskInfo.isSearch) {
            if (_searchMaskInfo._isEdit) {
                _searchMaskInfo._editSearchQuery = _searchMaskParams.toString()
                _searchMaskParams.clear()
                _searchMaskInfo._newSearchQuery = _searchMaskParams.toString()
            } else {
                _searchMaskInfo._editSearchQuery = null
                _searchMaskParams.clear()
                _searchMaskInfo._newSearchQuery = _searchMaskParams.toString()
            }
            if (!TextUtils.isEmpty(_searchMaskInfo._lostQuery)) {
                _searchMaskInfo._editSearchQuery = _searchMaskInfo._lostQuery
            }
        } else {
            _searchMaskInfo._editSearchQuery = if (_searchMaskInfo._isEdit) _searchMaskParams.toString() else null
            _searchMaskParams.clear()
            _searchMaskInfo._newSearchQuery = _searchMaskParams.toString()
        }

        if (_searchMaskInfo.category == null || _searchMaskInfo.category!!.id == 0 || _searchMaskInfo.category!!.id == -1) {
            _searchMaskInfo._hasPrice = true
            _searchMaskInfo._hasPriceType = !_searchMaskInfo.isSearch
        } else {
            _searchMaskInfo._hasPrice = false
            _searchMaskInfo._hasPriceType = false
        }
    }

    override fun setSearchText(text: String) {
         _searchMaskParams.searchText = text
    }

    override fun resetAll(resetKeyword: Boolean) {
        _searchMaskInfo._editSearchQuery = null
        val text = if (resetKeyword) "" else _searchMaskParams.searchText
        if (!resetKeyword) _searchMaskParams.searchText = text
    }

    override fun restoreSearchWithParams(params: String?) {
        _searchMaskInfo._editSearchQuery = params
    }
    data class LastSearchInfo(val category: CategoryListItem?, val attributeEntries: List<AttributeEntryListItem>?)

    override fun restoreLastSearch(): Single<LastSearchInfo> {
        var categoryId: Int? = null
        var entryIds = listOf<Int>()
        val parameters: MutableList<NameValuePair>? = UriHelper
            .getQueryStringParameters(
                if (!TextUtils.isEmpty(_searchMaskInfo._editSearchQuery)) _searchMaskInfo._editSearchQuery else _searchMaskInfo._newSearchQuery)

        if (parameters != null) {
            for (param: NameValuePair in parameters) {
                val key: String = param.getName().trim()
                val value: String = param.getValue().trim()
                if ((key == XBAPIConfig.Parameters.CATEGORY_ID)) {
                    categoryId = value.toInt()
                }
                else if (key == XBAPIConfig.Parameters.ATTRIBUTE_ID_LIST) {

                    val array: Array<String> =
                        value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                    entryIds = array.map {item -> item.toInt()}
                }
            }
        }
        if(categoryId == null) {
            return Single.just(LastSearchInfo(null, null))
        }else {
            val getEntries = masterdataService.getEntries(entryIds)
                .map { entries ->
                    entries.map { entry ->
                        AttributeEntryListItem.fromAttributeEntry(
                            entry,
                            null
                        )
                    }
                }

            val getCategory = masterdataService.getCategory(categoryId)
                .map { category -> CategoryListItem.fromCategory(category, false) }

            return Single.zip(getCategory, getEntries) { c, e -> LastSearchInfo(c, e) }
                .doOnSuccess { lastSearchInfo ->
                    getAttributeList(lastSearchInfo.category?.id)
                    _searchMaskParams.fromString(
                        lastSearchInfo.category, lastSearchInfo.attributeEntries,
                        if (!TextUtils.isEmpty(_searchMaskInfo._editSearchQuery)) _searchMaskInfo._editSearchQuery else _searchMaskInfo._newSearchQuery,
                        false
                    )
                }
        }
    }

    override fun getOrCreatePriceAttribute() : AttributeListItem {
        return AttributeListItem.fromAttribute(
            XBAttribute(1, 0, 0, 1, 1,
            20, null, 6,
            if(localizationService.currentIsoLanguage == "de") "Pries" else
                if(localizationService.currentIsoLanguage == "fr") "Prix" else "Prezzo",
            834, "CHF", 834))
    }

    override fun getSearchCountByParams(): Single<List<SearchCountCategory>> {
        return paramService.getSearchCount(_searchMaskParams.toString())
    }

    override fun getSearchCountByCategoryId(selectedId: Int?): Single<List<SearchCountCategory>> {
        return paramService.getSearchCount(_searchMaskParams.toString())
    }

    fun isParamsChanged(): Boolean {
        return !_searchMaskParams.toString()
            .equals(_searchResultParamsString.replace("&pi=[0-9]*", ""))
    }

    override fun onResume() {
        super.onResume()
        if (isParamsChanged()) {
            onExecuteNewSearch("BaseListResultFragment onResume")
        }
    }

    var _advertClickable = true

    fun onExecuteNewSearch(debug: String) {
        if (isParamsChanged()) {
            _advertClickable = false
            _onLoadingIdsStateChanged.onNext(true)
            _searchResultParamsString = _searchMaskParams.toString()
            val d = paramService.getSearchAdvertIds(_searchResultParamsString)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap { list ->
                    _ids = list
                    _currentPage = 0
                    paramService.getSearchLightDetails(getIdsInPage(0))}
                .subscribe{ list ->
                    _onLoadingIdsStateChanged.onNext(false)
                    _onNewAdvertReceived.onNext(list)
                }
        }
    }

    fun loadNextPage(){
        val d = paramService.getSearchLightDetails(getIdsInPage(++_currentPage))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe{ list ->
                _onNewAdvertReceived.onNext(list)
            }
    }

    fun getIdsInPage(page: Int): List<String> {
        return _ids.filterIndexed{index, id -> index >= page * PAGE_SIZE && index < (page + 1) * PAGE_SIZE}
    }
}

