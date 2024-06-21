package com.haidev.kanibis.shared.viewmodels

class ToastMessageViewModel private constructor(override val message: String?) :
    IToastMessageViewModel {
    override var duration = IToastMessageViewModel.LENGTH_SHORT
        private set

    class Builder {
        private var mMessage: String? = null
        private var mDuration = 0
        fun message(message: String?): Builder {
            mMessage = message
            return this
        }

        fun duration(@IToastMessageViewModel.Duration duration: Int): Builder {
            mDuration = duration
            return this
        }

        fun create(): ToastMessageViewModel {
            val viewModel = ToastMessageViewModel(mMessage)
            viewModel.duration = mDuration
            return viewModel
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}