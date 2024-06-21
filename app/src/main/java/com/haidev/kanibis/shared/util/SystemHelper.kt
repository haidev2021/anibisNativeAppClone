package com.haidev.kanibis.shared.util

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.util.DisplayMetrics

object SystemHelper {
    fun makeCall(context: Context, phone: String?) {
        var phone = phone
        if (phone != null) {
            if (phone.contains("//")) {
                phone = phone.replace("//", "")
            }
            if (checkCallable(context)) {
                val dialIntent = Intent(
                    Intent.ACTION_DIAL,
                    Uri.parse(
                        if (phone.startsWith("tel:")) phone else ("tel:"
                                + phone)
                    )
                )
                context.startActivity(dialIntent)
            } else {
                val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE)
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                context.startActivity(intent)
            }
        }
    }

    fun checkCallable(_context: Context): Boolean {
        val manager: TelephonyManager = _context
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE
    }

    fun isTablet(context: Context): Boolean {
        return ((context.resources.configuration.screenLayout
                and Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE)
    }

    fun isPortrait(context: Context): Boolean {
        return (context.resources.configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT)
    }

    fun isLandscape(context: Context): Boolean {
        return (context.resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE)
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getWidthDp(context: Context): Int {
        val widthPixels = context.resources.displayMetrics.widthPixels
        return convertPixelsToDp(widthPixels, context).toInt()
    }

    fun getHeightDp(context: Context): Int {
        val widthPixels = context.resources.displayMetrics.heightPixels
        return convertPixelsToDp(widthPixels, context).toInt()
    }

    fun convertPixelsToDp(px: Int, context: Context): Int {
        val resources = context.resources
        val metrics: DisplayMetrics = resources.displayMetrics
        return px * DisplayMetrics.DENSITY_DEFAULT / metrics.densityDpi
    }
}
