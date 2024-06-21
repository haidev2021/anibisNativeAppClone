package com.haidev.kanibis.shared.viewmodels

import android.view.View
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable

interface IBaseViewModel {
    var currentLanguageIsoCode: String
    fun translate(textKey: String?): String?
    fun translateView(view: View?)
    fun onAttach()
    fun onDetach()
    fun onPause()
    fun onResume()
    fun onPostResume()
    fun onPageView()
    fun onAlertDialog(): Observable<IAlertDialogViewModel?>?
    fun onToastMessage(): Observable<IToastMessageViewModel?>?
    fun onProgressDialog(): Observable<IProgressDialogViewModel?>?
    fun onSnackbar(): Observable<ISnackbarViewModel?>?
    fun onAction(): Observable<Int?>?

    companion object {
        const val ACTION_FINISH = 1
        const val ACTION_EXIT_APP = ACTION_FINISH + 1
        const val ACTION_RESTART_APPLICATION = ACTION_EXIT_APP + 1
        const val ACTION_OPEN_GOOGLE_PLAY = ACTION_RESTART_APPLICATION + 1
    }
}