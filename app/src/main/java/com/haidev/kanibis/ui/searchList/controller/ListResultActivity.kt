package com.haidev.kanibis.ui.searchList.controller

import android.app.SearchManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.transition.ChangeBounds
import android.transition.Transition
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.View.OnTouchListener
import android.view.ViewStub
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.haidev.kanibis.App
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.controller.BaseActivity.ContextMenuController
import com.haidev.kanibis.shared.controller.BaseDialogFragment
import com.haidev.kanibis.shared.controller.SingleFragmentActivity
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.util.AnibisUtils.closeKeyboard
import com.haidev.kanibis.shared.util.AnibisUtils.isFragmentOnStack
import com.haidev.kanibis.shared.util.AnibisUtils.updateRelativeLayoutTopMargin
import com.haidev.kanibis.ui.homeCategoryList.DaggerHomeCategoryListComponent
import com.haidev.kanibis.ui.homeCategoryList.HomeCategoryListComponent
import com.haidev.kanibis.ui.searchList.DaggerSearchResultListComponent
import com.haidev.kanibis.ui.searchList.SearchResultListComponent
import com.haidev.kanibis.ui.searchList.view.ABaseGroupableAttView
import com.haidev.kanibis.ui.searchList.view.ChipCategoryView
import com.haidev.kanibis.ui.searchList.view.KeywordSearchViewHolder
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

