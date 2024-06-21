package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel


abstract class ABaseGroupableAttView : LinearLayout {
    enum class LayoutType {
        Single,
        DuoLeft,
        DuoRight
    }

    lateinit var mViewModel: ISearchListViewModel
    var _attribute: AttributeListItem? =null
    private var _edge_magrin = -1
    var _errorView: TextView? = null
    var _childView: ABaseGroupableAttView? = null
    var _duoView: View? = null

    abstract val main: View?
    abstract val valueView: TextView?

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @SuppressLint("NewApi")
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        _edge_magrin = context.resources.getDimension(R.dimen.edge_margin).toInt()
    }

    fun getAttribute(): AttributeListItem? {
        return _attribute
    }

    fun setPaddingDuo(t: LayoutType) {
        if (main != null) {
            if (t == LayoutType.DuoLeft) main!!.setPadding(
                _edge_magrin,
                0,
                _edge_magrin / 2,
                0
            ) else if (t == LayoutType.DuoRight) main!!.setPadding(
                _edge_magrin / 2,
                0,
                _edge_magrin,
                0
            ) else if (t == LayoutType.Single) main!!.setPadding(_edge_magrin, 0, _edge_magrin, 0)
        }
        if (valueView != null) {
            valueView!!.setSingleLine(true)
            valueView!!.ellipsize = TextUtils.TruncateAt.END
        }
    }

    fun notifyUpdateSearchCount() {
        val isListener = context is OnSearchCountParamChangedListener
        if (context is OnSearchCountParamChangedListener) {
            (context as OnSearchCountParamChangedListener).onSearchCountParamChanged()
        }
    }

    interface OnSearchCountParamChangedListener {
        fun onSearchCountParamChanged()
    }
}