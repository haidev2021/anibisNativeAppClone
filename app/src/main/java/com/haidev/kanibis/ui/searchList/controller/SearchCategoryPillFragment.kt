package com.haidev.kanibis.ui.searchList.controller

import android.view.View
import android.widget.Button
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.view.CategoryListAdapter
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class SearchCategoryPillFragment : BaseCategoryListFragment<ISearchListViewModel>() {
    override fun  getAdapter(
        btnShowWithCount: Button?
    ): CategoryListAdapter {
        return CategoryListAdapter(
            requireActivity(), _clickListener, btnShowWithCount, false, _lv, false, mViewModel
        )
    }

    override val layoutId: Int
        get() = R.layout.fragment_category_pill

    override fun initCategoryViews() {
        super.initCategoryViews()
        _llHeader.visibility = View.VISIBLE
        _llCategoryHeader!!.visibility = View.GONE
        _ivBack.setOnClickListener { if (activity != null) requireActivity().onBackPressedDispatcher.onBackPressed() }
        _btnShowWithCount!!.setOnClickListener { _browsingListener!!.onFinishSelectCategory(_adapter._selectedCat) }
    }

    override fun createViewModel() {
        (requireActivity() as ListResultActivity).getListResultComponent().inject(this)
    }
}
