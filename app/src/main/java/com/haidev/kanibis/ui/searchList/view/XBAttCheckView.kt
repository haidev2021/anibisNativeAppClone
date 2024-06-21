package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class XBAttCheckView : ABaseGroupableAttView {
    private var _attributeEntry: AttributeEntryListItem? = null
    private var _selectedAttributeEntry: AttributeEntryListItem? = null
    var isCommonAllLanguage = false
    lateinit var _value: SwitchCompat
    lateinit var _label: TextView
    var factory: CategoryAttributeViewFactory? = null
        private set
    private var _touch: LinearLayout? = null
    val isSearchLayout: Boolean =true

    constructor(context: Context) : super(context)

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context, label: String?) : super(context, null) {
        val v = getInflateView(context)
        this.addView(v)
        setLabel(label)
    }

    constructor(
        context: Context, label: String?,
        factory: CategoryAttributeViewFactory?
    ) : super(context, null) {
        this.factory = factory
        val v = getInflateView(context)
        this.addView(v)
        setLabel(label)
    }

    constructor(
        context: Context,
        attribute: AttributeListItem,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context, null) {
        mViewModel = viewModel
        _attribute = attribute
        this.factory = factory
        _attributeEntry = attribute.entries?.get(0)
        val v = getInflateView(context)
        if (attribute != null) setLabel(attribute.name)
        this.addView(v)
        this.tag = attribute
    }

    constructor(
        context: Context, attributeEntry: AttributeEntryListItem,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context, null) {
        mViewModel = viewModel
        _attribute = attributeEntry.attribute
        this.factory = factory
        _attributeEntry = attributeEntry
        val v = getInflateView(context)
        setLabel(attributeEntry.name)
        this.addView(v)
        this.tag = _attribute
    }

    private fun getInflateView(context: Context): View {
        this.orientation = VERTICAL
        val lParams = LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        lParams.setMargins(0, 0, 0, 0)
        setLayoutParams(lParams)
        val vi = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v: View = vi.inflate(
            if (isSearchLayout) R.layout.item_att_check_search else R.layout.item_att_check,
            null
        )
        _value = v.findViewById<View>(R.id.check) as SwitchCompat
        _label = v.findViewById<View>(R.id.label) as TextView
        _value.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(arg0: CompoundButton, arg1: Boolean) {
                onValueChanged(mViewModel._searchMaskParams)
                if (!isSearchLayout) _label!!.setTextColor(
                    resources.getColor(if (arg1) R.color.black else R.color.light_gray))
            }
        })
        _touch = v.findViewById<View>(R.id.touch) as LinearLayout
        _touch!!.setOnClickListener { _value.performClick() }
        return v
    }

    fun updateValue(checked: Boolean) {
        _value.setChecked(checked)
        onValueChanged(mViewModel._searchMaskParams)
    }

    fun setLabel(s: String?) {
        _label!!.text = s
        AnibisUtils.notifySetContentDescription(
            _touch,
            "" + if (_attribute != null) _attribute!!.id else s
        )
    }

    val value: Boolean
        get() = _value.isChecked()

    private fun onMainValueChanged(params: XBSearchParameters) {
        if (_attributeEntry != null) {
            _selectedAttributeEntry = if (value) {
                params.attributeFilter.addAttributeEntry(_attributeEntry!!)
                _attributeEntry
            } else {
                params.attributeFilter.removeAttributeEntry(_attributeEntry)
                null
            }
        } else {
            if (isCommonAllLanguage) {
                params.useLanguageFilter = value
            }
        }
    }

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
            notifyUpdateSearchCount()
        }
    }

    fun checkUpdatingChild() {
        if (_attribute!!.children != null && _attribute!!.children!!.size > 0) factory!!.updateChildGrayOut(
            this.tag as LinearLayout,
            value
        )
    }
    val selectedItem: AttributeEntryListItem?
        get() = _selectedAttributeEntry
    override val main: View?
        get() = _touch
    override val valueView: TextView?
        get() = null
}
