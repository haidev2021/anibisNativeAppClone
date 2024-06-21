package com.haidev.kanibis.ui.main.services

import com.haidev.kanibis.shared.services.ActivityScope
import com.haidev.kanibis.shared.services.AppComponent
import com.haidev.kanibis.ui.main.controllers.MainActivity
import com.haidev.kanibis.ui.main.viewmodels.DashboardHeaderViewModel
import com.haidev.kanibis.ui.main.viewmodels.IDashboardHeaderViewModel
import com.haidev.kanibis.ui.main.views.DashboardHeaderViewHolder
import dagger.Component

@ActivityScope @Component (dependencies = [AppComponent::class], modules = [ MainScreenModule::class])
public interface MainScreenComponent {
    fun inject(target: MainActivity)
    fun inject(target: DashboardHeaderViewHolder.DashboardHeaderFragment)
}