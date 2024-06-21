package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.util.AnibisUtils.notifySetContentDescription
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class ABaseCategoryView : ABaseAttributeItemView,
    ABaseAttributeItemView.ErrorListProvider {
    val USE_OVER_FOCUS = false
    abstract val categoryArray: ArrayList<CategoryListItem>

	abstract var updateAttributeLayoutListener: UpdateAttributeLayoutListener?
    abstract var rootInstance: ABaseCategoryView?
    abstract val isAutoExpandNextLevel: Boolean
    abstract fun initByChildMostCategory(category: CategoryListItem?)
    open var selectCategory: CategoryListItem? = null
    var _child: ABaseCategoryView? = null
    var _hardcodedCat: CategoryListItem? = null
    protected var _level = -1
    var _overFocus = false
    protected var _sub: ViewGroup? = null

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context!!, viewModel) {
        init()
    }

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    private fun init() {
        _sub = findViewById<View>(R.id.sub) as ViewGroup?
        setLevel(0)
    }

    fun resetArray() {
        categoryArray.clear()
    }

    interface UpdateAttributeLayoutListener {
        fun onUpdateAttributeLayout(finalCategory: CategoryListItem?)
    }

    fun setLevel(l: Int) {
        _level = l
        notifySetContentDescription(valueView, "category_$_level")
    }

    abstract fun getNewCategorySearchItemAsChild(
        context: Context?,
        text: String?
    ): ABaseCategoryView?

    protected fun updateCategoryHierarchy(newCat: CategoryListItem?) {
        if (selectCategory == null || categoryArray.indexOf(selectCategory) >= 0) {
            if (selectCategory != null) {
                val pos = categoryArray.indexOf(selectCategory)
                while (categoryArray.size > pos) {
                    categoryArray.removeAt(pos)
                }
            }
            if (newCat != null) categoryArray.add(newCat)
            rootInstance?.onValueChanged(params)
            if (updateAttributeLayoutListener != null) updateAttributeLayoutListener!!.onUpdateAttributeLayout(
                finalCategory
            )
        }
    }

    var _autoClickNext = false
    fun autoClick() {
        _autoClickNext = true
        onValueClick()
        forceFocus()
        _autoClickNext = false
    }

    open val finalCategory: CategoryListItem?
        get() {
            val catList = categoryArray
            return if (catList.size == 0) null else catList[catList.size - 1]
        }

    override fun onMainValueChanged(params: XBSearchParameters) {
        if (finalCategory != null) {
            params.category = finalCategory
            params.attributeFilter.clearAll()
        } else {
            params.category = null
            params.attributeFilter.clearAll()
        }
    }

    protected fun notifyUpdateItemMainVisible(item: ABaseCategoryView, k: CategoryListItem) {
        if (_hardcodedCat != null) {
            item.setMainVisible(
                k.id > _hardcodedCat!!.id
                        || _hardcodedCat!!.id == 0
            )
        }
    }

    fun getCategoryHierarchyText(separator: String?): String {
        val sb = StringBuilder()
        for ((_, name) in categoryArray) {
            if (sb.length > 0) sb.append(separator)
            sb.append(name)
        }
        return sb.toString()
    }

    override fun onXClick() {}
}