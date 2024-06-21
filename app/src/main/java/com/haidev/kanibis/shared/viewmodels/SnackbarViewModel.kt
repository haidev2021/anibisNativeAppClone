package com.haidev.kanibis.shared.viewmodels
import android.text.TextUtils
import android.widget.Toast.LENGTH_SHORT

class SnackbarViewModel(override var message: String?) : ISnackbarViewModel {
    override var action: String? = null
        private set

    private var mActionListener: ISnackbarViewModel.IActionListener? = null
    private var mDismissListener: ISnackbarViewModel.IDismissListener? = null

    override var duration = 0
        private set

    override fun executeAction() {
        mActionListener?.onAction()
    }

    override fun dismiss() {
        mDismissListener?.onDismiss()
    }

    class Builder {
        private var mMessage: String? = null
        private var mActionTitle: String? = null
        private var mActionListener: ISnackbarViewModel.IActionListener? = null
        private var mDismissListener: ISnackbarViewModel.IDismissListener? = null
        private var mDuration: Int

        init {
            mDuration = LENGTH_SHORT
        }

        fun message(message: String?): Builder {
            mMessage = message
            return this
        }

        fun action(name: String?, listener: ISnackbarViewModel.IActionListener?): Builder {
            if (!TextUtils.isEmpty(name)) {
                mActionTitle = name
                mActionListener = listener
            }
            return this
        }

        fun onDismissListener(listener: ISnackbarViewModel.IDismissListener?): Builder {
            mDismissListener = listener
            return this
        }

        fun duration(duration: Int): Builder {
            mDuration = duration
            return this
        }

        fun create(): SnackbarViewModel {
            val viewModel = SnackbarViewModel(mMessage)
            viewModel.mActionListener = mActionListener
            viewModel.action = mActionTitle
            viewModel.message = mMessage
            viewModel.duration = mDuration
            viewModel.mDismissListener = mDismissListener
            return viewModel
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}