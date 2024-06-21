package com.haidev.kanibis.shared.viewmodels

import com.haidev.kanibis.shared.viewmodels.ProgressDialogViewModel

class ProgressDialogViewModel private constructor(
    override val message: String?,
    override val isCancelable: Boolean
) : IProgressDialogViewModel {

    class Builder {
        private var mMessage: String? = null
        private var mCancelable: Boolean? = null
        fun message(message: String?): Builder {
            mMessage = message
            return this
        }

        fun cancelable(isCancelable: Boolean): Builder {
            mCancelable = isCancelable
            return this
        }

        fun create(): ProgressDialogViewModel {
            return ProgressDialogViewModel(mMessage, mCancelable!!)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}