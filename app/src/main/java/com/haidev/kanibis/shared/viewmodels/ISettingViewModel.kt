package com.haidev.kanibis.shared.viewmodels

interface ISettingViewModel : IBaseViewModel {
    val currentIsoLanguage: String
    val supportedLanguages: Array<String>
    val textOfChangeLanguageTitle: String?
}