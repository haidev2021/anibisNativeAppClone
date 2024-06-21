package com.haidev.kanibis.shared.viewmodels

interface ISnackbarViewModel {
    val message: String?
    val action: String?
    val duration: Int
    fun executeAction()
    fun dismiss()
    interface IActionListener {
        fun onAction()
    }

    interface IDismissListener {
        fun onDismiss()
    }

    companion object {
        const val LENGTH_SHORT = -1
        const val LENGTH_LONG = 0
    }
}