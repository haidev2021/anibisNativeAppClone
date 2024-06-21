package com.haidev.kanibis.ui.searchList.controller

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.controller.BaseDialogFragment
import com.haidev.kanibis.ui.searchList.view.ABaseCategoryView
import com.haidev.kanibis.ui.searchList.view.SearchMaskViewHolder
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

abstract class ABaseSearchMaskFragment : BaseDialogFragment<ISearchListViewModel>() {

	protected lateinit var _viewHolder: SearchMaskViewHolder
	protected var _btnSearch: Button? = null
	protected var vgSearchLoading: ViewGroup? = null
    protected var vgSearchButton: ViewGroup? = null
    protected var _imgLoading: ImageView? = null
    private var _isPressedSearch = false
    abstract fun processShowResultActivity()
    abstract fun onPostCreateViewCustom(savedInstanceState: Bundle?)
    abstract val categorySelectorViewInstance: ABaseCategoryView
    abstract val resLayoutID: Int

	var _tvTotalCount: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addDisposable(mViewModel._onAttributeListChanged
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list ->
                _viewHolder.createCategoryAttributeViews(list)
            })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val returnIntent = Intent()
        requireActivity().setResult(Activity.RESULT_CANCELED, returnIntent) // init
        return onCreateViewCustom(inflater, container, savedInstanceState)
    }

    open fun onCreateViewCustom(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        var lostQuery: String? = null
        if (savedInstanceState != null
            && savedInstanceState.containsKey("query")
        ) {
            lostQuery = savedInstanceState.getString("query")
        }
        val view: View = inflater.inflate(resLayoutID, container, false)
        mViewModel.initSearchQueries(true, lostQuery)
        _viewHolder = SearchMaskViewHolder(
            requireActivity(), view,
            categorySelectorViewInstance,
            mViewModel
        )
        onPostCreateViewCustom(savedInstanceState)
        _viewHolder.restoreLastSearch()
        _btnSearch = view.findViewById<View>(R.id.btnSearch) as Button
        vgSearchLoading = view.findViewById<View>(R.id.vgSearchLoading) as ViewGroup
        vgSearchButton = view.findViewById<View>(R.id.vgSearchButton) as ViewGroup
        _imgLoading = view.findViewById<View>(R.id.imgLoading) as ImageView

        val frameAnimation: AnimationDrawable = _imgLoading!!.background as AnimationDrawable
        frameAnimation.start()
        return view
    }

    fun processSearch() {
        _isPressedSearch = true
        processShowResultActivity()
    }

    override fun onDestroy() {
        _isRestoreFromBackup = !_isPressedSearch
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, i: MenuInflater) {
        i.inflate(R.menu.search_mask_action, menu)
        super.onCreateOptionsMenu(menu, i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_reset_all) {
            _viewHolder.resetAllClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private var _isRestoreFromBackup = false
    }
}
