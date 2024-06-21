package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnFocusChangeListener
import android.view.ViewStub
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.controller.BaseDialogFragment
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.util.AnibisUtils.closeKeyboard
import com.haidev.kanibis.shared.util.AnibisUtils.notifySetContentDescription
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

abstract class ABaseAttributeItemView : ABaseGroupableAttView {

    private var _text_input_layout: CustomTextLayout? = null
	protected var _value: TextView? = null
    private var _tvLabel: TextView? = null
    private var _vsLegend: ViewStub? = null
    private var _tvLegend: TextView? = null
	protected lateinit var _x: ImageView
    private lateinit var _focusable: View
    private var _edtitableListener: OnClickListener? = null
    private var _readonlyListener: OnClickListener? = null
    protected var _main: LinearLayout? = null
    var factory: CategoryAttributeViewFactory? = null
        private set
	var _error = ""
    var _isAbsoluteInput = false
    private var _isStaticFocus = false
    private var _label: String? = null
    var hint: String? = null
        private set
    private val _isDirectUpdate = true
    private val _isMustFillError = false
    private var _currentError: String? = null

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context) {
        mViewModel = viewModel
        initParentView(context, null, -1)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        factory: CategoryAttributeViewFactory?,
        viewModel: ISearchListViewModel
    ) : super(context, attrs) {
        this.factory = factory
        mViewModel = viewModel
        initParentView(context, attrs, -1)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initParentView(context, attrs, -1)
    }

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initParentView(context, attrs, defStyle)
    }

	val isSearchLayout: Boolean = true
    protected open fun initParentView(context: Context, attrs: AttributeSet?, defStyle: Int) {
        this.orientation = HORIZONTAL
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, 0)
        setLayoutParams(params)
        val vi = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = vi.inflate(
            if (isSearchLayout) (
                    if (isDirectInput)
                R.layout.attribute_item_search_direct_input else
                    R.layout.attribute_item_search) else
                        R.layout.attribute_item_insertion,
            null
        )
        this.addView(v)
        initViews(context, attrs, defStyle)
    }

    protected open fun initViews(context: Context, attrs: AttributeSet?, defStyle: Int) {
        _text_input_layout = findViewById(R.id.text_input_layout)
        _value = findViewById(R.id.value)
        _tvLabel = findViewById(R.id.label)
        _x = findViewById(R.id.x)
        _main = findViewById(R.id.main)
        _focusable = findViewById(R.id.focusable)
        _vsLegend = findViewById(R.id.vsLegend)
        _errorView = findViewById<View>(R.id.error) as TextView?
        if (isSearchLayout) _value?.setHint(mViewModel.translate(TextId.APPS_ALL))
        _x.setOnClickListener {
            defaultXClick()
            onXClick()
        }
        if (isSearchLayout) {
            _main!!.setOnClickListener { _value?.performClick() }
        }
        if (!isDirectInput) {
            _edtitableListener = OnClickListener {
                onValueClick()
                notifyDelayCloseKeyboard()
            }
            _readonlyListener = OnClickListener { }
            _value?.setOnClickListener(_edtitableListener)
            _value?.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus && !_isStaticFocus) {
                    _value?.performClick()
                }
            }
            _value?.setKeyListener(null)
            _value?.setCursorVisible(false)
            if (this is RightDrawableProvider) {
                _value?.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0,
                    (this as RightDrawableProvider).rightDrawableResId, 0
                )
            }
        } else {
            _value?.setCursorVisible(true)
            _value?.setCompoundDrawables(null, null, null, null)
        }
        if (attrs != null) {
            try {
                val set = intArrayOf(android.R.attr.contentDescription)
                val a = context.obtainStyledAttributes(attrs, set)
                if (a != null) {
                    if (a.getText(0) != null) _label = mViewModel.translate(a.getText(0).toString())
                    a.recycle()
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun notifyDelayCloseKeyboard() {
        if (!inputByKeyboard()) {
            Handler(Looper.getMainLooper())
                .postDelayed({ closeKeyboard((context as Activity)) }, 500)
        }
    }

    private fun inputByKeyboard(): Boolean {
        return isDirectInput || isIndirectInput
    }

    private val isIndirectInput: Boolean
        get() = this is XBAttRangeView

    private val isDirectInput: Boolean
        get() = this is ABaseAttInputView

    private fun removeFocus() {
        _focusable.requestFocus()
        _focusable.requestFocusFromTouch()
    }

    fun forceFocus() {
        _isStaticFocus = true
        _value?.requestFocus()
        _value?.requestFocusFromTouch()
        _isStaticFocus = false
    }

    open fun updateUIStringValue(newValue: String?) {
        var newValue = newValue
        if (!isDirectInput || SearchMaskViewHolder._isRestorePhase || _isAbsoluteInput) {
            if (TextUtils.isEmpty(newValue)) {
                newValue = ""
                removeFocus()
            }
            _value?.text = newValue
            _isAbsoluteInput = false
        }
        notifyUpdateHint()
    }

    private fun notifyUpdateHint() {
        if (TextUtils.isEmpty(uIValue) && !TextUtils.isEmpty(hint)) {
            if (_text_input_layout != null) _text_input_layout!!.setHint(hint) else if (_tvLabel != null) _tvLabel!!.text =
                hint
        } else {
            if (_text_input_layout != null) _text_input_layout!!.setHint(_label) else if (_tvLabel != null) _tvLabel!!.text =
                _label
        }
    }

    open fun updateVisibilityX() {
        val visible = !TextUtils.isEmpty(_value?.getText().toString())
        _x!!.setVisibility(if (visible) VISIBLE else GONE)
    }

    abstract fun onMainValueChanged(params: XBSearchParameters)

    open val value: String?
        get() = uIValue
    val uIValue: String
        get() = _value?.getText().toString()
    open val isHaveData: Boolean
        get() = !TextUtils.isEmpty(value)

    fun onValueChanged(params: XBSearchParameters) {
        if (SearchMaskViewHolder._isRestorePhase) {
            if (_attribute != null && factory != null && _childView != null) {
                factory!!.handleDependingDefinedByApp(this, _childView!!, _duoView!!)
            }
        } else {
            onMainValueChanged(params)
            if (_attribute != null && factory != null) {
                if (_attribute!!.children != null && _attribute!!.children!!.size > 0) {
                    factory!!.handleDepending()
                }
                if (factory != null && _childView != null) {
                    factory!!.handleDependingDefinedByApp(this, _childView!!, _duoView!!)
                }
            }
        }
        notifyUpdateSearchCount()
    }

    fun setHint(s: String) {
        hint = s
        if (TextUtils.isEmpty(value)) {
            if (_text_input_layout != null) _text_input_layout!!.setHint(hint) else if (_tvLabel != null) _tvLabel!!.text =
                hint
        }
    }

    fun getLabel(): String {
        return _label!!
    }

    fun setLabel(s: String?) {
        if (_text_input_layout != null) _text_input_layout!!.hint =
            s else if (_tvLabel != null) _tvLabel!!.text =
            s
        _label = s
        if (this !is ABaseCategoryView) {
            notifySetContentDescription(_value, "" + if (_attribute != null) _attribute!!.id else s)
        }
    }

    abstract fun onValueClick()
    abstract fun onXClick()
    fun defaultXClick() {
        updateUIStringValue("")
    }

    fun setMainVisible(b: Boolean) {
        _main!!.visibility = if (b) VISIBLE else GONE
    }

    protected open val params: XBSearchParameters
        protected get() = mViewModel._searchMaskParams

    val isSearchMask: Boolean
        get() = true
    open val isEmpty: Boolean
        get() = TextUtils.isEmpty(_value?.getText().toString())

    fun dismissAutoFocus() {
        _focusable.requestFocus()
    }

    fun setEditable(editable: Boolean) {
        _value?.isFocusable = editable
        _value?.setFocusableInTouchMode(editable)
        _value?.setOnClickListener(if (editable) _edtitableListener else _readonlyListener)
        if (isSearchLayout) {
            if (_tvLabel != null) _tvLabel!!.setTextColor(resources.getColor(if (editable) R.color.black else R.color.light_gray))
        } else {
            if (!(this is ABaseAttInputView) ){
                _value?.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    if (editable) R.drawable.ic_attribute_expand else R.drawable.ic_attribute_expand_gray_out,
                    0
                )
            }
            _value?.setTextColor(resources.getColor(if (editable) R.color.black else R.color.light_gray))
        }
        if (!editable) onXClick()
    }

    override val main = _main

    override val valueView = _value

    interface RightDrawableProvider {
        val rightDrawableResId: Int
    }

    interface ErrorListProvider {
    }

    val isFragmentShown: Boolean
        get() = context is ListResultActivity

    fun showAttributeFragmentDialog(f: Fragment?) {
        if (isFragmentShown && f is BaseDialogFragment<*>) {
            (context as ListResultActivity).showFullScreenDialog((f as BaseDialogFragment<*>?)!!)
        }
    }

    val legendView: TextView?
        get() {
            if (_vsLegend != null) {
                if (_tvLegend == null) _tvLegend = _vsLegend!!.inflate() as TextView
                return _tvLegend
            }
            return null
        }

    companion object {
        fun resolveFormat(d: Double, attribute: AttributeListItem): String {
            return if (isHaveNoFormat(attribute) || isHaveOneDecimalFormat(
                    attribute
                )
            ) {
                Log.v(
                    "resolveFormat", "resolveFormat: formatNumber1: " + " | d. = " +
                            d + " | attribute. = " + attribute.name + " | attribute.id = " + attribute.id
                )
                formatAttributeDecimal(d, attribute)
            } else {
                Log.v(
                    "resolveFormat", "resolveFormat: formatNumber2: " + " | d. = " +
                            d + " | attribute. = " + attribute.name
                )
                AnibisUtils.formatNumber(d)
            }
        }

        val _attIDHaveNoFormat = intArrayOf(4, 108)
        val _attIDOneDecimalFormat = intArrayOf(28)

		fun formatAttributeDecimal(d: Double?, attribute: AttributeListItem?): String {
            if (d == null || attribute == null) return ""
            Log.v("room", "0102 attribute.id = " + attribute.id)
            var formatter: DecimalFormat? = null
            val dfs = DecimalFormatSymbols()
            dfs.setDecimalSeparator('.')
            dfs.setGroupingSeparator('\'')
            formatter = if (isHaveNoFormat(attribute)) {
                DecimalFormat("#", dfs)
            } else if (isHaveOneDecimalFormat(attribute)) {
                DecimalFormat("#,###.0", dfs)
            } else if (attribute.type == AttributeListItem.XBAttributeType.InputInt.ordinal) {
                DecimalFormat("#,###", dfs)
            } else if (attribute.type == AttributeListItem.XBAttributeType.InputDecimal.ordinal) {
                DecimalFormat("#,###.00", dfs)
            } else if (attribute.type == AttributeListItem.XBAttributeType.InputDate.ordinal) {
                DecimalFormat("dd-MM-yyyy", dfs)
            } else {
                DecimalFormat("#", dfs)
            }
            return formatter.format(d)
        }

        protected fun isHaveNoFormat(attribute: AttributeListItem): Boolean {
            for (i in _attIDHaveNoFormat) {
                if (attribute.id == i) return true
            }
            return false
        }

        protected fun isHaveOneDecimalFormat(attribute: AttributeListItem): Boolean {
            for (i in _attIDOneDecimalFormat) {
                if (attribute.id == i) return true
            }
            return false
        }
    }
}