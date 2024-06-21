package com.haidev.kanibis.shared.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsService

class CustomTabHelper(private val mContext: Context) {
    private var mPackageNameToUse: String? = null
    val packageName: String?
        get() {
            if (mPackageNameToUse != null) {
                return mPackageNameToUse
            }

            // Get default VIEW intent handler that can view a web url.
            val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.test-url.com"))

            // Get all apps that can handle VIEW intents.
            val pm = mContext.packageManager
            val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
            val packagesSupportingCustomTabs: MutableList<String> = ArrayList()
            for (info in resolvedActivityList) {
                val serviceIntent = Intent()
                serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
                serviceIntent.setPackage(info.activityInfo.packageName)
                if (pm.resolveService(serviceIntent, 0) != null) {
                    packagesSupportingCustomTabs.add(info.activityInfo.packageName)
                }
            }

            // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
            // and service calls.
            mPackageNameToUse = if (packagesSupportingCustomTabs.isEmpty()) {
                null
            } else if (packagesSupportingCustomTabs.size == 1) {
                packagesSupportingCustomTabs[0]
            } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
                STABLE_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
                BETA_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
                DEV_PACKAGE
            } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
                LOCAL_PACKAGE
            } else {
                packagesSupportingCustomTabs[0]
            }
            return mPackageNameToUse
        }

    companion object {
        private const val TAG = "CustomTabLauncher"
        private const val STABLE_PACKAGE = "com.android.chrome"
        private const val BETA_PACKAGE = "com.chrome.beta"
        private const val DEV_PACKAGE = "com.chrome.dev"
        private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    }
}
