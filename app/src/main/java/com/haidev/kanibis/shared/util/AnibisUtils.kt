package com.haidev.kanibis.shared.util

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.ParseException
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

object AnibisUtils {
    private const val NON_THIN = "[^iIl1\\.,']"
    private const val PRICE_FORMAT_STRING = "###,###"
    private const val PRICE_SHORT_FORMAT_STRING = "###,###.##"
    private const val PRICE_GROUPING_SEPARATOR = '\''
    var _sharePricesMap = HashMap<Int, String>()
    private const val _isAutotestEnable = true
    private var _suspendUpdateChecks = false

    val sQuinticInterpolator = Interpolator { t ->
        var t = t
        t -= 1.0f
        t * t * t * t * t + 1.0f
    }

    fun getDisableAutoScrollLinearLayoutManager(
        context: Context?,
        orientation: Int,
        reverseLayout: Boolean
    ): LinearLayoutManager {
        return object : LinearLayoutManager(context, orientation, reverseLayout) {
            override fun requestChildRectangleOnScreen(
                parent: RecyclerView,
                child: View,
                rect: Rect,
                immediate: Boolean
            ): Boolean {
                return false
            }

            override fun requestChildRectangleOnScreen(
                parent: RecyclerView, child: View, rect: Rect,
                immediate: Boolean,
                focusedChildVisible: Boolean
            ): Boolean {
                return false
            }
        }
    }

