package com.haidev.kanibis.ui.categoryList.controllers

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.category.view.CategoryListAdapter
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity


class HomeCategoryListFragment : BaseCategoryListFragment<ICategoryListViewModel>() {

    override fun getAdapter(btnShowWithCount: Button?): CategoryListAdapter {
        return CategoryListAdapter(
            requireActivity(), _clickListener, btnShowWithCount, false, _lv, false, mViewModel
        )
    }

    override val layoutId: Int
        get() = R.layout.fragment_category_browser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBrowsingListener(onBrowserCategoryListener)
    }

    override fun createViewModel() {
        (requireActivity() as HomeCategoryListActivity).getHomeCategoryListComponent().inject(this)
    }

    override fun initCategoryViews() {
        super.initCategoryViews()
        _ivBack.setOnClickListener {
        }
    }

    override fun initFragmentContent(root: View?, btnShowWithCount: Button?) {
        super.initFragmentContent(root, btnShowWithCount)
        var id = activity?.getIntent()?.getExtras()?.getInt("CATEGORY_ID");
        if(id == 0) id = null
        onBrowserCategoryBase(id, true, false)
    }

    private var _onBrowserCategoryListener: OnBrowserCategoryListener? = null
    private val onBrowserCategoryListener: OnBrowserCategoryListener
        get() {
            if (_onBrowserCategoryListener == null) {
                _onBrowserCategoryListener = object : OnBrowserCategoryListener {
                    override fun onFinishSelectCategory(selectedCategory: CategoryListItem?): Boolean {
                        if (selectedCategory != null) {
                            if (getActivity() != null) {
                                val myIntent = Intent(requireActivity(), ListResultActivity::class.java)
                                myIntent.putExtra("fromType", ListResultActivity.FROM_HOME_CATEGORY)
                                requireActivity().startActivity(myIntent)

                            }
                        }
                        return true
                    }

                    override fun onBrowserCategory(selectedCategory: CategoryListItem?): Boolean {
                        return true
                    }
                }
            }
            return _onBrowserCategoryListener as OnBrowserCategoryListener
        }

    override fun customDismiss() {
    }
}
