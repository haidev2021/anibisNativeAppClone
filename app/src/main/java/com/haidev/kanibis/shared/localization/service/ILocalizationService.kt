package com.haidev.kanibis.shared.localization.service

import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

interface ILocalizationService {
    var mDisposable: Disposable //for debugging

    val supportedIsoLanguages: List<String>

    val supportedLanguageName: List<String>

    var currentIsoLanguage: String

    fun onInitComplete(): BehaviorSubject<Boolean>

    fun translate(key: String?): String?

}