package com.haidev.kanibis.ui.homeCategoryList

import com.haidev.kanibis.shared.parameter.service.IParamService
import com.haidev.kanibis.shared.services.ActivityScope
import com.haidev.kanibis.shared.services.AppComponent
import com.haidev.kanibis.ui.categoryList.controllers.HomeCategoryListActivity
import com.haidev.kanibis.ui.categoryList.controllers.HomeCategoryListFragment
import dagger.Component
import retrofit2.Retrofit

@ActivityScope @Component (dependencies = [AppComponent::class], modules = [ HomeCategoryListModule::class])
public interface HomeCategoryListComponent: AppComponent {
    fun inject(target: HomeCategoryListActivity)
    fun inject(target: HomeCategoryListFragment)
    fun paramService(): IParamService
}