package com.haidev.kanibis.shared.viewmodels

interface IAlertDialogViewModel {
    val title: String?
    val message: String?
    val positiveButtonText: String?
    val negativeButtonText: String?
    val isCancelable: Boolean
    val isShowing: Boolean

    fun onPositiveButtonClick()
    fun onNegativeButtonClick()
    fun onDismiss()
    fun onShow()
    interface IButtonClickListener {
        fun onClick()
    }

    interface IShowListener {
        fun onShow()
    }
}