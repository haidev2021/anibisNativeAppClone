package com.haidev.kanibis.shared.util

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.haidev.kanibis.shared.localization.service.ILocalizationService

object TextkeyUtils {
    fun translateView(view: View?, service: ILocalizationService) {
        translateViewRecursive(view, service)
    }
    private fun translateViewRecursive(view: View?, service: ILocalizationService) {
            if (view is TextView) {
                translateViewWithContenDescriptionIndicator(view, service)
            } else if (view is ViewGroup) {
                val parent: ViewGroup = view as ViewGroup
                for (i in 0 until parent.getChildCount()) {
                    val v: View = parent.getChildAt(i)
                    translateViewRecursive(v, service)
                }
            }
    }

    private fun translateViewWithTagIndicator(v: View, service: ILocalizationService) {
        val tag = v.tag
        if (tag != null && tag is String &&
            (tag.toString().startsWith("apps") || v.tag.toString().endsWith("*"))
        ) {
            Log.v("textKey", "0611 loadTextkeyWithTagIndicator  : $tag")
            val translated: String? = service.translate(tag.toString())
            if (v is EditText) {
                v.setHint(translated)
            } else if (v is TextView) {
                v.setText(translated)
            }
        }
    }

    private fun translateViewWithContenDescriptionIndicator(v: View, service: ILocalizationService) {
        val cd = v.contentDescription as String?
        if (cd != null && (cd.startsWith("apps") || cd.startsWith("#") || cd.endsWith("*"))) {
            val translated: String? = service.translate(cd)
            if (v is EditText) {
                v.setHint(translated)
            } else if (v is TextView) {
                v.setText(translated)
            }
        }
    }
}
