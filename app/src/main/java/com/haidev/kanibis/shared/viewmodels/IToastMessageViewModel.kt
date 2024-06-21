package com.haidev.kanibis.shared.viewmodels

import androidx.annotation.IntDef
import android.widget.Toast

interface IToastMessageViewModel {
    val message: String?

    @get:Duration
    val duration: Int

    @IntDef(LENGTH_LONG, LENGTH_SHORT)
    annotation class Duration
    companion object {
        const val LENGTH_LONG = Toast.LENGTH_LONG
        const val LENGTH_SHORT = Toast.LENGTH_SHORT
    }
}