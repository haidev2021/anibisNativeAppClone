package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.util.AnibisUtils.customizeSearchBox
import com.haidev.kanibis.shared.util.AnibisUtils.findEditText
import com.haidev.kanibis.shared.util.AnibisUtils.toastException
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.view.is24location.CustomDropdownKeyword
import com.haidev.kanibis.ui.searchList.view.is24location.ISAutoComplete


class KeywordSearchViewHolder(
    v: View,
    private val _context: Context
) : View.OnClickListener {
    private var _cv: CardView? = null
    private var _menuKeywordSearchView: SearchView? = null
    private var _vOpenSearch: View? = null
    private var _ivContextMenu: ImageView? = null
    private var _ivDelete: ImageView? = null
    private var _vContextAnchor: View? = null
    private var _rlAnchor: View? = null
    private val _dropDownWidthAndroid4 = 0
    private var _categorySelector: CategorySelector? = null

    fun getAndResetCategorySelector(): CategorySelector {
        val tmp = _categorySelector!!
        _categorySelector = null
        return tmp
    }

    val query: String?
        get() = if (mEditWhere != null) mEditWhere!!.getText().toString() else null
    val activity: Activity
        get() = _context as Activity

    private fun initViews(view: View) {
        _cv = view.findViewById(R.id.cvSearch)
        _ivContextMenu = view.findViewById(R.id.ivContextMenu)
        _ivDelete = view.findViewById(R.id.ivDelete)
        _vContextAnchor = view.findViewById(R.id.vContextAnchor)
        _rlAnchor = view.findViewById(R.id.rlAnchor)
        _menuKeywordSearchView = view.findViewById(R.id.MenuKeywordSearchView) as SearchView
        _menuKeywordSearchView!!.setQueryHint("")
        _vOpenSearch = view.findViewById(R.id.vOpenSearch)
        customizeSearchBox(_menuKeywordSearchView!!, -1)
        if (_vOpenSearch != null) _vOpenSearch!!.visibility = View.GONE
        _menuKeywordSearchView!!.setOnQueryTextFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.postDelayed(object : Runnable {
                    override fun run() {
                        val imm =
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(view.findFocus(), 0)
                    }
                }, 100)
            }
            if (_onFocusChangeListener != null) _onFocusChangeListener!!.onFocusChange(
                view,
                hasFocus
            )
        }
        customizeSearchView()
        val searchManager = activity.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        _menuKeywordSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(activity.componentName))
        initAutocompleteView(view)
    }

    private var mIsFirstTyping = true
    private val mSelectedLocation = ""
    private var mEditWhere: ISAutoComplete? = null
    private var _trueEditWhere: EditText? = null
    class CategorySelector(var suggestCat: Category?)

    private fun initAutocompleteView(root: View?) {
        if (root == null) return
        mIsFirstTyping = true
        mEditWhere = root.findViewById(R.id.autoCompleteView)
        mEditWhere!!.setHint("apps.searchkeyword")
        mEditWhere!!.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                _categorySelector = null
                setQuery(mEditWhere!!.getEditableText().toString(), true, false, "onEditorAction")
            }
            true
        }
        _trueEditWhere = findEditText(mEditWhere!!)
        mEditWhere!!.setOnFocusChangeListener { view, hasFocus ->
            try {
                if (_onFocusChangeListener != null) _onFocusChangeListener!!.onFocusChange(
                    view,
                    hasFocus
                )
                if (hasFocus && !(activity as ListResultActivity).isShowSearchViewOnly) mEditWhere!!.showDropDown()
            } catch (e: Exception) {
            }
        }
        if ((activity as ListResultActivity).isShowSearchViewOnly) requestFocus()
        mEditWhere!!.setDropDownWidth(activity.resources.displayMetrics.widthPixels / 2)
        mEditWhere!!.setText("")
        mEditWhere!!.setThreshold(3)
        mEditWhere!!.setOnClickListener(this)
        mEditWhere!!.setOnItemClickListener { adapterView, view, i, l ->
            if (adapterView != null) {
                val sug = adapterView.getItemAtPosition(i) as CustomDropdownKeyword
                if (sug != null) {
                    _categorySelector = CategorySelector(
                        if (sug.categoryId > 0) Category(sug.categoryId, "name", null, 0) else null
                    )
                    setQuery(sug.keyword, true, true, "onItemClick")
                }
            }
        }
        mEditWhere!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence, start: Int, before: Int,
                count: Int
            ) {
                if (mIsFirstTyping) {
                    mIsFirstTyping = false
                    val term = s.subSequence(start, start + count)
                        .toString()
                    mEditWhere!!.getText().clear()
                    mEditWhere!!.append(term)
                    FINAL_TERM = term
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(s.toString())) {
                    _menuKeywordSearchView!!.setQuery("", false)
                }
                _menuKeywordSearchView!!.visibility =
                    if (TextUtils.isEmpty(s.toString())) View.VISIBLE else View.GONE
            }
        })
        _ivDelete!!.setOnClickListener(this)
        customizeAutocompleteView()
    }

    private fun customizeSearchView() {
        val searchEditText =
            _menuKeywordSearchView!!.findViewById<AutoCompleteTextView>(androidx.appcompat.R.id.search_src_text)
        val params = searchEditText.layoutParams
        params.width = 0
        searchEditText.setLayoutParams(params)
        searchEditText.isFocusable = false
        searchEditText.setCursorVisible(false)
        searchEditText.setTextColor(-1)
    }

    private fun customizeAutocompleteView() {
        if (_rlAnchor != null) {
            _rlAnchor!!.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
                mEditWhere!!.dropDownWidth =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) _cv!!.width else _dropDownWidthAndroid4
            }
        }
    }

    fun setQuery(text: String?, commit: Boolean, updateUIText: Boolean, debug: String) {
        if (text != null && text.trim { it <= ' ' }.length > 0) {
            if (commit) mEditWhere!!.dismissDropDown()
            _menuKeywordSearchView!!.setQuery(text, commit)
        }
        if (updateUIText && mEditWhere != null && mEditWhere!!.getText().toString() != text) {
            mEditWhere!!.getText().clear()
            if (!TextUtils.isEmpty(text)) mEditWhere!!.append(text)
        }
    }

    fun clearFocus() {
        if (mEditWhere != null) mEditWhere!!.clearFocus()
    }

    private var _onFocusChangeListener: OnFocusChangeListener? = null

    init {
        initViews(v)
    }

    fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        _onFocusChangeListener = listener
    }

    private fun requestFocus() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (mEditWhere != null) {
                try {
                    mEditWhere!!.dispatchTouchEvent(
                        MotionEvent.obtain(
                            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, 0f, 0f, 0
                        )
                    )
                    mEditWhere!!.dispatchTouchEvent(
                        MotionEvent.obtain(
                            SystemClock.uptimeMillis(), SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_UP, 0f, 0f, 0
                        )
                    )
                } catch (e: Exception) {
                }
            }
        }, 200)
    }

    override fun onClick(v: View) {
        if (v.id == mEditWhere!!.id || v.id == _trueEditWhere!!.id) {
            mEditWhere!!.showDropDown()
        } else if (v.id == _ivDelete!!.id) {
            mEditWhere!!.setText("")
            requestFocus()
        }
    }

    fun onDestroy() {
    }

    companion object {
        private var FINAL_TERM = ""
    }
}
