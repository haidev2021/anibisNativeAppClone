package com.haidev.kanibis.ui.homeCategoryList.viewmodels

import android.annotation.SuppressLint
import android.util.Log
import com.haidev.kanibis.shared.category.model.CategoryList
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.category.model.CategoryTree
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.parameter.service.IParamService
import com.haidev.kanibis.shared.userData.service.IUserDataService
import com.haidev.kanibis.shared.util.AnibisUtils.addNonNullItem
import com.haidev.kanibis.shared.util.AnibisUtils.safeFirstListItem
import com.haidev.kanibis.shared.viewmodels.BaseViewModel
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

val HISTORY_UNCHANGED: List<CategoryListItem>? = null
val API_ROOT_CATEGORY_ID = 0

open class HomeCategoryListViewModel(
    private val paramService: IParamService, open val localizationService: ILocalizationService,
    private val masterdataService: IMasterdataService, private val userDataService: IUserDataService
) :
    ICategoryListViewModel, BaseViewModel(localizationService)
{
    override var mOnSearchCountChanged: PublishSubject<Map<Int?, Int>> = PublishSubject.create()

    @SuppressLint("CheckResult")
    override fun getCategoryList(selectedId: Int?, updateHistory :Boolean):Single<CategoryList> {
    val selectedCategories = getSelectedCategories(selectedId)
        .map { list -> listOf(null) + list }

    val children: Single<List<CategoryListItem>> =
        masterdataService.getChildrenOfCategory(selectedId)
            .doOnSuccess { list -> Log.v("", "2904 doOnSuccess size ${list.size} $selectedId") }
            .doOnError { error -> Log.v("", "2904 doOnError error ${error}  $selectedId") }
            .flatMap { list -> convertToListItems(list) }

    return (if (updateHistory)
            Single.zip(
                getHistoryCategories(),
                selectedCategories,
                children
            ) { h, s, c -> CategoryList(h, CategoryTree(s, c), null) }
        else
            Single.zip(selectedCategories, children) { s, c ->
                CategoryList(
                    HISTORY_UNCHANGED,
                    CategoryTree(s, c),
                    null
                )
            }).doAfterSuccess(::getSearchCountForList)
    }

    private fun convertToListItems(categories: List<Category>): Single<List<CategoryListItem>> {
        val ids = categories.map{item -> item.id}
        return masterdataService.filterHasChildren(ids)
            .map{list -> categories.map { item ->
                CategoryListItem.fromCategory(item, list.contains(item.id))
            }}
    }

    private fun convertToListItem(category: Category): Single<CategoryListItem> {
        val ids = listOf(category.id)
        return masterdataService.filterHasChildren(ids)
            .map{list ->
                CategoryListItem.fromCategory(category, list.contains(category.id))
            }
    }

    override fun getSearchCountByCategoryId(selectedId: Int?): Single<List<SearchCountCategory>> {
        return paramService.getSearchCount("cid=$selectedId")
    }

    private fun getSearchCountForList(list: CategoryList) {
        var ids: List<Int?>
        ids = if (list.history == null) listOf() else list.history.map{item -> item.id}
        var selectedId = if (list.tree.selectedCategories == null
            || list.tree.selectedCategories.last() == null) API_ROOT_CATEGORY_ID else
            (list.tree.selectedCategories.last()!!.id)
        ids= ids + listOf(selectedId)

        val d = getSearchCountByCategoryIds(ids, selectedId)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe ({ list ->
            Log.v("", "getSearchCountForList list ${list}");
            val resultMap: MutableMap<Int?, Int> = mutableMapOf()
            list.map{item -> resultMap[if (item.id == API_ROOT_CATEGORY_ID) null else item.id] = item.count}
            mOnSearchCountChanged.onNext(resultMap)
        }, {error -> Log.v("", "getSearchCountForList error $error");})
    }

    fun getSearchCountByCategoryIds(selectedIds: List<Int?>, selectedId: Int?): Single<List<SearchCountCategory>> {
        return Observable.fromIterable(selectedIds)
            .flatMapSingle{
                id -> getSearchCountByCategoryId(id)
                    .map{list ->
                        if (selectedId == id) list
                        else list.filter { item -> item.id == id }}}
            .toList().map{ list -> list.flatten()}
    }

    fun getHistoryCategories(): Single<List<CategoryListItem>> {
        return userDataService.getUserData()
            .flatMap{data -> masterdataService.getCategories(data.historyCategoryIds)}
            .toObservable()
            .flatMapIterable { categories -> categories }
            .flatMapSingle{cat -> getSelectedCategories(cat.id)}
            .map{list ->
                CategoryListItem.cloneWithNewName(
                    list.last(),
                    list.foldIndexed("") {
                        index, acc, cat -> acc + cat.name + if (index < list.size - 1) " / " else ""})
            }
            .toList()
    }

    private fun addNextParentCategory(list: List<CategoryListItem>): Single<List<CategoryListItem>> {
        return masterdataService.getCategory(safeFirstListItem(list)?.parentId)
            .flatMap { category -> convertToListItem(category) }
            .map { categoryListItem -> addNonNullItem(categoryListItem, list)
        }.onErrorResumeNext{e -> run {
                Log.v("", "2104 addNextParentCategory e $e")
                Single.just(list)
            }
        }
    }

    private fun getSelectedCategories(selectedId: Int?): Single<List<CategoryListItem>> {
        val selectedCategory: Single<CategoryListItem> = masterdataService.getCategory(selectedId)
            .flatMap { category -> convertToListItem(category) }

        val selectedCategories = selectedCategory.map { category -> addNonNullItem(category, arrayListOf()) }
            .onErrorResumeNext{e -> run {
                Single.just(arrayListOf())
            }
            }
            .flatMap { list -> addNextParentCategory(list) }
            .flatMap { list -> addNextParentCategory(list) }

        return selectedCategories;
    }

    override fun isBlueFont(): Boolean {
        return true;
    }
}