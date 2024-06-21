package com.haidev.kanibis.ui.main.controllers

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.ui.main.views.ABaseFragmentViewHolder
import com.haidev.kanibis.ui.main.views.DashboardHeaderViewHolder
import com.haidev.kanibis.ui.main.views.KanibisFragmentViewHolder
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity


class TabDashboardFragment : Fragment()
{
    private lateinit var _adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private val _fragmentViewHolders: MutableList<KanibisFragmentViewHolder?> = ArrayList()

    private lateinit var _rv: RecyclerView
    private var _openSearchFlag = false
    var VIEW_TYPE_HEADER = 0
    var VIEW_TYPE_GALLERY = 1
    var VIEW_TYPE_FAVORITE_NO_GALLERY = 1
    var VIEW_TYPE_FAVORITE = 2
    var VIEW_TYPE_LAST_SEARCH_NO_GALLERY = 2
    var VIEW_TYPE_LAST_SEARCH = 3
    var VIEW_TYPE_HISTORY_NO_GALLERY = 3
    var VIEW_TYPE_HISTORY = 4
    var VIEW_TYPE_TOTAL_NO_GALLERY = 4
    var VIEW_TYPE_TOTAL = 5

    override fun onResume() {
        super.onResume()
        _adapter.notifyDataSetChanged()
        notifyResetScrollPosition()
    }

    fun notifyResetScrollPosition() {
        if (_openSearchFlag) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (_rv != null) _rv.smoothScrollBy(
                    0, -_rv.computeVerticalScrollOffset(),
                    AnibisUtils.sQuinticInterpolator
                )
            }, 100)
            _openSearchFlag = false
        }
    }

    fun focusSearchBox(topScroll: Int) {
        _rv.smoothScrollBy(
            0,
            topScroll - _rv.computeVerticalScrollOffset(),
            AnibisUtils.sQuinticInterpolator
        )
        _openSearchFlag = true
    }

    var _onScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        var currState: Int = RecyclerView.SCROLL_STATE_IDLE
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (_openSearchFlag && newState == RecyclerView.SCROLL_STATE_IDLE && currState != newState)
                    showKeywordResultActivity()
            currState = newState
        }
    }

    fun showKeywordResultActivity() {
        val myIntent = Intent(requireActivity(), ListResultActivity::class.java)
        myIntent.putExtra("fromType", ListResultActivity.FROM_HOME_KEYWORD)
        this.startActivity(myIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.tabs_fragment_dashboard, container, false)
        _rv = view.findViewById(R.id.rvContent)
        _rv.addOnScrollListener(_onScrollListener)
        _rv.setLayoutManager(
            AnibisUtils.getDisableAutoScrollLinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false
            )
        )
        _adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(vg: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                var vh: RecyclerView.ViewHolder? = null
                val v: View =
                    LayoutInflater.from(activity).inflate(R.layout.fragment_holder, vg, false)
                if (isHeader(viewType)) {
                    vh = DashboardHeaderViewHolder(v, this@TabDashboardFragment)
                }
                _fragmentViewHolders.add(vh as KanibisFragmentViewHolder?)
                return vh!!
            }

            override fun getItemCount(): Int {
                return 1;
            }

            fun isHeader(viewType: Int): Boolean {
                return viewType == VIEW_TYPE_HEADER
            }

            fun isGallery(viewType: Int): Boolean {
                return viewType == VIEW_TYPE_GALLERY && itemCount == VIEW_TYPE_TOTAL
            }

            fun isLastSearch(viewType: Int): Boolean {
                return viewType == VIEW_TYPE_LAST_SEARCH_NO_GALLERY && itemCount == VIEW_TYPE_TOTAL_NO_GALLERY ||
                        viewType == VIEW_TYPE_LAST_SEARCH && itemCount == VIEW_TYPE_TOTAL
            }

            fun isHistory(viewType: Int): Boolean {
                return viewType == VIEW_TYPE_HISTORY_NO_GALLERY && itemCount == VIEW_TYPE_TOTAL_NO_GALLERY ||
                        viewType == VIEW_TYPE_HISTORY && itemCount == VIEW_TYPE_TOTAL
            }

            fun isFavorite(viewType: Int): Boolean {
                return viewType == VIEW_TYPE_FAVORITE_NO_GALLERY && itemCount == VIEW_TYPE_TOTAL_NO_GALLERY ||
                        viewType == VIEW_TYPE_FAVORITE && itemCount == VIEW_TYPE_TOTAL
            }

            override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, postion: Int) {
                if (viewHolder is ABaseFragmentViewHolder) (viewHolder as ABaseFragmentViewHolder).update()
            }
        }
        _rv.setAdapter(_adapter)
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        for (f in _fragmentViewHolders) {
            if (f != null) {
                f.onDestroy()
            }
        }
    }
}