    fun closeKeyboard(activity: Activity) {
        val inputMethodManger =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputMethodManger != null && activity.currentFocus != null) inputMethodManger.hideSoftInputFromWindow(
            activity.currentFocus!!.windowToken, 0
        )
    }

    fun <T>addNonNullItem(newItem: T?, list: List<T>): List<T> {
        return if (newItem == null ) list else listOf(newItem) + list
    }

    fun <T>safeFirstListItem(list: List<T>?): T? {
        return if (list.isNullOrEmpty()) null else list.first()
    }

    fun formatCount(count: String): String {
        val amount = count.toDouble()
        val dfs = DecimalFormatSymbols()
        dfs.setDecimalSeparator('.')
        dfs.setGroupingSeparator('\'')
        val formatter = DecimalFormat("#,###", dfs)
        return formatter.format(amount)
    }

    fun openWebLink(activity: Activity, url: String?) {
        Log.v("link", "openWebLink: url  = $url")
        Log.v("AnibisBaseActivity", "AnibisBaseActivity openWebLink")
        try {
            val refineUrl: String = LinkUtils.resolveServerUrl(url)
            if (LinkUtils.isAnibisLink(refineUrl) && !TextUtils.isEmpty(CustomTabHelper(activity).packageName)) {
                getCustomTabIntent(activity).launchUrl(activity, Uri.parse(refineUrl))
            } else {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(refineUrl))
                activity.startActivity(browserIntent)
                Log.d("WebLink", refineUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCustomTabIntent(activity: Activity): CustomTabsIntent {
        val intentBuilder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        intentBuilder.setShowTitle(true)
        intentBuilder.setCloseButtonIcon(
            BitmapFactory.decodeResource(
                activity.resources,
                com.haidev.kanibis.R.drawable.ic_back_white
            )
        )
        val intent: CustomTabsIntent = intentBuilder.build()
        intent.intent.setPackage(CustomTabHelper(activity).packageName)
        return intent
    }

    fun isLargeLayout(context: Context): Boolean {
        return context.resources.getBoolean(com.haidev.kanibis.R.bool.is_large_layout)
    }

    fun isFragmentOnStack(activity: AppCompatActivity, simpleName: String?): Boolean {
        return activity.supportFragmentManager.findFragmentByTag(simpleName) != null
    }

    fun updateRelativeLayoutTopMargin(v: View?, dy: Int, min: Int, max: Int, forceValue: Int?): Int {
        if (v != null) {
            val params = v.layoutParams as RelativeLayout.LayoutParams
            var newMarginTop = params.topMargin - dy
            if (forceValue != null) {
                newMarginTop = forceValue
            } else {
                if (newMarginTop < min) newMarginTop =
                    min else if (newMarginTop > max) newMarginTop = max
            }
            if (newMarginTop != params.topMargin) {
                params.setMargins(
                    params.leftMargin,
                    newMarginTop,
                    params.rightMargin,
                    params.bottomMargin
                )
                v.setLayoutParams(params)
            }
            return newMarginTop
        }
        return -1
    }


    fun customizeSearchBox(searchView: SearchView, imeAction: Int): AutoCompleteTextView? {
        val ctx: Context = searchView.getContext()
        searchView.setIconifiedByDefault(false)
        val search_text: AutoCompleteTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text)
        if (imeAction != -1) search_text!!.setImeOptions(imeAction)
        if (search_text != null) {
            search_text.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                ctx.resources.getDimensionPixelSize(com.haidev.kanibis.R.dimen.textsize_large).toFloat()
            )
            (search_text.parent as View).setPadding(0, 0, 0, 0)
            search_text.setDropDownBackgroundResource(com.haidev.kanibis.R.color.white)
        }
        val searchPlateView: View =
            searchView.findViewById(androidx.appcompat.R.id.search_plate)
        if (searchPlateView != null) searchPlateView.setBackgroundColor(0x00000000)

        searchView.setMaxWidth(ctx.resources.displayMetrics.widthPixels)
        return search_text
    }
    const val SPECTATOR_CHARACTER = "\u00A7"

    fun safeIntegerFromString(s: String): Int? {
        var i: Int? = null
        if (!TextUtils.isEmpty(s)) {
            try {
                i = s.toInt()
            } catch (e: java.lang.Exception) {
            }
        }
        return i
    }

    fun findEditText(view: View): EditText? {
        val time1 = System.currentTimeMillis()
        val edt: EditText? = findEditTextRecursive(view)
        val time2 = System.currentTimeMillis()
        Log.v("opttextkey", "time2 - time1 = " + (time2 - time1))
        return edt
    }

    private fun findEditTextRecursive(view: View): EditText? {
        if (view is EditText) {
            return view
        } else if (view is ViewGroup) {
            val parent = view
            Log.v("textKey", "0611 findEditTextRecursive")
            for (i in 0 until parent.childCount) {
                val v = parent.getChildAt(i)
                return findEditTextRecursive(v)
            }
        }
        return null
    }

    fun toastException(e: java.lang.Exception, className: String) {

    }

    @Throws(java.lang.Exception::class)
    fun openGoogleMapApp(activity: Activity, lat: Double, lng: Double, hasStreet: Boolean) {
        val gmmIntentUri: Uri
        if (hasStreet) {
            var uri =
                String.format(Locale.getDefault(), "geo:%f@%f?z=15&q=%f@%f", lat, lng, lat, lng)
            uri = uri.replace(",", ".").replace("@", ",")
            gmmIntentUri = Uri.parse(uri)
        } else {
            gmmIntentUri = Uri.parse("geo:$lat,$lng")
        }
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        activity.startActivity(mapIntent)
    }

    fun suspendUpdateChecks() {
        _suspendUpdateChecks = true
    }

    fun updateChecksSuspended(): Boolean {
        return _suspendUpdateChecks
    }

    fun setSRFValue(context: Context?, key: String?, value: String, prefName: String?) {
        Log.v("lost", "0102 setSRFValue value = $value")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.putString(key, value)
            editor.apply()
        }
    }

    fun getSRFValue(
        context: Context?,
        key: String?,
        defaultValue: String,
        prefName: String?
    ): String? {
        if (context != null) {
            val configs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val value = configs.getString(key, defaultValue)
            Log.v("lost", "0102 getSRFValue value = $value")
            return value
        }
        Log.v("lost", "0102 getSRFValue defaultValue = $defaultValue")
        return defaultValue
    }

    fun setSRFIntValue(context: Context?, key: String?, value: Int, prefName: String?) {
        Log.v("lost", "0102 setSRFValue value = $value")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.putInt(key, value)
            editor.apply()
        }
    }

    fun getSRFIntValue(context: Context?, key: String?, defaultValue: Int, prefName: String?): Int {
        if (context != null) {
            val configs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val value = configs.getInt(key, defaultValue)
            Log.v("lost", "0102 getSRFIntValue value = $value")
            return value
        }
        Log.v("lost", "0102 getSRFValue defaultValue = $defaultValue")
        return defaultValue
    }

    fun setSRFLongValue(context: Context?, key: String?, value: Long, prefName: String?) {
        Log.v("lost", "0102 setSRFValue value = $value")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.putLong(key, value)
            editor.apply()
        }
    }

    fun getSRFLongValue(
        context: Context?,
        key: String?,
        defaultValue: Long,
        prefName: String?
    ): Long {
        if (context != null) {
            val configs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val value = configs.getLong(key, defaultValue)
            Log.v("lost", "0102 getSRFlongValue value = $value")
            return value
        }
        Log.v("lost", "0102 getSRFValue defaultValue = $defaultValue")
        return defaultValue
    }

    fun setSRFBooleanValue(context: Context?, key: String?, value: Boolean, prefName: String?) {
        Log.v("lost", "0102 setSRFValue value = $value")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.putBoolean(key, value)
            editor.apply()
        }
    }

    fun getSRFBooleanValue(
        context: Context?,
        key: String?,
        defaultValue: Boolean,
        prefName: String?
    ): Boolean {
        if (context != null) {
            val configs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val value = configs.getBoolean(key, defaultValue)
            Log.v("lost", "0102 getSRFbooleanValue value = $value")
            return value
        }
        Log.v("lost", "0102 getSRFValue defaultValue = $defaultValue")
        return defaultValue
    }

    fun setSRFStringValue(context: Context?, key: String?, value: String?, prefName: String?) {
        Log.v("lost", "0102 setSRFValue value = $value")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.putString(key, value)
            editor.apply()
        }
    }

    fun removeSRFStringKey(context: Context?, key: String, prefName: String?) {
        Log.v("lost", "0102 removeSRFStringKey key = $key")
        if (context != null) {
            val editor = context.getSharedPreferences(prefName, Context.MODE_PRIVATE).edit()
            editor.remove(key)
            editor.apply()
        }
    }

    fun getSRFStringValue(
        context: Context?,
        key: String?,
        defaultValue: String,
        prefName: String?
    ): String? {
        if (context != null) {
            val configs = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
            val value = configs.getString(key, defaultValue)
            Log.v("lost", "0102 getSRFStringValue value = $value")
            return value
        }
        Log.v("lost", "0102 getSRFValue defaultValue = $defaultValue")
        return defaultValue
    }

    fun updatePricesMapItems(id: Int, complexPrice: String?) {
        if (!_sharePricesMap.containsKey(id)) {
            _sharePricesMap.remove(id)
        }
        _sharePricesMap[id] = (complexPrice)!!
        Log.v("_sharePricesMap", "_sharePricesMap size = " + _sharePricesMap.size)
    }

    @Throws(FileNotFoundException::class)
    fun decodeImageUri(activity: Activity, selectedImage: Uri?): Bitmap? {

        // Decode image size
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeStream(
            activity.contentResolver.openInputStream(selectedImage!!), null, o
        )

        // The new size we want to scale to
        val REQUIRED_SIZE = 140

        // Find the correct scale value. It should be the power of 2.
        var width_tmp = o.outWidth
        var height_tmp = o.outHeight
        var scale = 1

        //compute scale ratio
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                || height_tmp / 2 < REQUIRED_SIZE
            ) {
                break
            }
            width_tmp /= 2
            height_tmp /= 2
            scale *= 2
        }

        // Decode with inSampleSize
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = scale
        return BitmapFactory.decodeStream(
            activity.contentResolver.openInputStream(selectedImage), null, o2
        )
    }

    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    fun notifySetContentDescription(v: View?, s: String?) {
        if (_isAutotestEnable && v != null) {
            v.setContentDescription(s)
        }
    }

    fun isDuoGrouped(parentId: Int): Boolean {
        return parentId == AttributeListItem.getPriceTypeAttributeId()
                || parentId == AttributeListItem.ATTRIBUTE_ID_AVAILABILITY
                || parentId == AttributeListItem.ATTRIBUTE_ID_ANIMAL_SKG
                || parentId == AttributeListItem.ATTRIBUTE_ID_OBJEKTART
    }

    fun isChildAttIgnored(parentId: Int, parentEntry: AttributeEntryListItem?): Boolean {
        if (parentId == AttributeListItem.getPriceTypeAttributeId()) {
            return parentEntry == null || parentEntry.id == AttributeEntryListItem.ATTRIBUTE_ENTRY_ID_ON_DEMAND
                    || parentEntry.id == AttributeEntryListItem.ATTRIBUTE_ENTRY_ID_FREE
        } else if (parentId == AttributeListItem.ATTRIBUTE_ID_AVAILABILITY) {
            return parentEntry == null || parentEntry.id == AttributeEntryListItem.ATTRIBUTE_ENTRY_ID_FROM_NOW
                    || parentEntry.id == AttributeEntryListItem.ATTRIBUTE_ENTRY_ID_FROM_DEMAND
        } else if (parentId == AttributeListItem.ATTRIBUTE_ID_OBJEKTART) {
            return parentEntry == null
        } else return if (parentId == AttributeListItem.ATTRIBUTE_ID_ANIMAL_SKG) {
            parentEntry == null
        } else {
            false
        }
    }

    fun safeDoubleFromString(s: String): Double? {
        var d: Double? = null
        if (!TextUtils.isEmpty(s)) {
            try {
                d = s.toDouble()
            } catch (e: java.lang.Exception) {
                toastException(e, "AnibisUtils.java.safeDoubleFromString")
            }
        }
        return d
    }

    fun getGroupVisible(parent: View?, content: View?): Boolean {
        return parent != null && content != null && parent.visibility == View.VISIBLE && content.visibility == View.VISIBLE
    }

    fun setGalleryViewSize(width: Int, v: View) {
        val params1 = v.layoutParams
        params1.width = width
        params1.height = width * 3 / 4
        v.setLayoutParams(params1)
    }

    fun setInsertionGalleryViewSize(width: Int, v: View) {
        val params1 = v.layoutParams
        params1.width = width
        params1.height = width * 264 / 360
        v.setLayoutParams(params1)
    }

    fun setCardGalleryViewSize(width: Int, v: View) {
        Log.v("1612", " setCardGalleryViewSize:  | v. = $v")
        val params1 = v.layoutParams
        params1.width = width
        params1.height = width * 194 / 344
        v.setLayoutParams(params1)
    }

    fun ellipsize(text: String, max: Int): String {
        if (TextUtils.isEmpty(text) || textWidth(text) <= max) return text

        // Start by chopping off at the word before max
        // This is an over-approximation due to thin-characters...
        var end = text.lastIndexOf(' ', max - 3)

        // Just one long word. Chop it off.
        if (end == -1) return text.substring(0, max - 3) + "..."

        // Step forward as long as textWidth allows.
        var newEnd = end
        do {
            end = newEnd
            newEnd = text.indexOf(' ', end + 1)

            // No more spaces.
            if (newEnd == -1) newEnd = text.length
        } while (textWidth(text.substring(0, newEnd) + "...") < max)
        return text.substring(0, end) + "..."
    }

    @SuppressLint("DefaultLocale")
    fun getFormattedPrice(currencySymbol: String?, price: Int): String {
        val symbol = DecimalFormatSymbols()
        symbol.setGroupingSeparator(PRICE_GROUPING_SEPARATOR)
        val format = DecimalFormat(PRICE_FORMAT_STRING, symbol)
        return String.format(
            "%s %s.\u2014", currencySymbol,
            format.format(price.toLong())
        )
    }

    fun getFormattedShortenedPrice(
        currencySymbol: String?,
        price: Int, mioSymbol: String?
    ): String {
        if (price < 1000000) return getFormattedPrice(currencySymbol, price) else {
            var p = price.toDouble()
            p = p / 1000000
            val symbol = DecimalFormatSymbols()
            symbol.setGroupingSeparator(PRICE_GROUPING_SEPARATOR)
            val format = DecimalFormat(
                PRICE_SHORT_FORMAT_STRING,
                symbol
            )
            return String.format(
                "%s %s %s", currencySymbol, format.format(p),
                mioSymbol
            )
        }
    }

    fun isEmailValid(email: String?): Boolean {
        return if (TextUtils.isEmpty(email)) false else Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    fun openAppInPlayStore(context: Context) {
        val appPackageName = context.packageName
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse(
                        "http://play.google.com/store/apps/details?id="
                                + appPackageName
                    )
                )
            )
        }
    }

    private fun textWidth(str: String): Int {
        return str.length - str.replace(NON_THIN.toRegex(), "").length / 2
    }


    @TargetApi(Build.VERSION_CODES.M)
    fun shouldAskStoragePermissionForM(activity: Activity): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun shouldAskLocationPermissionForM(activity: Activity): Boolean {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (
                activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) && (
                activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
    }

    fun isPreM(): Boolean {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestStoragePermissionsForM(activity: Activity, code: Int) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            val perms = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE)
            activity.requestPermissions(perms, code)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestLocationPermissionsForM(activity: Activity, code: Int) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
            val perms = arrayOf<String>(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            activity.requestPermissions(perms, code)
        }
    }

    fun showKeyboard(activity: Activity, v: View?) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
    }

    private var _isSmallCardMode: Boolean? = null
    fun getIsSmallCardMode(_context: Context?): Boolean {
        if (_isSmallCardMode == null) {
            // get the card-mode that is stored in the shared prefs
            // 1 = smallcards, 2 = bigcards, default = bigcards
            val srfVal = getSRFIntValue(_context, "CardViewType", 0, "DefaultSRF")
            _isSmallCardMode = (srfVal == 1)
        }
        return _isSmallCardMode!!
    }

    fun setIsSmallCardMode(_context: Context?, b: Boolean) {
        val srfVal = if (b) 1 else 2 // 1 = smallcards, 2 = bigcards
        setSRFIntValue(_context, "CardViewType", srfVal, "DefaultSRF")
        _isSmallCardMode = b
    }

    private val EMAIL_HASH_SRF_NAME = "email_hash_srf_name"
    private val EMAIL_HASH_SRF_KEY = "email_hash_srf_key"

    fun getSaveEmailHash(context: Context?): String {
        return if (context == null) "" else (getSRFStringValue(
            context,
            EMAIL_HASH_SRF_KEY,
            "",
            EMAIL_HASH_SRF_NAME
        ))!!
    }

    fun updateRelativeLayoutTopMargin(v: View?, dy: Int, min: Int, max: Int, forceValue: Int): Int {
        if (v != null) {
            val params = v.layoutParams as RelativeLayout.LayoutParams
            var newMarginTop = params.topMargin - dy
            if (forceValue != null) {
                newMarginTop = forceValue
            } else {
                if (newMarginTop < min) newMarginTop =
                    min else if (newMarginTop > max) newMarginTop = max
            }
            if (newMarginTop != params.topMargin) {
                params.setMargins(
                    params.leftMargin,
                    newMarginTop,
                    params.rightMargin,
                    params.bottomMargin
                )
                v.setLayoutParams(params)
            }
            return newMarginTop
        }
        return -1
    }

    fun updateDrawerLayoutTopMargin(v: View?, dy: Int, min: Int, max: Int, forceValue: Int): Int {
        if (v != null) {
            val params = v.layoutParams as DrawerLayout.LayoutParams
            var newMarginTop = params.topMargin - dy
            if (forceValue != null) {
                newMarginTop = forceValue
            } else {
                if (newMarginTop < min) newMarginTop =
                    min else if (newMarginTop > max) newMarginTop = max
            }
            if (newMarginTop != params.topMargin) {
                params.setMargins(
                    params.leftMargin,
                    newMarginTop,
                    params.rightMargin,
                    params.bottomMargin
                )
                v.setLayoutParams(params)
                Log.v(
                    "1602",
                    " updateDrawerLayoutTopMargin:  | newMarginTop. = $newMarginTop"
                )
            }
            return newMarginTop
        }
        return -1
    }

    fun setLinearLayoutTopMargin(v: View, newMargin: Int) {
        val params = v.layoutParams as LinearLayout.LayoutParams
        if (params.topMargin != newMargin) {
            params.setMargins(params.leftMargin, newMargin, params.rightMargin, params.bottomMargin)
            v.setLayoutParams(params)
        }
    }

    private val SWISS_PHONE_PATTERN: Pattern =
        Pattern.compile("^(?:(?:|0{1,2}|\\+{0,2})41(?:|\\(0\\))|0)(7[5-9])(\\d{3})(\\d{2})(\\d{2})$")

    fun isMatchSMSNumber(s: String?): Boolean {
        try {
            val matcher: Matcher = SWISS_PHONE_PATTERN.matcher(s)
            return matcher.matches()
        } catch (e: RuntimeException) {
            return false
        }
    }

    fun sendSMSToNumber(activity: Activity, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
            activity.startActivity(intent)
        } catch (e: RuntimeException) {
        }
    }

    fun getUriFromIntent(intent: Intent?, requiredAction: String): Uri? {
        Log.v("2906", " getUriFromIntent:  | intent. = $intent")
        if (intent != null) {
            val action = intent.action
            Log.v("2906", " getUriFromIntent:  | action. = $action")
            return if ((requiredAction == action)) intent.data else null
        } else return null
    }

    val GOOGLE_PHOTOS_PACKAGE_NAME = "com.google.android.apps.photos"

    fun isGooglePhotosAvailable(context: Context): Boolean {
        try {
            val pi = context.packageManager.getPackageInfo(
                GOOGLE_PHOTOS_PACKAGE_NAME,
                PackageManager.GET_ACTIVITIES
            )
            val ai: ApplicationInfo =
                context.packageManager.getApplicationInfo(GOOGLE_PHOTOS_PACKAGE_NAME, 0)
            return (pi != null) && (ai != null) && ai.enabled
        } catch (e: java.lang.Exception) {
            Log.v("0702", " isGooglePhotosAvailable:  | e. = $e")
            return false
        }
    }

    fun getGooglePhotosVersion(context: Context): String {
        try {
            val pi = context.packageManager.getPackageInfo(GOOGLE_PHOTOS_PACKAGE_NAME, 0)
            return pi.versionName + "(" + pi.versionCode + ")"
        } catch (e: java.lang.Exception) {
            return "NotInstalled"
        }
    }

    fun refinePhoneMain(phoneMain: String?): String? {
        var phoneMain = phoneMain
        if (phoneMain != null) {
            phoneMain = phoneMain.trim { it <= ' ' }
            while (phoneMain!!.startsWith("0")) phoneMain = phoneMain.substring(1)
            return phoneMain
        } else {
            return ""
        }
    }

    fun getDisableAutoScrollLLM(
        context: Context?,
        orientation: Int,
        reverseLayout: Boolean
    ): LinearLayoutManager {
        return object : LinearLayoutManager(context, orientation, reverseLayout) {
            override fun requestChildRectangleOnScreen(
                parent: RecyclerView,
                child: View,
                rect: Rect,
                immediate: Boolean
            ): Boolean {
                return false
            }

            override fun requestChildRectangleOnScreen(
                parent: RecyclerView, child: View, rect: Rect,
                immediate: Boolean,
                focusedChildVisible: Boolean
            ): Boolean {
                return false
            }
        }
    }

    fun getOpenResultByCountText(count: Int?, viewModel: IBaseViewModel): String {
        return String.format(
            viewModel.translate(
                if (count == null || count == 0) "apps.action.showresultsfromcategories_0"
                else (if (count == 1) "apps.action.showresultsfromcategories_1"
                else "apps.action.showresultsfromcategories_n")
            )!!.replace("%d", "%s"),
            if (count != null) formatCount(count.toString()) else ""
        )
    }

    fun getDateFormatString(): String {
        return "yyyy-MM-dd'T'HH:mm:ss"
    }

    fun getDateFromISO8601String(date: String?): Date? {
        return if (TextUtils.isEmpty(date)) null else try {
            val sdf = SimpleDateFormat(
                getDateFormatString(),
                Locale.US
            )
            sdf.parse(date)
        } catch (e: ParseException) {
            null
        }
    }

    fun getISO8601TimeStamp(date: Date?): String? {
        if (date == null) return null
        val sdf = SimpleDateFormat(
            getDateFormatString(),
            Locale.US
        )
        return sdf.format(date)
    }

    fun formatNumber(number: Double?, attribute: AttributeListItem): String {
        if (number == null) return ""
        val dfs = DecimalFormatSymbols()
        dfs.setDecimalSeparator('.')
        dfs.setGroupingSeparator('\'')
        val formatter: DecimalFormat
        formatter = if (attribute.id == 108 || attribute.id == 4) {
            DecimalFormat("#", dfs)
        } else if (attribute.id == 28) {
            DecimalFormat("#,###.0", dfs)
        } else if (attribute.type == AttributeListItem.XBAttributeType.InputInt.ordinal) {
            DecimalFormat("#,###", dfs)
        } else if (attribute.type == AttributeListItem.XBAttributeType.InputDecimal.ordinal) {
            DecimalFormat("#,###.00", dfs)
        } else if (attribute.type == AttributeListItem.XBAttributeType.InputDate.ordinal) {
            DecimalFormat("dd-MM-yyyy", dfs)
        } else {
            DecimalFormat("#", dfs)
        }
        var numberString = formatter.format(number)
        numberString += " " + attribute.unit
        return numberString
    }

    fun getMimeType(path: String): String? {
        if (TextUtils.isEmpty(path)) return null
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(path.substring(path.lastIndexOf(".")))
        if (extension != null) {
            type = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
        }
        return type
    }

    fun format(date: Date?, lang: String): String {
        if (date == null) return ""
        val locale = Locale(lang)
        val sdf = SimpleDateFormat("dd.MM.yyyy", locale)
        return sdf.format(date)
    }

    fun formatNumber(number: Double): String {
        val locale: String = "FR_CH";//LocalizationManager.instance().getCurrentLanguageIsoCode() + "_CH" // refactor
        val formater: NumberFormat = NumberFormat.getInstance(Locale(locale))
        return formater.format(number)
    }
}
