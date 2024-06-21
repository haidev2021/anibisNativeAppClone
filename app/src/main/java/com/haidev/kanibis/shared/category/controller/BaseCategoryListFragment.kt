package com.haidev.kanibis.shared.category.controller

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.attribute.controller.BaseAttributeEditorFragment
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.category.view.CategoryListAdapter
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.util.AnibisUtils.isLargeLayout
import com.haidev.kanibis.shared.util.AnibisUtils.openWebLink
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers


abstract class BaseCategoryListFragment<T : ICategoryListViewModel> : BaseAttributeEditorFragment<T>() {

    abstract val layoutId: Int

    interface OnBrowserCategoryListener {
        fun onFinishSelectCategory(selectedCategory: CategoryListItem?): Boolean
        fun onBrowserCategory(selectedCategory: CategoryListItem?): Boolean
    }

    private var _parentView: View? = null
    protected var _tvCategoryTitle: TextView? = null
    protected var _ivCategoryBack: ImageView? = null
    protected var _ivCategorySelect: ImageView? = null
    protected var _llCategoryHeader: ViewGroup? = null
    protected var _btnShowWithCount: Button? = null
    lateinit var _adapter: CategoryListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (_parentView == null) {
            _parentView = inflater.inflate(layoutId, container, false)
            super.initViews(_parentView!!)
            initCategoryViews()
        }
        return _parentView
    }

    override fun onResume() {
        super.onResume()
        addDisposable(mViewModel.mOnSearchCountChanged, {map -> _adapter.updateSearchCount(map)})
    }

    open fun initCategoryViews() {
        _llCategoryHeader = _parentView!!.findViewById<View>(R.id.llCategoryHeader) as ViewGroup
        _tvCategoryTitle = _parentView!!.findViewById<View>(R.id.tvCategoryTitle) as TextView
        _ivCategoryBack = _parentView!!.findViewById<View>(R.id.ivCategoryBack) as ImageView
        _ivCategorySelect = _parentView!!.findViewById<View>(R.id.ivCategorySelect) as ImageView
        _btnShowWithCount = _parentView!!.findViewById<View>(R.id.btnShowWithCount) as Button
        _tvTitle.setText(translate(TextId.APPS_CATEGORY))
        _tvCategoryTitle?.setText(translate(TextId.APPS_CATEGORY))
        initFragmentContent(_parentView, _btnShowWithCount)
        _ivBack.setOnClickListener{ requireActivity().onBackPressedDispatcher.onBackPressed() }
        _ivCategoryBack!!.setOnClickListener {requireActivity().onBackPressedDispatcher.onBackPressed()}
        _llHeader.setVisibility(if (getActivity() is ListResultActivity) View.VISIBLE else View.GONE)
        val searchCatListener =
            View.OnClickListener { _browsingListener!!.onFinishSelectCategory(_adapter._selectedCat) }
        if (_btnShowWithCount != null) _btnShowWithCount!!.setOnClickListener(searchCatListener)
        _ivCategorySelect!!.setOnClickListener(searchCatListener)
    }

    fun setBrowsingListener(listener: OnBrowserCategoryListener) {
        _browsingListener = listener
    }

    var _browsingListener: OnBrowserCategoryListener? = null
    lateinit var _lv: RecyclerView
    protected open fun initFragmentContent(root: View?, btnShowWithCount: Button?) {
        _lv = root!!.findViewById<View>(R.id.lvCategory) as RecyclerView
        initAdapter(btnShowWithCount)
        _lv.setLayoutManager(LinearLayoutManager(getActivity()))
        _adapter?.setHasStableIds(true)
        _lv.setAdapter(_adapter)
        _lv.addOnScrollListener(onScrollListener)
    }

    var _onScrollListener: RecyclerView.OnScrollListener? = null
    val onScrollListener: RecyclerView.OnScrollListener
        get() {
            if (_onScrollListener == null) {
                _onScrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        _adapter?.setAnimationEnable(false)
                    }
                }
            }
            return _onScrollListener!!
        }

    abstract fun getAdapter(btnShowWithCount: Button?
    ): CategoryListAdapter

    private fun initAdapter(btnShowWithCount: Button?) {
        _adapter = getAdapter(btnShowWithCount)
    }

    fun tryShowAdultMessage(category: CategoryListItem?): Boolean {
        if (category != null && category.isErotic()) {
            AlertDialog.Builder(requireActivity())
                .setTitle(category.name)
                .setMessage(translate("apps.message.noerotic"))
                .setPositiveButton(translate("apps.action.openweb"),
                    DialogInterface.OnClickListener { dialog, which ->
                        openWebLink(requireActivity(),
                            translate(if (isLargeLayout(requireActivity())) "apps.links.search.erotic.tablet"
                            else "apps.links.search.erotic.phone"))
                    })
                .setNegativeButton(translate("apps.action.cancel"), { dialog, which -> })
                .show()
            return true
        }
        return false
    }

    var _clickListener: AdapterView.OnItemClickListener = object : AdapterView.OnItemClickListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onItemClick(arg0: AdapterView<*>?, arg1: View, position: Int, arg3: Long) {
            if (position >= 0) {
                val selCat: CategoryListItem? = _adapter.getItem(position)
                if (!tryShowAdultMessage(selCat)) {
                    _adapter._selectedHistoryPosition =
                        if (_adapter.isHistory(position)) position else null
                    if (_adapter.isHistory(position)) {
                        _browsingListener!!.onBrowserCategory(selCat)
                        _adapter.notifyDataSetChanged(false, "_clickListener ")
                        Handler(Looper.getMainLooper()).postDelayed({
                            _browsingListener!!.onFinishSelectCategory(selCat)
                            _adapter._selectedHistoryPosition = null
                        }, 170)
                    } else if (!_adapter.isRoot(selCat) && selCat === _adapter._selectedCat) {
                        _browsingListener!!.onFinishSelectCategory(selCat)
                    } else if (!_adapter.isRoot(selCat) && !selCat!!.hasChildren) {
                        _adapter._selectedCat = selCat
                        _browsingListener!!.onBrowserCategory(selCat)
                        _adapter.notifyDataSetChanged(false, "_clickListener ")
                        Handler(Looper.getMainLooper())
                            .postDelayed(
                                { _browsingListener!!.onFinishSelectCategory(selCat) },
                                170
                            )
                    } else if (_adapter.isRoot(selCat) && selCat === _adapter._selectedCat) {
                        _browsingListener!!.onFinishSelectCategory(selCat)
                    } else {
                        _adapter._selectedCat = selCat
                        onBrowserCategoryBase(selCat?.id, false, false)
                        _browsingListener!!.onBrowserCategory(selCat)
                    }
                }
            }
        }
    }

    fun onBrowserCategoryBase(selectedId: Int?, updateHistory: Boolean, doAnimation: Boolean) {
        addDisposable(mViewModel.getCategoryList(selectedId, updateHistory)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                _adapter.update(list, doAnimation)
            })
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun customDismiss() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
