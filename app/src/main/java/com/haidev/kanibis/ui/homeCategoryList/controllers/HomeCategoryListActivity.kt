package com.haidev.kanibis.ui.categoryList.controllers

import com.haidev.kanibis.App
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.controller.SingleFragmentActivity
import com.haidev.kanibis.ui.homeCategoryList.DaggerHomeCategoryListComponent
import com.haidev.kanibis.ui.homeCategoryList.HomeCategoryListComponent

class HomeCategoryListActivity : SingleFragmentActivity<ICategoryListViewModel>() {

    override val mContentFragment = HomeCategoryListFragment()
    override val mActionBarTitle = ""
    override val mLayoutResourceId = R.layout.activity_single_screen_base

    var mHomeCategoryListComponent: HomeCategoryListComponent? = null

    fun getHomeCategoryListComponent(): HomeCategoryListComponent {
        if (mHomeCategoryListComponent == null) {
            mHomeCategoryListComponent = DaggerHomeCategoryListComponent.builder()
                .appComponent((application as App).getAppComponent())
                .build()
        }
        return mHomeCategoryListComponent!!
    }

    override fun createViewModel() {
        getHomeCategoryListComponent().inject(this)
    }
}
