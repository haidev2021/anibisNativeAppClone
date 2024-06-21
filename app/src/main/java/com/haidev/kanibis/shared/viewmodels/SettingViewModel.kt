package com.haidev.kanibis.shared.viewmodels

import com.haidev.kanibis.shared.localization.service.ILocalizationService

open class SettingViewModel(localizationService: ILocalizationService) : BaseViewModel(localizationService) {
    private val mLocalizationService: ILocalizationService

    init {
        mLocalizationService = localizationService
    }
}
