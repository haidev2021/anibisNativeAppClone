package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.controller.BaseActivity
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class RefineSearchCategoryView(context: Context, viewModel: ISearchListViewModel) : ABaseSearchCategoryView(context, viewModel) {
    var _fragment: SearchCategoryFragment? = null
    override val itemLayout: Int
        get() = R.layout.attribute_item_search
    override val selectorFragment: BaseCategoryListFragment<ISearchListViewModel>
        get() {
            if (_fragment == null) {
                _fragment = SearchCategoryFragment()
                _fragment!!.setAttributeItemView(this)
                _fragment!!.setBrowsingListener(onBrowserCategoryListener)
            }
            return _fragment!!
        }
    val newSelectorFragment: BaseCategoryListFragment<ISearchListViewModel>
        get() {
            _fragment = SearchCategoryFragment()
            _fragment!!.setAttributeItemView(this)
            _fragment!!.setBrowsingListener(onBrowserCategoryListener)
            return _fragment!!
        }
    var _onBrowserCategoryListener: BaseCategoryListFragment.OnBrowserCategoryListener? = null
    override val onBrowserCategoryListener: BaseCategoryListFragment.OnBrowserCategoryListener
        get() {
            if (_onBrowserCategoryListener == null) {
                _onBrowserCategoryListener = object : BaseCategoryListFragment.OnBrowserCategoryListener {
                    override fun onFinishSelectCategory(selectedCategory: CategoryListItem?): Boolean {
                        (mViewModel as ISearchListViewModel).getAttributeList(selectedCategory?.id)
                        if (context is ListResultActivity) {
                            val activity: ListResultActivity = context as ListResultActivity
                            activity.notifyRepopulateCategoryAttributes("onFinishSelectCategory")
                            activity.onBackPressedDispatcher.onBackPressed()
                        }
                        return true
                    }

                    override fun onBrowserCategory(selectedCategory: CategoryListItem?): Boolean {
                        initByChildMostCategory(selectedCategory)
                        (context as ListResultActivity).setRepopulateAttributeFlag(
                            true,
                            "onBrowserCategory"
                        )
                        return true
                    }
                }
            }
            return _onBrowserCategoryListener as BaseCategoryListFragment.OnBrowserCategoryListener
        }

    override fun onValueClick() {
        notifyResetFragmentSearchCount()
        showAttributeFragmentDialog(newSelectorFragment)
        //        }
        (Handler(Looper.getMainLooper())).post {
            selectorFragment.onBrowserCategoryBase(
                selectCategory?.id,
                false,
                false
            )
        }
    }

    fun notifyResetFragmentSearchCount() {
    }
}
