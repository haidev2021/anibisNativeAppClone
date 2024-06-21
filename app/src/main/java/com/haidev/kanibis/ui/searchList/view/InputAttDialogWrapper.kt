package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel

class InputAttDialogWrapper(activity: Activity?) : BaseDialogWrapper(activity) {
    lateinit var mViewModel: IBaseViewModel
    private lateinit var _value: XBEditText
    private lateinit var _inputView: ABaseAttInputView
    private var _FinishInputInterface: FinishInputInterface? = null
    var _historyValue: String? = null
    fun onCreateViewCustom(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.dialog_att_input, container, false)
        _value = view.findViewById<View>(R.id.value) as XBEditText
        val label = view.findViewById<View>(R.id.label) as TextView
        label.text = _inputView.getLabel()
        if (_historyValue != null) _value!!.setText(_historyValue) else _value!!.setText(
            _inputView.valueView?.getText().toString()
        )
        _value!!.setAttribute(_inputView.getAttribute()!!)
        mViewModel.translateView(view)
        return view
    }

    fun setFinishInputInterface(_FinishInputInterface: FinishInputInterface?) {
        this._FinishInputInterface = _FinishInputInterface
    }

    val text: String
        get() = _value.getText().toString()

    override fun createDialog(): AlertDialog {
        _title = _inputView.getLabel()
        val dialog: AlertDialog = AlertDialog.Builder(activity)
            .setTitle(if (_title != null) _title else "")
            .setPositiveButton(
                mViewModel.translate(
                    TextId.APPS_ACTION_DONE
                )
            ) { dialog, whichButton ->
                if (_FinishInputInterface != null) _FinishInputInterface!!.onFinishInput()
                hideKeyboard()
            }
            .setNegativeButton(
                mViewModel.translate(
                    TextId.APPS_ACTION_CANCEL
                )
            ) { dialog, whichButton -> hideKeyboard() }.create()
        val inflater = activity.layoutInflater
        dialog.setView(onCreateViewCustom(inflater, null, null))
        setDefaultSoftInputMode(dialog)
        return dialog
    }

    interface FinishInputInterface {
        fun onFinishInput()
    }

    private fun hideKeyboard() {
        val imm = activity
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(_value.windowToken, 0)
    }

    fun setInputView(xbInputView: ABaseAttInputView) {
        _inputView = xbInputView
    }
}
