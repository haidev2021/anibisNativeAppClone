package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment.OnBrowserCategoryListener
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class ABaseSearchCategoryView : ABaseCategoryView {
    override val categoryArray = ArrayList<CategoryListItem>()
    override var updateAttributeLayoutListener: UpdateAttributeLayoutListener? = null

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context, viewModel) {
    }

    abstract val itemLayout: Int
    override fun initParentView(context: Context, attrs: AttributeSet?, defStyle: Int) {
        this.orientation = HORIZONTAL
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 0)
        setLayoutParams(params)
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = vi.inflate(itemLayout, null)
        this.addView(v)
        initViews(context, attrs, defStyle)
    }

    abstract val selectorFragment: BaseCategoryListFragment<*>?

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    abstract val onBrowserCategoryListener: OnBrowserCategoryListener?

    override fun initByChildMostCategory(category: CategoryListItem?) {
        selectCategory = category
        rootInstance?.onValueChanged(params)
        if (updateAttributeLayoutListener != null) updateAttributeLayoutListener!!.onUpdateAttributeLayout(
            selectCategory
        )
        updateUIStringValue(category?.name ?: "")
    }

    override val isAutoExpandNextLevel: Boolean
        get() = false

    override fun getNewCategorySearchItemAsChild(
        context: Context?,
        text: String?
    ): ABaseCategoryView? {
        return null
    }

    override var rootInstance: ABaseCategoryView? = this

    override var selectCategory: CategoryListItem? = null
        set(selectCategory) {
            field = selectCategory
        }

    override val finalCategory: CategoryListItem?
        get() = selectCategory
}
