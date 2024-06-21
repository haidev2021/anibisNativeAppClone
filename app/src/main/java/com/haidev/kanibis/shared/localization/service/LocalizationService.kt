package com.haidev.kanibis.shared.localization.service

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.room.Room
import com.haidev.kanibis.shared.localization.model.TextKeys
import com.haidev.kanibis.shared.localization.model.TranslationDatabase
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class LocalizationService(val app: Application): ILocalizationService {
    private val ISO_STRINGS: List<String> = mutableListOf("de", "fr", "it")
    private val LANGUAGE_NAMES: List<String> = mutableListOf("Deutsch", "Français", "Italiano")
    var mDB: TranslationDatabase
    private var mCompleted: BehaviorSubject<Boolean>
    private lateinit var mAllTranslation: MutableMap<String, String>
    override lateinit var mDisposable: Disposable
    init{
        mDB = Room.databaseBuilder(app, TranslationDatabase::class.java, "Translation.db")
            .createFromAsset("textresources.sqlite")
            .build()
        mCompleted = BehaviorSubject.create()
        loadTranslation()
    }

    @SuppressLint("CheckResult")
    fun loadTranslation() {
        val textKeys = TextKeys.SET
        mCompleted.onNext(false)
        mDisposable = mDB.getTranslationDao().getTranslationList("de", textKeys)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ list ->
                mAllTranslation = mutableMapOf();
                for(translation in list) {
                    mAllTranslation[translation.textKey!!] = translation.text!!
                }
                mCompleted.onNext(true)
            }, {error -> Log.v("", "1204 loadTranslation error ${error}")})
    }

    override val supportedIsoLanguages: List<String> = ISO_STRINGS

    override val supportedLanguageName: List<String> = LANGUAGE_NAMES

    override var currentIsoLanguage: String = "de"

    override fun onInitComplete(): BehaviorSubject<Boolean> {
        return mCompleted
    }

    override fun translate(key: String?): String? {
        return mAllTranslation[key]
    }

}