package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute

open class XBEditText : AppCompatEditText {
    private var _attribute: AttributeListItem? = null

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    constructor(context: Context, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context) : super(context!!)

    constructor(context: Context, attribute: AttributeListItem) : super(context!!) {
        setAttribute(attribute)
    }

    fun setAttribute(attribute: AttributeListItem) {
        _attribute = attribute
        if (attribute.type == AttributeListItem.XBAttributeType.InputDecimal.ordinal)
            setInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
        else if (attribute.type == AttributeListItem.XBAttributeType.InputInt.ordinal)
            setInputType(InputType.TYPE_CLASS_NUMBER)
        else if (attribute.type == AttributeListItem.XBAttributeType.InputDate.ordinal)
            setInputType(InputType.TYPE_CLASS_DATETIME)
        else {
            setInputType(InputType.TYPE_CLASS_TEXT)
        }
    }
}
