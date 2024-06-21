package com.haidev.kanibis.ui.searchList

import com.haidev.kanibis.shared.services.SubActivityScope
import com.haidev.kanibis.ui.homeCategoryList.HomeCategoryListComponent
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.controller.ListResultFragment
import com.haidev.kanibis.ui.searchList.controller.RefineSearchFragment
import com.haidev.kanibis.ui.searchList.controller.SearchCategoryPillFragment
import com.haidev.kanibis.ui.searchList.view.RangeSelectDialogWrapper
import com.haidev.kanibis.ui.searchList.view.SearchCategoryFragment
import com.haidev.kanibis.ui.searchList.view.XBAttSelectView
import com.haidev.kanibis.ui.searchList.view.XBCommonAttSelectView
import dagger.Component

@SubActivityScope
@Component (dependencies = [HomeCategoryListComponent::class], modules = [ SearchResultListModule::class])
interface SearchResultListComponent {
    fun inject(target: ListResultActivity)
    fun inject(target: ListResultFragment)
    fun inject(target: RefineSearchFragment)
    fun inject(target: SearchCategoryPillFragment)
    fun inject(target: SearchCategoryFragment)
    fun inject(target: XBCommonAttSelectView.ContentFragment)
    fun inject(target: XBAttSelectView.ContentFragment)
    fun inject(target: RangeSelectDialogWrapper.ContentFragment)
}