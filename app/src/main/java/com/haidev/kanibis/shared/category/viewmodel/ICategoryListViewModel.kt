package com.haidev.kanibis.shared.category.viewmodel

import com.haidev.kanibis.shared.category.model.CategoryList
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject

interface ICategoryListViewModel: IBaseViewModel {
    fun getCategoryList(selectedId: Int?, updateHistory :Boolean): Single<CategoryList>
    fun getSearchCountByCategoryId(selectedId: Int?): Single<List<SearchCountCategory>>
    fun isBlueFont(): Boolean
    val mOnSearchCountChanged: PublishSubject<Map<Int?, Int>>
}