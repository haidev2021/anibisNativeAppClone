package com.haidev.kanibis.shared.attribute.controller

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.controller.BaseDialogFragment
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.view.ABaseAttributeItemView

abstract class BaseAttributeEditorFragment<T : IBaseViewModel> : BaseDialogFragment<T>() {
    protected lateinit var _tvTitle: TextView
    protected lateinit var _root: ViewGroup
    protected lateinit var _llHeader: ViewGroup
    protected var _tvOk: TextView? = null
    protected lateinit var _ivBack: ImageView

    protected lateinit var _v: ABaseAttributeItemView
    fun setAttributeItemView(v: ABaseAttributeItemView) {
        _v = v
    }

    protected fun initViews(root: View) {
        translateView(root.findViewById(R.id.llHeader))
        _tvTitle = root.findViewById<View>(R.id.tvTitle) as TextView
        _tvTitle.setOnClickListener({ _ivBack.performClick() })
        _tvOk = root.findViewById<View>(R.id.tvOk) as TextView?
        _ivBack = root.findViewById<View>(R.id.ivBack) as ImageView
        _root = root.findViewById<View>(R.id.root) as ViewGroup
        _llHeader = root.findViewById<View>(R.id.llHeader) as ViewGroup

        _tvTitle.setFocusableInTouchMode(true)
        _tvTitle.setFocusable(true)
        _ivBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        _tvOk?.setOnClickListener{ requireActivity().onBackPressedDispatcher.onBackPressed() }
        _root.setOnClickListener{}
        translateView(_root)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        if (sDisableFragmentAnimations) {
            val a: Animation = object : Animation() {}
            a.setDuration(0)
            return a
        }
        return super.onCreateAnimation(transit, enter, nextAnim)
    }

    open fun customDismiss() {
       (getActivity() as ListResultActivity).pop(this)
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        var sDisableFragmentAnimations = false
    }
}
