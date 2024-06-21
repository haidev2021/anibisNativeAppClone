package com.haidev.kanibis.ui.searchList.viewmodel
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.parameter.model.LightAdvertDetail
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.ui.searchList.model.SearchMaskInfo
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

interface ISearchListViewModel: ICategoryListViewModel
{
    var _searchMaskInfo: SearchMaskInfo
    var _searchMaskParams: XBSearchParameters
    var _searchResultParamsString: String
    var _onAttributeListChanged: PublishSubject<List<AttributeListItem>>
    var _onLoadingIdsStateChanged: PublishSubject<Boolean>
    var _onNewAdvertReceived: PublishSubject<List<LightAdvertDetail>>
    var _ids: List<String>
    var _currentPage: Int
    fun initSearchQueries(isEdit: Boolean, lostQuery: String?)
    fun setCategory(category: CategoryListItem?)
    fun setSearchText(text: String)
    fun resetAll(resetKeyword: Boolean)
    fun restoreSearchWithParams(params: String?)
    fun restoreLastSearch(): Single<SearchListViewModel.LastSearchInfo>
    fun getAttributeList(categoryId: Int?)
    fun getOrCreatePriceAttribute() : AttributeListItem
    fun getSearchCountByParams(): Single<List<SearchCountCategory>>
}