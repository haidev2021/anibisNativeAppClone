package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import com.haidev.kanibis.R
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class ABaseAttInputView : ABaseAttributeItemView,
    ABaseAttributeItemView.RightDrawableProvider {

    private var _inputType = -1
    override var value: String? = null
        protected set
    private var _inputWatcher: TextWatcher? = null

    constructor(context: Context, label: String?, factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel) : super(
        context!!, null, factory, viewModel
    ) {
        setLabel(label)
        init()
    }

    open fun init() {
        _value?.setImeOptions(EditorInfo.IME_ACTION_DONE)
        _value?.addTextChangedListener(textWatcher)
        if (isSearchLayout) {
            main?.setOnClickListener {
                _value?.requestFocus()
                _value?.requestFocusFromTouch()
                val imm = context
                    .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(_value, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    val textWatcher: TextWatcher
        get() {
            if (_inputWatcher == null) _inputWatcher = object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    updateValue(s.toString())
                }

                override fun afterTextChanged(arg0: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }
            }
            return _inputWatcher as TextWatcher
        }
    var inputType: Int
        get() = _inputType
        set(type) {
            if (_attribute == null) {
                _inputType = type
                _value?.setInputType(_inputType)
            }
        }

    override fun onValueClick() {
        val dialogWrapper = InputAttDialogWrapper(
            this.context as Activity
        )
        dialogWrapper.setInputView(this@ABaseAttInputView)
        dialogWrapper
            .setFinishInputInterface(object : InputAttDialogWrapper.FinishInputInterface {
                override fun onFinishInput() {
                    updateValue(dialogWrapper.text)
                }
            })
        dialogWrapper.showDialog()
    }

    override fun onXClick() {
        _isAbsoluteInput = true
        updateValue(null)
        _isAbsoluteInput = false
    }

    override val rightDrawableResId: Int
        get() = R.drawable.ic_attribute_input_drawable_placeholder

    fun updateAbsoluteValue(newValue: String?) {
        _isAbsoluteInput = true
        updateValue(newValue)
        _isAbsoluteInput = false
    }

    fun updateValue(newValue: String?) {
        try {
            if (!TextUtils.isEmpty(value)
                && value != newValue
                || !TextUtils.isEmpty(newValue)
                && newValue != value
            ) {
                value = newValue
                onValueChanged(params)
                super.updateUIStringValue(value)
            }
        } catch (e: Exception) {
        }
    }

    fun checkUpdatingChild() {
        if (_attribute!!.children != null && _attribute!!.children!!.size > 0) factory!!.updateChildGrayOut(
            this.tag as LinearLayout,
            !TextUtils.isEmpty(value)
        )
    }

    fun setSingleLine(b: Boolean) {
        if (b) {
            _value?.setInputType(InputType.TYPE_CLASS_TEXT)
        }
    }
}
