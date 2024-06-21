package com.haidev.kanibis.ui.searchList.view

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import java.util.Calendar
import java.util.Date

abstract class XBAttDateView : ABaseAttributeItemView {
    var _inputType = -1
    var _inputWatcher: TextWatcher? = null
    var _dateValue: Date? = null

    constructor(context: Context, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context, label: String?) : super(context!!, null) {
        setLabel(label!!)
    }

    constructor(
        context: Context?, label: String?,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context!!, null, factory, viewModel) {
        setLabel(label!!)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context, viewModel)

    constructor(context: Context, attribute: AttributeListItem) : super(context, null) {
        _attribute = attribute
        if (attribute != null) setLabel(attribute.name)
        this.tag = attribute
    }

    constructor(
        context: Context?, attribute: AttributeListItem,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context!!, null, factory, viewModel) {
        _attribute = attribute
        if (attribute != null) setLabel(attribute.name)
        this.tag = attribute
    }

    override fun onValueClick() {
        val dateSetListener = OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar[year, monthOfYear] = dayOfMonth
            updateValue(Date(calendar.getTimeInMillis()))
        }
        val calendar = Calendar.getInstance()
        val initYear = if (_dateValue != null) (DateFormat.format(
            "yyyy",
            _dateValue
        ) as String).toInt() else calendar[Calendar.YEAR]
        val initMonth = if (_dateValue != null) (DateFormat.format(
            "MM",
            _dateValue
        ) as String).toInt() else calendar[Calendar.MONTH]
        val initDay = if (_dateValue != null) (DateFormat.format(
            "dd",
            _dateValue
        ) as String).toInt() else calendar[Calendar.DAY_OF_MONTH]

        val dialog = DatePickerDialog(
            this.context, R.style.DateDialogTheme, dateSetListener,
            initYear, initMonth, initDay
        )
        if (_dateValue != null) {
            dialog.setButton(
                DatePickerDialog.BUTTON_NEUTRAL,
                mViewModel.translate(TextId.APPS_ACTION_DELETE),
                    { dialog, whichButton -> updateValue(null) }
            )
        }
        dialog.setButton(
            DatePickerDialog.BUTTON_NEGATIVE,
            mViewModel.translate(TextId.APPS_ACTION_CANCEL),
                { dialog, whichButton -> }
        )

        dialog.setButton(
            DatePickerDialog.BUTTON_POSITIVE,
            mViewModel.translate(TextId.APPS_ACTION_DONE)
        ) { dialogInterface, whichButton ->
            val datePicker = dialog.datePicker
            dateSetListener.onDateSet(
                datePicker, datePicker.year,
                datePicker.month, datePicker.dayOfMonth
            )
        }

        dialog.show()
    }

    override fun onXClick() {
        updateValue(null)
    }

    fun updateValue(newValue: Date?) {
        try {
            if (_dateValue != null
                && _dateValue != newValue
                || newValue != null
                && newValue != _dateValue
            ) {
                _dateValue = newValue
                onValueChanged(params)
                val formatString = if (_dateValue != null) AnibisUtils.format(_dateValue, mViewModel.currentLanguageIsoCode) else ""
                super.updateUIStringValue(formatString)
            }
        } catch (e: Exception) {
        }
    }

    override fun onMainValueChanged(params: XBSearchParameters) {
        if (_attribute != null) {
            params.attributeFilter.setAttributeDate(_attribute, _dateValue)
        }
    }

    fun checkUpdatingChild() {
        if (_attribute!!.children != null && _attribute!!.children!!.size > 0) factory!!.updateChildGrayOut(
            this.tag as LinearLayout,
            _dateValue != null
        )
    }

}