class ListResultActivity : SingleFragmentActivity<ISearchListViewModel>(), ContextMenuController,
    IOnCustomBackPressedCallbak, View.OnClickListener, IProcessRefineSearchCallback,
    ABaseGroupableAttView.OnSearchCountParamChangedListener {
    var _headerView: View? = null
    private  var _resultFragment: ListResultFragment? = null
    private var _nestedAppBar: AppBarLayout? = null
    private var _expandSJButtonHeight = 0
    private var _appBarHeight = 0
    private var _headerBarHeight = 0
    var _stateTrace = ArrayList<Int>()

    var mSearchResultListComponent: SearchResultListComponent? = null
    var mHomeCategoryListComponent: HomeCategoryListComponent? = null

    fun getListResultComponent(): SearchResultListComponent {
        if (mSearchResultListComponent == null) {

            mHomeCategoryListComponent = DaggerHomeCategoryListComponent.builder()
                .appComponent((application as App).getAppComponent())
                .build()

            mSearchResultListComponent = DaggerSearchResultListComponent.builder()
                .homeCategoryListComponent(mHomeCategoryListComponent)
                .build()
        }
        return mSearchResultListComponent!!
    }

    override fun createViewModel() {
        getListResultComponent().inject(this)
    }
    
    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
        _stateTrace = ArrayList()
        if (arg0 != null && arg0.containsKey("autoDestroyed")) finish()
        initDimens()
        if (intent.extras != null) {
            _isSaveSearchExecuted = intent.extras!!
                .getInt(
                    "fromType",
                    FROM_KEYWORD_COMMIT
                ) == FROM_SJ_SEARCH
        }
        window.setSharedElementEnterTransition(fasterTransition(200))
        window.setSharedElementReturnTransition(fasterTransition(200))
        initPersistentViews()
        initOptionalViews(intent)
    }

    private fun initDimens() {
        _expandSJButtonHeight =
            getResources().getDimension(R.dimen.button_search_job_expand_height).toInt()
        _appBarHeight = getResources().getDimension(R.dimen.searchview_toolbar_height).toInt()
        _headerBarHeight = getResources().getDimension(R.dimen.search_filter_toolbar_height).toInt()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        initTextSearchBeforeSearchFragmentResume(intent)
        initOptionalViews(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("autoDestroyed", "")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState != null && savedInstanceState.containsKey("autoDestroyed")) finish()
    }

    fun initSearchViewQuery(debug: String?) {
    }

    private fun fasterTransition(duration: Long): Transition? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bounds = ChangeBounds()
            bounds.setDuration(duration)
            return bounds
        }
        return null
    }

    private var _showResultFragment = false
    private var _showSearchView = false
    private var _showShadow = false
    var isShowSearchViewOnly = false
        private set

    private fun initOptionalViews(intent: Intent) {
        if (intent.extras != null) {
            val fromType =
                intent.extras!!.getInt("fromType", FROM_KEYWORD_COMMIT)
            _isSaveSearchExecuted =
                fromType == FROM_SJ_SEARCH
            _stateTrace.add(fromType)
            when (fromType) {
                FROM_HOME_KEYWORD -> {
                    _showResultFragment = false
                    _showSearchView = true
                    _showShadow = true
                    isShowSearchViewOnly = true
                }

                FROM_HOME_CATEGORY, FROM_SEARCH_MASH_APPLY, FROM_SJ_SEARCH, FROM_HOME_LAST_SEARCH, FROM_KEYWORD_COMMIT -> {
                    _showResultFragment = true
                    _showSearchView = true
                    _showShadow = false
                    isShowSearchViewOnly = false
                }

                else -> {}
            }
            if (_showShadow) {
                _vShadow!!.visibility = View.VISIBLE
            }
            if (_showSearchView) {
                initToolbarView()
                initSearchViewQuery("_showSearchView")
            }
            if (_showResultFragment) {
                notifyUpdateHomeLastSearchUI()
                initDrawer()
            }
        }
        intent.removeExtra("fromType")
    }

    private fun notifyUpdateHomeLastSearchUI() {
    }

    private fun notifyDelayCloseKeyboard() {
        Handler(Looper.getMainLooper())
            .postDelayed({ closeKeyboard(this@ListResultActivity) }, 500)
    }

    override fun onSearchCountParamChanged() {
       refineSearchFragment.onSearchCountParamChanged();
    }

    fun initDrawer() {
        if (_rlResultContent == null) {
            _rlResultContent =
                (findViewById<View>(R.id.vsResultContent) as ViewStub).inflate() as RelativeLayout
            _vgFragmentContent = findViewById<View>(R.id.vgFragmentContent) as RelativeLayout
            _headerView = findViewById(R.id.rlHeader)
            mViewModel!!.translateView(_headerView)
            _nestedAppBar = findViewById<View>(R.id.nestedAppBar) as AppBarLayout
            _nestedAppBar!!.bringToFront()
            _tvFilter = findViewById<View>(R.id.tvFilter) as TextView
            _rootCategoriesLayout =
                findViewById<View>(R.id.rootChipCategoriesLayout) as LinearLayout
            _rootCategoriesLayout!!.addView(chipCategoryView)
            _rlButtonsExpanded = findViewById<View>(R.id.rlButtonsExpanded) as RelativeLayout
            _tvFilter!!.setOnClickListener(this)
            buttonSaveSearchExpanded = findViewById<View>(R.id.tvSaveSearchExpanded) as TextView
            buttonGreenSaveSearchExpanded =
                findViewById<View>(R.id.tvGreenSaveSearchExpanded) as TextView
            _tvExpandeds.add(buttonSaveSearchExpanded)
            _tvExpandeds.add(buttonGreenSaveSearchExpanded)
            _vgFragmentContent!!.setBackgroundColor(-0x1)
            if (!isFragmentOnStack(
                    this,
                    resultFragment.javaClass.getSimpleName()
                )
            ) replaceMainFragment(
                resultFragment
            )
        }
    }

    var _chipCategoryView: ChipCategoryView? = null
    val chipCategoryView: ChipCategoryView
        get() {
            if (_chipCategoryView == null) {
                _chipCategoryView = ChipCategoryView(this, mViewModel)
            }
            return _chipCategoryView!!
        }

    private var _refineSearchFragment: RefineSearchFragment? = null
    val refineSearchFragment: RefineSearchFragment
        get() {
            if (_refineSearchFragment == null) _refineSearchFragment = RefineSearchFragment()
            return _refineSearchFragment!!
        }
    val newRefineSearchFragment: RefineSearchFragment
        get() {
            _refineSearchFragment = RefineSearchFragment()
            return _refineSearchFragment!!
        }

    private fun initTextSearchBeforeSearchFragmentResume(intent: Intent) {
        var query = intent.getStringExtra(SearchManager.QUERY)
    }

    private var _toolbar: Toolbar? = null
    private var _appBar: AppBarLayout? = null
    var buttonSaveSearchExpanded: TextView? = null
        private set
    private var _tvFilter: TextView? = null
    private var _rootCategoriesLayout: LinearLayout? = null
    private var _rlButtonsExpanded: RelativeLayout? = null
    var buttonGreenSaveSearchExpanded: TextView? = null
        private set
    private val _tvExpandeds: MutableList<TextView?> = ArrayList()
    private var _searchViewHolder: KeywordSearchViewHolder? = null
    private var _vShadow: View? = null
    private var _ivSearchBarBack: View? = null
    private var _vFocusable: View? = null
    private var _rlRoot: RelativeLayout? = null
    private var _vgFragmentContent: RelativeLayout? = null
    fun initPersistentViews() {
        _rlRoot = findViewById<View>(R.id.rlRoot) as RelativeLayout
        _vFocusable = findViewById(R.id.vFocusable)
        _vShadow = findViewById(R.id.vShadow)
        mViewModel!!.translateView(_rlRoot)
    }

    fun notifyRepopulateCategoryAttributes(debug: String) {
        setRepopulateAttributeFlag(false, debug)
    }

    var _repopulateAttributeFlag = false
    fun setRepopulateAttributeFlag(b: Boolean, debug: String) {
        _repopulateAttributeFlag = b
    }

    var _onSearchViewFocusChangeListener: OnFocusChangeListener = object : OnFocusChangeListener {
        override fun onFocusChange(view: View, focus: Boolean) {
            _queryKeyboardShown = focus
            if (isShowSearchViewOnly) {
                if (!focus) finish()
            } else {
                if (!focus) {
                    initSearchViewQuery("_onSearchViewFocusChangeListener")
                }
                _vShadow!!.visibility = if (focus) View.VISIBLE else View.GONE
            }
            _toolbar!!.setBackgroundColor(getResources().getColor(if (focus) R.color.theme_primary_shadowed else R.color.theme_primary))
            if (focus) {
                _appBar!!.setExpanded(true, true)
            }
        }
    }
    var _onShadowViewTouchListener: OnTouchListener = object : OnTouchListener {
        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                if (isShowSearchViewOnly) {
                    onBackPressedDispatcher.onBackPressed()
                } else {
                    _vShadow!!.visibility = View.GONE
                    clearFocus()
                    notifyDelayCloseKeyboard()
                }
            }
            return true
        }
    }
    var _onAppbarOffsetChangedListener = OnOffsetChangedListener { appBarLayout, verticalOffset ->
        setViewHeight(
            _rlButtonsExpanded, (_appBar!!.height + verticalOffset) *
                    _expandSJButtonHeight / _appBar!!.height
        )
    }
    var _queryKeyboardShown = false
    fun initToolbarView() {
        if (_appBar == null) {
            _appBar = (findViewById<View>(R.id.vsSearchBar) as ViewStub).inflate() as AppBarLayout
            _appBar!!.bringToFront()
            _toolbar = findViewById<View>(R.id.toolbar) as Toolbar
            setSupportActionBar(_toolbar)
            _searchViewHolder = KeywordSearchViewHolder(_toolbar!!, this)
            _ivSearchBarBack = findViewById(R.id.ivSearchBarBack)
            _ivSearchBarBack!!.setOnClickListener(this)
            _searchViewHolder!!.setOnFocusChangeListener(_onSearchViewFocusChangeListener)
            _vShadow!!.setOnTouchListener(_onShadowViewTouchListener)
            _toolbar!!.setContentInsetsAbsolute(0, 0)
            _appBar!!.addOnOffsetChangedListener(_onAppbarOffsetChangedListener)
        }
    }

    fun replaceMainFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.replace(R.id.vgFragmentContent, fragment, fragment.javaClass.getSimpleName())
        transaction.commitAllowingStateLoss()
        fm.executePendingTransactions()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvFilter -> openFilter()
            R.id.ivSearchBarBack -> onBackPressedDispatcher.onBackPressed()
        }
    }

    fun openFilter() {
        showFullScreenDialog(newRefineSearchFragment)
    }

    fun pop(fragmentName: String?) {
        val fm = supportFragmentManager
        if (fragmentName != null) {
            fm.popBackStack(fragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    fun pop(fragment: Fragment?) {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun processRefineSearch() {
        openFilter()
    }

    private fun setViewHeight(v: View?, h: Int) {
        val saveSearchParams = v!!.layoutParams as RelativeLayout.LayoutParams
        saveSearchParams.height = h
        v.setLayoutParams(saveSearchParams)
    }

    private var _rlResultContent: RelativeLayout? = null

    override val mContentFragment: BaseDialogFragment<ISearchListViewModel>?
        get() = null

    override val mLayoutResourceId: Int
        get() = R.layout.activity_search_result

    private val resultFragment: Fragment
        private get() {
            if (_resultFragment == null) _resultFragment = ListResultFragment()
            return _resultFragment!!
        }
    override val mActionBarTitle: String
        get() = ""

    public override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        _vFocusable!!.visibility =
            if (isShowSearchViewOnly) View.GONE else View.VISIBLE
        super.onResume()
    }

    override val isContextMenuShown: Boolean
        get() = false

    override fun onCustomBackPressed(): Boolean {
        return if (isShowSearchViewOnly) {
            finish()
            true
        } else if (_queryKeyboardShown) {
            if (_vFocusable != null) {
                _vFocusable!!.requestFocus()
                _vFocusable!!.requestFocusFromTouch()
            }
            closeKeyboard(this)
            true
        } else {
            false
        }
    }

    fun clearFocus() {
        if (_searchViewHolder != null) _searchViewHolder!!.clearFocus()
    }

    fun setSmallCardMode(b: Boolean) {
    }

    override fun onDestroy() {
        if (_searchViewHolder != null) _searchViewHolder!!.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == LOCATION_SETTING_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Log.v("1512", " onActivityResult: " + " | RESULT_OK. = " + RESULT_OK)
            } else if (resultCode == RESULT_CANCELED) {
                Log.v("1512", " onActivityResult: " + " | RESULT_CANCELED. = " + RESULT_CANCELED)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun showSelectCriteriaMessage() {
    }

    companion object {
        var _isSaveSearchExecuted = false
        const val FROM_KEYWORD_COMMIT = -1
        const val FROM_HOME_KEYWORD = 0
        const val FROM_HOME_CATEGORY = 1
        const val FROM_SEARCH_MASH_APPLY = 2
        const val FROM_SJ_SEARCH = 3
        const val FROM_HOME_LAST_SEARCH = 4
    }
}
