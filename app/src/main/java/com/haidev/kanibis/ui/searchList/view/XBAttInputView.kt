package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class XBAttInputView : ABaseAttInputView {

    constructor(
        context: Context, attribute: AttributeListItem,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context, null, factory, viewModel) {
        _attribute = attribute
        if (attribute != null) setLabel(attribute.name)
        this.tag = attribute
        init()
        moreInit()
    }

    fun moreInit() {
        setSingleLine(true)
        if (!isSearchLayout) {
            legendView!!.visibility = GONE
            legendView!!.text = "apps.insertion.legalrequired"
        }
    }

    override fun onMainValueChanged(params: XBSearchParameters) {
        if (_attribute != null) {
            if (_attribute!!.isInputType()) {
                val v = _value?.text.toString()
                val ok = params.attributeFilter.setAttributeText(
                    _attribute,
                    if (!TextUtils.isEmpty(v)) v else null
                )
                Log.v("", "0513 setAttributeText v  ${v } ok ${ok}");
            }
        }
    }
}
