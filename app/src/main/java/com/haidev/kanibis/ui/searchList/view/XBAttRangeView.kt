package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.util.MathUtils
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import kotlin.math.floor

class XBAttRangeView(
    context: Context, attribute: AttributeListItem,
    factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
) : ABaseAttributeItemView(context!!, null, factory, viewModel) {
    var _from: String? = null
    var _to: String? = null
    var _dfrom: Double? = null
    var _dto: Double? = null
    fun updateValue(dfrom: Double?, dto: Double?) {
        var dfrom = dfrom
        var dto = dto
        try {
            if (MathUtils.hasDifference(_dfrom, dfrom) || MathUtils.hasDifference(_dto, dto)) {
                if (dfrom != null && dto != null && dfrom > dto) {
                    val tmp: Double = dfrom
                    dfrom = dto
                    dto = tmp
                }
                _dfrom = dfrom
                _dto = dto
                _from = updateEditabeString(_dfrom)
                _to = updateEditabeString(_dto)
                onValueChanged(params)
                updateUIStringValue(makeValue())
            }
        } catch (e: Exception) {
        }
    }

    fun updateEditabeString(d: Double?): String {
        if (d == null) return ""
        val i = floor(d).toInt()
        return if (_attribute!!.type == AttributeListItem.XBAttributeType.InputInt.ordinal) "" + i else "" + d
    }

    private fun makeValue(): String {
        val FROM: String = mViewModel.translate(TextId.APPS_FROM)!!
        val TO: String = mViewModel.translate(TextId.APPS_TO)!!
        var result = ""
        if (_dfrom != null || _dto != null) result =
            ((if (_dfrom == null) "" else ((if (_dto == null) FROM else "") + SPACE
                    + formatAttributeDecimal(
                _dfrom,
                _attribute
            ) + getPosFix(_attribute!!)))
                    + (if (_dfrom == null && _dto == null) "" else SPACE) // " "
                    + if (_dto == null) "" else (TO + SPACE
                    + formatAttributeDecimal(_dto, _attribute)
                    + getPosFix(_attribute!!)))
        if (!TextUtils.isEmpty(result)) result = result.trim { it <= ' ' }
        return result
    }

    val SPACE = " "

    init {
        _attribute = attribute
        setLabel(attribute.name
                    + if (!TextUtils.isEmpty(attribute.unit)) " "
                    + attribute.unit else "")
        this.tag = attribute
    }

    fun getPosFix(_attribute: AttributeListItem): String {
        return if (_attribute.isPriceAttribute()) ".-" else ""
    }

    override fun onValueClick() {
        val dialogWrapper = RangeSelectDialogWrapper(this.context as Activity, mViewModel)
        dialogWrapper
            .setFinishInputRangeInterface(object : RangeSelectDialogWrapper.FinishInputRangeInterface {
                override fun onFinishInputRange(
                    rangeFrom: Double?,
                    rangeTo: Double?
                ) {
                    updateValue(rangeFrom, rangeTo)
                }
            })
        dialogWrapper.setXBRangeView(this@XBAttRangeView)
        if (!isFragmentShown) {
            dialogWrapper.showDialog()
        } else {
            showAttributeFragmentDialog(dialogWrapper.contentAsFragment)
        }
    }

    override fun onXClick() {
        updateValue(null, null)
    }

    override fun onMainValueChanged(params: XBSearchParameters) {
        params.attributeFilter?.setAttributeRange(_attribute, _dfrom, _dto)
    }

    fun checkUpdatingChild() {
        if (_attribute!!.children != null && _attribute!!.children!!.size > 0) factory!!.updateChildGrayOut(
            this.tag as LinearLayout,
            _dfrom != null || _dto != null
        )
    }

    fun syncValue() {
        if (!SearchMaskViewHolder._isRestorePhase && _attribute!!.id == 1) {
            onValueChanged(params)
        }
    }
}
