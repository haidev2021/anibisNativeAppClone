package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.controller.BaseActivity
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.ui.searchList.view.ABaseCategoryView.UpdateAttributeLayoutListener
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class SearchMaskViewHolder(
    context: Context, v: View, categoryView: ABaseCategoryView, val mViewModel: ISearchListViewModel
) {
    private val _context: Context
    private val _view: View
    val rootCategoriesView: ABaseCategoryView
    val attributeViewFactory: CategoryAttributeViewFactory
    private lateinit var _txtSearchText: AutoCompleteTextView
    private var _MenuKeywordSearchView: SearchView? = null
    private lateinit var _llCategoryAttLayout: LinearLayout
    private lateinit var _llCommonAttLayout: LinearLayout
    private lateinit var _llPriceLayout: LinearLayout
    private lateinit var _llLanguageLayout: LinearLayout
    private lateinit var _imgTopAction: ImageView
    private lateinit var _llSearchText: LinearLayout
    private lateinit var _rootCategoriesLayout: LinearLayout
    private lateinit var _focusable: View


    val activity: Activity
        get() = _context as Activity

    init {
        _context = context
        _view = v
        rootCategoriesView = categoryView
        attributeViewFactory = CategoryAttributeViewFactory(_context, mViewModel)
        initViews()
    }

    fun createCategoryAttributeViews(list:  List<AttributeListItem>) {
        attributeViewFactory.createCategoryAttributeViewsInLayout(_llCategoryAttLayout, list)
    }

    private fun initViews() {
        _focusable = _view.findViewById<View>(R.id.focusable)
        _llSearchText = _view
            .findViewById<View>(R.id.llSearchText) as LinearLayout
        _MenuKeywordSearchView = _view.findViewById<View>(R.id.MenuKeywordSearchView) as SearchView
        if (_MenuKeywordSearchView != null) {
            _MenuKeywordSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }
            })
            if (_llSearchText != null) _txtSearchText = AnibisUtils.customizeSearchBox(
                _MenuKeywordSearchView!!,
                EditorInfo.IME_ACTION_DONE
            )!!
        }
        if (_llSearchText != null) _llSearchText!!.visibility = View.VISIBLE
        rootCategoriesView.resetArray()
        rootCategoriesView.updateAttributeLayoutListener = object : UpdateAttributeLayoutListener {
            override fun onUpdateAttributeLayout(finalCategory: CategoryListItem?) {
                Log.v("", "0509 onUpdateAttributeLayout");
                mViewModel.setCategory(finalCategory)
                if (_postCatAttViewCreatedListener != null) {
                    _postCatAttViewCreatedListener!!.onPostCatAttViewCreated(finalCategory)
                }
            }
        }
        rootCategoriesView.setLayoutParams(
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        _rootCategoriesLayout = _view.findViewById<View>(R.id.rootCategoriesLayout) as LinearLayout
        _rootCategoriesLayout!!.addView(rootCategoriesView)

        if (_txtSearchText != null) _txtSearchText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (!_isRestorePhase) mViewModel.setSearchText(_txtSearchText!!.getText().toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
        _llCommonAttLayout = _view
            .findViewById<View>(R.id.llCommonAttLayout) as LinearLayout
        _llPriceLayout = _view
            .findViewById<View>(R.id.llPriceLayout) as LinearLayout
        _llLanguageLayout = _view
            .findViewById<View>(R.id.llLanguageLayout) as LinearLayout

        _llCategoryAttLayout = _view
            .findViewById<View>(R.id.llCategoryAttLayout) as LinearLayout
        _imgTopAction = _view.findViewById<View>(R.id.imgTopAction) as ImageView
        if (_imgTopAction != null) {
            _imgTopAction!!.setVisibility(View.VISIBLE)
            _imgTopAction!!.setOnClickListener { resetAllClick() }
        }
        mViewModel.translateView(_view)
        loadTextkeyForSpecialText()
        initCommonAttributes()
    }

    fun addView(child: View?) {
        if (_rootCategoriesLayout != null) _rootCategoriesLayout!!.addView(child)
    }

    fun hideSearchTextVisibility() {
        if (_llSearchText != null) _llSearchText!!.visibility = View.GONE
    }
    private fun loadTextkeyForSpecialText() {
        if (_txtSearchText != null) _txtSearchText.setHint(
            mViewModel.translate(
                TextId.APPS_SEARCHKEYWORD
            )
        )
        rootCategoriesView.setLabel(mViewModel.translate("apps.categories")!!)
    }

    private fun initCommonAttributes() {
        if (mViewModel._searchMaskInfo.isSearch) {
            attributeViewFactory
                .createCommonAttributeViewsInLayout(
                    _llCommonAttLayout,
                    _llPriceLayout,
                    _llLanguageLayout
                )
        } else attributeViewFactory
            .createCommonAttributeViewsInLayout(_llCommonAttLayout)
    }

    private fun resetAll(resetKeyword: Boolean) {
        mViewModel.resetAll(resetKeyword)
        restoreLastSearch()
    }

    fun resetAllClick() {
        AlertDialog.Builder(activity)
            .setTitle(mViewModel.translate("apps.action.resetfilters"))
            .setMessage(mViewModel.translate("apps.resetsearch"))
            .setPositiveButton(mViewModel.translate(TextId.APPS_ACTION_YES)
            ) { dialog, which ->
                resetAll(false)
            }
            .setNegativeButton(mViewModel.translate(TextId.APPS_ACTION_CANCEL)
            ) { dialog, which -> }
            .show()
    }

    fun restoreLastSearch() {
        (activity as BaseActivity<ISearchListViewModel>).addDisposable(mViewModel
            .restoreLastSearch()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { category ->
                _isRestorePhase = true
                if (_txtSearchText != null) _txtSearchText.setText(mViewModel._searchMaskParams.searchText)
                rootCategoriesView.initByChildMostCategory(mViewModel._searchMaskParams.category)
                attributeViewFactory.restoreCommonAttributes()
                attributeViewFactory.restoreCategoryAttributes("restoreLastSearch")
                resetScrollPosition()
                _isRestorePhase = false
            })
    }

    private fun restoreLastSearch(restoreCategoryAttOnly: Boolean) {
        (activity as BaseActivity<ISearchListViewModel>).addDisposable(mViewModel
            .restoreLastSearch()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { category ->
                _isRestorePhase = true
                if (!restoreCategoryAttOnly) {
                    if (_txtSearchText != null) _txtSearchText.setText(mViewModel._searchMaskParams.searchText)
                    rootCategoriesView.initByChildMostCategory(mViewModel._searchMaskParams.category)
                    attributeViewFactory.restoreCommonAttributes()
                    resetScrollPosition()
                }
                attributeViewFactory.restoreCategoryAttributes("restoreLastSearch")
                _isRestorePhase = false
            })
    }

    val isHaveSearchableData: Boolean
        get() {
            val hasText = !TextUtils.isEmpty(mViewModel._searchMaskParams.searchText)
            val hasCategory = mViewModel._searchMaskParams.category != null && mViewModel._searchMaskParams.category!!.id > 0
            val hasCommonAtt = attributeViewFactory.isHaveCommonSearchableData
            return hasText || hasCategory || hasCommonAtt
        }

    private fun resetScrollPosition() {
        if (_focusable != null) _focusable!!.requestFocus()
    }

    interface PostCatAttViewCreatedListener {
        fun onPostCatAttViewCreated(finalCategory: CategoryListItem?)
    }

    private var _postCatAttViewCreatedListener: PostCatAttViewCreatedListener? = null

    companion object {
        @JvmField
		var _isRestorePhase = false
        var _isCategorySelectPhase = false
    }
}
