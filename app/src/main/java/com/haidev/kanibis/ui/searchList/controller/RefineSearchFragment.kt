package com.haidev.kanibis.ui.searchList.controller

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.util.SystemHelper
import com.haidev.kanibis.ui.searchList.view.ABaseCategoryView
import com.haidev.kanibis.ui.searchList.view.RefineSearchCategoryView
import com.haidev.kanibis.ui.searchList.view.VerticalScrollView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.math.abs

class RefineSearchFragment : ABaseSearchMaskFragment(), View.OnClickListener {
    private lateinit var _tvTitle: TextView
    private lateinit var _ivBack: ImageView
    private lateinit var _rlModeSwitch: RelativeLayout
    private lateinit var _rgCard: RadioGroup
    private lateinit var _rbSmallCard: RadioButton
    private lateinit var _rbBigCard: RadioButton
    private lateinit var _svMain: VerticalScrollView
    private lateinit var activity: Activity
    private var _root: View? = null
    override fun onCreateViewCustom(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_root == null) {
            _root = super.onCreateViewCustom(inflater, container, savedInstanceState)!!
            _tvTitle = _root!!.findViewById<TextView>(R.id.tvTitle)
            if (_tvTitle != null) {
                _tvTitle!!.setOnClickListener { if (_ivBack != null) _ivBack!!.performClick() }
            }
            _ivBack = _root!!.findViewById<ImageView>(R.id.ivBack)
            _ivBack.setOnClickListener{
                customDismiss()
            }
            _tvTitle.setText(mViewModel.translate("apps.filter"))
            _btnSearch!!.setOnClickListener { closeSearchMaskAndSearch() }
            _tvTotalCount = _tvTitle
            _rlModeSwitch = _root!!.findViewById<RelativeLayout>(R.id.rlModeSwitch)
            _rgCard = _root!!.findViewById<RadioGroup>(R.id.rgCard)
            _rbSmallCard = _root!!.findViewById<RadioButton>(R.id.rbSmallCard)
            _rbBigCard = _root!!.findViewById<RadioButton>(R.id.rbBigCard)
            val smallCardChecked: Boolean = AnibisUtils.getIsSmallCardMode(requireActivity())
            _rbSmallCard.setChecked(smallCardChecked)
            _rbBigCard.setChecked(!smallCardChecked)
            _rgCard.setOnCheckedChangeListener(_onCardModeChangedListener)
            _rlModeSwitch.setOnClickListener(this)
            _svMain = _root!!.findViewById(R.id.svMain)
            _svMain.setOnScrollChangedListener(onScrollListener)
            if (rootVisibility != null) _root!!.visibility = rootVisibility!!
        }
        return _root
    }

    private fun customDismiss() {
        (requireActivity() as ListResultActivity?)!!.pop(this)
    }

    private var rootVisibility: Int? = null

    val KB_HIDE_SCROLL_DIS_THRESHOLD = 5
    var inputMethodManger: InputMethodManager? = null
    private var _onScrollListener: VerticalScrollView.OnScrollChangedListener? = null
    private val onScrollListener: VerticalScrollView.OnScrollChangedListener
        private get() {
            if (_onScrollListener == null) {
                _onScrollListener = object : VerticalScrollView.OnScrollChangedListener {
                    override fun onScrollChanged(l: Int, scrollY: Int, oldl: Int, oldScrollY: Int) {
                        val dy = SystemHelper.convertPixelsToDp(
                            oldScrollY - scrollY,
                            requireActivity()
                        )
                        inputMethodManger =
                            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        if (inputMethodManger != null && inputMethodManger!!.isAcceptingText() && abs(
                                dy.toDouble()
                            ) > KB_HIDE_SCROLL_DIS_THRESHOLD
                        ) {
                            inputMethodManger!!.hideSoftInputFromWindow(
                                requireActivity()!!.currentFocus!!.windowToken, 0
                            )
                        }
                    }
                }
            }
            return _onScrollListener as VerticalScrollView.OnScrollChangedListener
        }
    var _onCardModeChangedListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
        (requireActivity() as ListResultActivity?)!!.setSmallCardMode(checkedId == R.id.rbSmallCard)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rlModeSwitch -> _rgCard.check(if (!_rbBigCard!!.isChecked) R.id.rbBigCard else R.id.rbSmallCard)
            else -> {}
        }
    }

    fun closeSearchMaskAndSearch() {
       if (_viewHolder!!.isHaveSearchableData) {
            processSearch()
            (activity as ListResultActivity?)!!.pop(this)
        } else {
            (requireActivity() as ListResultActivity?)!!.showSelectCriteriaMessage()
        }
        
    }

    override fun processShowResultActivity() {
        closeFilterAndExecuteSearch()
    }

    private fun closeFilterAndExecuteSearch() {
    }

    override fun onPostCreateViewCustom(savedInstanceState: Bundle?) {
        updateSearchTextVisibility()
    }

    private fun updateSearchTextVisibility() {
        _viewHolder!!.hideSearchTextVisibility()
    }

    var _refineSearchCategoryView: RefineSearchCategoryView? = null
    override val categorySelectorViewInstance: ABaseCategoryView
        get() {
            if (_refineSearchCategoryView == null) {
                _refineSearchCategoryView = RefineSearchCategoryView(requireActivity(), mViewModel)
            }
            return _refineSearchCategoryView as RefineSearchCategoryView
        }
    override val resLayoutID: Int
        get() = R.layout.fragment_search_mask_refine

    override fun onCreateOptionsMenu(menu: Menu, i: MenuInflater) {}

    override fun createViewModel() {
        (requireActivity() as ListResultActivity).getListResultComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = requireActivity()
    }

    override fun onResume() {
        super.onResume()
        fetchSearchCount()
    }

     fun onSearchCountParamChanged() {
         fetchSearchCount()
     }

     private fun fetchSearchCount() {
         addDisposable(mViewModel.getSearchCountByParams().subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe({list ->
                 updateAttachedButtonLabel(false,
                     list.filter { item ->  item.id == mViewModel._searchMaskParams.category?.id}.firstOrNull()?.count)

             }, {}))
         updateAttachedButtonLabel(true, null);
     }

    private fun updateAttachedButtonLabel(isLoading: Boolean, searchCount: Int?) {
        if (isLoading) {
			vgSearchLoading?.setVisibility(View.VISIBLE);
		}else{
			vgSearchLoading?.setVisibility(View.GONE);
			_btnSearch?.setText(AnibisUtils.getOpenResultByCountText(
                searchCount, mViewModel));
		}
    }
}
