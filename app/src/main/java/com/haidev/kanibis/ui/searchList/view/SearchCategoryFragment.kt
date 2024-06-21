package com.haidev.kanibis.ui.searchList.view

import android.view.View
import android.widget.Button
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.controller.BaseCategoryListFragment
import com.haidev.kanibis.shared.category.view.CategoryListAdapter
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class SearchCategoryFragment : BaseCategoryListFragment<ISearchListViewModel>() {

    override val layoutId: Int
        get() = R.layout.fragment_category_filter

    override fun initCategoryViews() {
        super.initCategoryViews()
        _btnShowWithCount?.setOnClickListener {
            _browsingListener?.onFinishSelectCategory(
                _adapter._selectedCat
            )
        }
    }

    override fun getAdapter(btnShowWithCount: Button?): CategoryListAdapter {
        return CategoryListAdapter(requireActivity(), _clickListener,
            btnShowWithCount, false, _lv, false, mViewModel)
    }

    override fun createViewModel() {
        (requireActivity() as ListResultActivity).getListResultComponent().inject(this)
    }
}
