package com.haidev.kanibis.shared.viewmodels

import com.haidev.kanibis.shared.viewmodels.IAlertDialogViewModel.IButtonClickListener
import com.haidev.kanibis.shared.viewmodels.IAlertDialogViewModel.IShowListener
import android.text.TextUtils

class AlertDialogViewModel : IAlertDialogViewModel {
    override var title: String? = null
        private set
    override var message: String? = null
        private set
    override var negativeButtonText: String? = null
        private set
    override var positiveButtonText: String? = null
        private set
    private var mNegativeButtonListener: IButtonClickListener? = null
    private var mPositiveButtonListener: IButtonClickListener? = null
    override var isCancelable = false
        private set
    override var isShowing = false
        private set
    private var mShowListener: IShowListener? = null
    override fun onPositiveButtonClick() {
        if (mPositiveButtonListener != null) mPositiveButtonListener!!.onClick()
    }

    override fun onNegativeButtonClick() {
        if (mNegativeButtonListener != null) mNegativeButtonListener!!.onClick()
    }

    override fun onDismiss() {
        isShowing = false
    }

    override fun onShow() {
        isShowing = true
        if (mShowListener != null) mShowListener!!.onShow()
    }

    class Builder {
        private var mTitle: String? = null
        private var mMessage: String? = null
        private var mNegativeButton: String? = null
        private var mPositiveButton: String? = null
        private var mNegativeButtonListener: IButtonClickListener? = null
        private var mPositiveButtonListener: IButtonClickListener? = null
        private var mCancelable = true
        private var mShowListener: IShowListener? = null
        fun title(title: String?): Builder {
            mTitle = title
            return this
        }

        fun message(message: String?): Builder {
            mMessage = message
            return this
        }

        fun negativeButton(name: String?, listener: IButtonClickListener?): Builder {
            if (!TextUtils.isEmpty(name)) {
                mNegativeButton = name
                mNegativeButtonListener = listener
            }
            return this
        }

        fun positiveButton(name: String?, listener: IButtonClickListener?): Builder {
            if (!TextUtils.isEmpty(name)) {
                mPositiveButton = name
                mPositiveButtonListener = listener
            }
            return this
        }

        fun cancelable(isCancelable: Boolean): Builder {
            mCancelable = isCancelable
            return this
        }

        fun onShowListener(listener: IShowListener?): Builder {
            mShowListener = listener
            return this
        }

        fun create(): AlertDialogViewModel {
            val viewModel = AlertDialogViewModel()
            viewModel.title = mTitle
            viewModel.message = mMessage
            viewModel.negativeButtonText = mNegativeButton
            viewModel.positiveButtonText = mPositiveButton
            viewModel.mPositiveButtonListener = mPositiveButtonListener
            viewModel.mNegativeButtonListener = mNegativeButtonListener
            viewModel.isCancelable = mCancelable
            viewModel.mShowListener = mShowListener
            return viewModel
        }
    }
}