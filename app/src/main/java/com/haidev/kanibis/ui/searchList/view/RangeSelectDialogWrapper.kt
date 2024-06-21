package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnShowListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.attribute.controller.BaseAttributeEditorFragment
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.XBNumericRange
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.view.ABaseAttributeItemView.Companion.formatAttributeDecimal
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class RangeSelectDialogWrapper(activity: Activity?, val mViewModel: IBaseViewModel):  BaseDialogWrapper(activity) {
    private lateinit var _from: XBEditText
    private lateinit var _to: XBEditText
    private lateinit var _title: String
    private var _finishInputRangeInterface: FinishInputRangeInterface? = null
    private lateinit var _xbRangeView: XBAttRangeView

    val contentAsFragment: Fragment
        get() {
            val f = ContentFragment()
            f.setAttributeItemView(_xbRangeView)
            f.setRangeSelectDialogWrapper(this)
            return f
        }

    class ContentFragment : BaseAttributeEditorFragment<ISearchListViewModel>() {
        private var _w: RangeSelectDialogWrapper? = null
        private lateinit var _from: XBEditText
        private lateinit var _to: XBEditText
        fun setRangeSelectDialogWrapper(w: RangeSelectDialogWrapper?) {
            _w = w
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            requireActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            )
        }

        override fun onDestroy() {
            super.onDestroy()
            requireActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            )
        }

        override fun createViewModel() {
            (requireActivity() as ListResultActivity).getListResultComponent().inject(this)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
             container: ViewGroup?,
             savedInstanceState: Bundle?
        ): View {
            val root: View =
                inflater.inflate(R.layout.fragment_att_range_att_input, container, false)
            super.initViews(root)
            _w!!.onCreateViewCustom(root)
            _from = root.findViewById<View>(R.id.value_from) as XBEditText
            _to = root.findViewById<View>(R.id.value_to) as XBEditText
            val positiveClick = View.OnClickListener {
                _w!!.processPositiveClick()
                Handler(Looper.getMainLooper()).postDelayed(
                    { requireActivity().onBackPressedDispatcher.onBackPressed() }, 100)
            }
            _ivBack.setOnClickListener(positiveClick)
            _tvOk?.setOnClickListener(positiveClick)
            return root
        }

        override fun onResume() {
            super.onResume()
            Handler(Looper.getMainLooper()).postDelayed({
                _from.requestFocus()
                _from.requestFocusFromTouch()
                AnibisUtils.showKeyboard(requireActivity(), _from)
            }, 300)
        }
    }

    fun onCreateViewCustom(view: View): View {
        _from = view.findViewById<View>(R.id.value_from) as XBEditText
        _to = view.findViewById<View>(R.id.value_to) as XBEditText
        _from.setImeOptions(EditorInfo.IME_ACTION_DONE)
        _to.setImeOptions(EditorInfo.IME_ACTION_DONE)
        (view.findViewById<View>(R.id.label_from) as TextView).setText(mViewModel.translate(TextId.APPS_FROM))
        (view.findViewById<View>(R.id.label_to) as TextView).setText(mViewModel.translate(TextId.APPS_TO))
        val fromLabel = view.findViewById<View>(R.id.label_from) as TextView
        val unit = _xbRangeView.getAttribute()!!.unit
        val labelText = (if (!TextUtils.isEmpty(unit)) unit + " " else "") + fromLabel.getText().toString()
        fromLabel.text = labelText
        _from.setAttribute(_xbRangeView.getAttribute()!!)
        _to.setAttribute(_xbRangeView.getAttribute()!!)
        _from.setText(if (_xbRangeView._from != null) _xbRangeView._from else "")
        _to.setText(if (_xbRangeView._to != null) _xbRangeView._to else "")
        mViewModel.translateView(view)
        return view
    }

    fun setFinishInputRangeInterface(_finishInputRangeInterface: FinishInputRangeInterface?) {
        this._finishInputRangeInterface = _finishInputRangeInterface
    }

    override fun createDialog(): AlertDialog {
        _title = _xbRangeView.getAttribute()!!.name
        val dialog: AlertDialog = AlertDialog.Builder(getActivity())
            .setTitle(if (_title != null) _title else "")
            .setPositiveButton(
                mViewModel.translate(TextId.APPS_ACTION_DONE),
                { dialog, whichButton -> })
            .setNegativeButton(
                mViewModel.translate(TextId.APPS_ACTION_CANCEL),
                { dialog, whichButton -> hideKeyboard() }).create()
        dialog.setView(
            onCreateViewCustom(
                getActivity().getLayoutInflater().inflate(
                    R.layout.dialog_range_att_input,
                    null, false
                )
            )
        )
        this.setDefaultSoftInputMode(dialog)
        overrideButtonClick(dialog)
        dialog.setOnShowListener(_onShowListener)
        return dialog
    }

    private val _onShowListener = OnShowListener { dialog -> overrideButtonClick(dialog) }
    private fun processPositiveClick() {
        var rangeFrom: Double? = null
        var rangeTo: Double? = null
        try {
            rangeFrom = AnibisUtils.safeDoubleFromString(_from.getText().toString())
        } catch (e: Exception) {
        }
        try {
            rangeTo = AnibisUtils.safeDoubleFromString(_to.getText().toString())
        } catch (e: Exception) {
        }
        if (validateInputs(rangeFrom, rangeTo)) {
            if (_finishInputRangeInterface != null) _finishInputRangeInterface!!.onFinishInputRange(
                rangeFrom, rangeTo
            )
            hideKeyboard()
            this@RangeSelectDialogWrapper.dismissDialog()
        } else {
            val numericRange: XBNumericRange = _xbRangeView.getAttribute()!!.getDefaultNumericRange()
            var mess: String? = mViewModel.translate("apps.message.wrongrange")
            mess = java.lang.String.format(
                mess, _xbRangeView.getAttribute()!!
                    .name, formatAttributeDecimal(
                        numericRange.minValue,
                        _xbRangeView.getAttribute()
                    ),
                formatAttributeDecimal(
                    numericRange.maxValue,
                    _xbRangeView.getAttribute()
                )
            )
            val toast = Toast.makeText(
                getActivity()
                    .getApplicationContext(), mess,
                Toast.LENGTH_LONG
            )
            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
            toast.show()
        }
    }

    private fun overrideButtonClick(d: DialogInterface) {
        val positiveButton: Button = (d as AlertDialog)
            .getButton(Dialog.BUTTON_POSITIVE)
        if (positiveButton != null) positiveButton.setOnClickListener { processPositiveClick() }
    }

    interface FinishInputRangeInterface {
        fun onFinishInputRange(rangeFrom: Double?, rangeTo: Double?)
    }

    fun setXBRangeView(xbRangeView: XBAttRangeView) {
        _xbRangeView = xbRangeView
    }

    private fun validateInputs(rangeFrom: Double?, rangeTo: Double?): Boolean {
        return true
    }

    private fun validateInputs2(rangeFrom: Double?, rangeTo: Double?): Boolean {
        if (_xbRangeView.getAttribute()!!.isNumericInputType()) {
            val numericRange: XBNumericRange = _xbRangeView.getAttribute()!!.getDefaultNumericRange()

            return if (numericRange != null) {
                if (rangeFrom != null
                    && (rangeFrom < numericRange.minValue || rangeFrom > numericRange.maxValue)
                ) return false
                if (rangeTo != null
                    && (rangeTo < numericRange.minValue || rangeTo > numericRange.maxValue)
                ) false else true
            } else false
        }
        return false
    }

    private fun hideKeyboard() {
        val imm = getActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(_from.getWindowToken(), 0)
        imm.hideSoftInputFromWindow(_to.getWindowToken(), 0)
    }
}
