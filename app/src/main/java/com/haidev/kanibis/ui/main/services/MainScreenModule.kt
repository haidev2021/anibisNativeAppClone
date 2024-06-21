package com.haidev.kanibis.ui.main.services

import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.ui.main.viewmodels.DashboardHeaderViewModel
import com.haidev.kanibis.ui.main.viewmodels.IDashboardHeaderViewModel
import dagger.Module
import dagger.Provides

@Module class MainScreenModule {
    @Provides fun providesDashboardHeaderViewModel(localizationService: ILocalizationService): IDashboardHeaderViewModel {
        return DashboardHeaderViewModel(localizationService);
    }
}