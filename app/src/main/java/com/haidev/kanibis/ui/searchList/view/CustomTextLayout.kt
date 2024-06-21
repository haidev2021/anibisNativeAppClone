package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout

class CustomTextLayout : TextInputLayout {
    constructor(context: Context) : super(context!!) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        init()
    }

    fun init() {
        isErrorEnabled = false
    }
}