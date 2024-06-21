package com.haidev.kanibis.ui.searchList.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.core.content.res.ResourcesCompat
import com.haidev.kanibis.R

class XBAttributeEditText : XBEditText {
    private val imgCloseButton: Drawable? =
        ResourcesCompat.getDrawable(resources, R.drawable.ic_x_gray, null)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context, attrs, defStyle) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        imgCloseButton!!.setBounds(
            0, 0, imgCloseButton.getIntrinsicWidth(),
            imgCloseButton.getIntrinsicHeight()
        )
        handleClearButton()
        setOnTouchListener(OnTouchListener { v, event ->
            val et = this@XBAttributeEditText
            if (et.getCompoundDrawables()[2] == null) return@OnTouchListener false
            if (event.action != MotionEvent.ACTION_UP) return@OnTouchListener false
            if (event.x > (et.width - et.getPaddingRight()
                        - imgCloseButton.getIntrinsicWidth())
            ) {
                et.setText("")
                handleClearButton()
            }
            false
        })

        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
                handleClearButton()
            }

            override fun afterTextChanged(arg0: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int, count: Int,
                after: Int
            ) {
            }
        })
    }

    fun handleClearButton() {
        if (getText().toString() == "") {
            setCompoundDrawables(
                getCompoundDrawables()[0],
                getCompoundDrawables()[1], null,
                getCompoundDrawables()[3]
            )
        } else {
            setCompoundDrawables(
                getCompoundDrawables()[0],
                getCompoundDrawables()[1], imgCloseButton,
                getCompoundDrawables()[3]
            )
        }
    }
}