package com.haidev.kanibis.shared.viewmodels

import android.view.View
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.util.TextkeyUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

open class BaseViewModel(localizationService: ILocalizationService) : IBaseViewModel {

    private val mLocalizationService: ILocalizationService
    protected val mAlertDialog: PublishSubject<IAlertDialogViewModel>
    protected val mSnackbar: PublishSubject<ISnackbarViewModel>
    protected val mToastMessage: PublishSubject<IToastMessageViewModel>
    protected val mAction: PublishSubject<Int>
    private val mProgressDialog: PublishSubject<IProgressDialogViewModel>

    init {
        mLocalizationService = localizationService
        mAlertDialog = PublishSubject.create()
        mSnackbar = PublishSubject.create()
        mToastMessage = PublishSubject.create()
        mAction = PublishSubject.create()
        mProgressDialog = PublishSubject.create()
    }

    override fun translate(textKey: String?): String? {
        return mLocalizationService.translate(textKey)
    }

    override fun translateView(view: View?) {
        TextkeyUtils.translateView(view, mLocalizationService)
    }

    override var currentLanguageIsoCode = ""
        get() = mLocalizationService.currentIsoLanguage

    override fun onAttach() {}
    override fun onDetach() {
    }

    override fun onPause() {}
    override fun onResume() {}
    override fun onPostResume() {}
    override fun onPageView() {}

    override fun onAlertDialog(): Observable<IAlertDialogViewModel?>? {
        return mAlertDialog
    }

    override fun onToastMessage(): Observable<IToastMessageViewModel?>? {
        return mToastMessage
    }

    override fun onProgressDialog(): Observable<IProgressDialogViewModel?>? {
        return mProgressDialog
    }

    override fun onSnackbar(): Observable<ISnackbarViewModel?>? {
        return mSnackbar
    }

    override fun onAction(): Observable<Int?>? {
        return mAction
    }

    protected fun sendProgressMessage(message: String?) {
        mProgressDialog.onNext(
            ProgressDialogViewModel.builder().message(message).cancelable(false).create()
        )
    }
}
