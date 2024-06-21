package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ScrollView
import androidx.core.view.GestureDetectorCompat
import kotlin.math.abs

class VerticalScrollView(context: Context?, attrs: AttributeSet?) : ScrollView(context, attrs) {
    private val mGestureDetector: GestureDetectorCompat
    var mGestureListener: OnTouchListener? = null
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        super.onInterceptTouchEvent(ev)
        return mGestureDetector.onTouchEvent(ev)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (_onScrollChangedListener != null) {
            _onScrollChangedListener!!.onScrollChanged(l, t, oldl, oldt)
        }
    }

    interface OnScrollChangedListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
    }

    var _onScrollChangedListener: OnScrollChangedListener? = null

    init {
        mGestureDetector = GestureDetectorCompat(
            context!!,
            object : GestureDetector.OnGestureListener {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    return false
                }

                override fun onShowPress(e: MotionEvent) {}
                override fun onScroll(
                    e1: MotionEvent?, e2: MotionEvent,
                    distanceX: Float, distanceY: Float
                ): Boolean {
                    return if (abs(distanceY.toDouble()) > abs(distanceX.toDouble())) {
                        true
                    } else false
                }

                override fun onLongPress(e: MotionEvent) {}
                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent,
                    velocityX: Float, velocityY: Float
                ): Boolean {
                    return false
                }

                override fun onDown(e: MotionEvent): Boolean {
                    return false
                }
            })
        setFadingEdgeLength(0)
    }

    fun setOnScrollChangedListener(onScrollChangedListener: OnScrollChangedListener?) {
        _onScrollChangedListener = onScrollChangedListener
    }
}
