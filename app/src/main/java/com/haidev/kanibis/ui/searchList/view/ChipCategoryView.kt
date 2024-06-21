package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.controller.ListResultFragment
import com.haidev.kanibis.ui.searchList.controller.SearchCategoryPillFragment
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class ChipCategoryView(context: Context, viewModel: ISearchListViewModel) : ABaseSearchCategoryView(context, viewModel) {
    var _fragment: SearchCategoryPillFragment? = null
    override val itemLayout: Int
        get() = R.layout.attribute_item_category_chip

  override fun onMainValueChanged(params: XBSearchParameters) {}
    
    override val selectorFragment: BaseCategoryListFragment<ISearchListViewModel>
        get() {
            if (_fragment == null) {
                _fragment = SearchCategoryPillFragment()
                _fragment!!.setAttributeItemView(this)
                _fragment!!.setBrowsingListener(onBrowserCategoryListener)
            }
            return _fragment as SearchCategoryPillFragment
        }
    val newSelectorFragment: BaseCategoryListFragment<ISearchListViewModel>
        get() {
            _fragment = SearchCategoryPillFragment()
            _fragment!!.setAttributeItemView(this)
            _fragment!!.setBrowsingListener(onBrowserCategoryListener)
            return _fragment as SearchCategoryPillFragment
        }
    
    var _onBrowserCategoryListener: BaseCategoryListFragment.OnBrowserCategoryListener? = null
    override val onBrowserCategoryListener: BaseCategoryListFragment.OnBrowserCategoryListener
        get() {
            if (_onBrowserCategoryListener == null) {
                _onBrowserCategoryListener = object : BaseCategoryListFragment.OnBrowserCategoryListener {
                    override fun onFinishSelectCategory(selectedCategory: CategoryListItem?): Boolean {
                        mViewModel.getAttributeList(selectedCategory?.id)
                        if (isSearchable(selectedCategory)) {
                            closeSearchMaskAndSearch()
                            updateUIStringValue(if (selectCategory != null) selectCategory!!.name else "")
                        } else {
                            activity.showSelectCriteriaMessage()
                        }
                        return true
                    }

                    override fun onBrowserCategory(selectedCategory: CategoryListItem?): Boolean {
                        initByChildMostCategory(selectedCategory)
                        return true
                    }
                }
            }
            return _onBrowserCategoryListener as BaseCategoryListFragment.OnBrowserCategoryListener
        }

    override fun initByChildMostCategory(category: CategoryListItem?) {
        selectCategory = category
    }

    fun closeSearchMaskAndSearch() {
        if (selectCategory != null
        ) {
            processSearch()
            activity.pop(selectorFragment)
        } else {
            activity.showSelectCriteriaMessage()
        }
    }

    fun processSearch() {
        closeFilterAndExecuteSearch()
    }

    val activity: ListResultActivity
        get() = context as ListResultActivity

    private fun closeFilterAndExecuteSearch() {
        ListResultActivity._isSaveSearchExecuted = false
        if (activity != null) {
            val fragment: ListResultFragment = activity
                .getSupportFragmentManager()
                .findFragmentByTag(ListResultFragment::class.java.getSimpleName()) as ListResultFragment
            if (fragment != null) {
            }
        }
    }

    override fun onValueClick() {
        notifyResetFragmentSearchCount()
        showAttributeFragmentDialog(newSelectorFragment)
        Handler(Looper.getMainLooper()).post {
            selectorFragment.onBrowserCategoryBase(selectCategory?.id, false, false)
        }
    }

    fun notifyResetFragmentSearchCount() { }

    override fun updateUIStringValue(newValue: String?) {
        super.updateUIStringValue(newValue)
        updateVisibilityX()
        val resolveLengthByHint = !TextUtils.isEmpty(_value?.getText())
        _value?.setHint(
            if (resolveLengthByHint) ""
            else mViewModel.translate("apps.home.new.inallcategories"))
    }

    override fun updateVisibilityX() {
        val temp = _x.visibility
        val visible = !TextUtils.isEmpty(_value?.getText().toString())
        _x.setVisibility(if (visible) VISIBLE else GONE)
        if (temp != _x.visibility) _mainChip.setBackgroundResource(
            if (visible) R.drawable.shape_chip_drawable_left
            else R.drawable.shape_chip_drawable)
    }

    override fun onXClick() {
        initByChildMostCategory(null)
        processSearch()
    }

    lateinit var _mainChip: RelativeLayout
    override fun initViews(context: Context, attrs: AttributeSet?, defStyle: Int) {
        super.initViews(context, attrs, defStyle)
        _mainChip = findViewById<View>(R.id.mainChip) as RelativeLayout
        _value?.setHint(mViewModel.translate("apps.home.new.inallcategories"))
    }

    companion object {
        fun isSearchable(selectedCategory: CategoryListItem?): Boolean {
            return selectedCategory != null
        }
    }
}
