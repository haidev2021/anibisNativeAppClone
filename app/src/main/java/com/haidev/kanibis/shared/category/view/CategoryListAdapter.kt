package com.haidev.kanibis.shared.category.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AdapterView.OnItemClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.model.CategoryList
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.util.AnibisUtils

class CategoryListAdapter(
    private val _context: Context,
    clickListener: OnItemClickListener,
    btnSearchCount: Button?, eroticEnable: Boolean, rv: RecyclerView, val isBlueFont: Boolean,
    var mViewModel: ICategoryListViewModel
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var _histories: MutableList<CategoryListItem?> = mutableListOf()
    private val _allItems: MutableList<CategoryListItem?> = mutableListOf()
    private var _normals: MutableList<CategoryListItem?> = mutableListOf()
    private var _parents: MutableList<CategoryListItem?> = mutableListOf()
    private var _parentCount = -1
    private var _parentStartPosition = -1
    private var _historyCount = -1
    private var _historyStartPosition = -1
    private var _normalStartPosition = -1
    private var _eroticEnable = false
    var _selectedCat: CategoryListItem? = null
    var _selectedHistoryPosition: Int? = null
    var _clickListener: OnItemClickListener

    private val _layoutInflater: LayoutInflater
    protected var _btnSearchCount: Button?
    var _rv: RecyclerView

    fun update(list: CategoryList, doAnimation: Boolean) {
        _selectedCat = list.tree?.selectedCategories?.lastOrNull()
        initItemsWithAnimation(list, doAnimation)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun initItemsWithAnimation(list: CategoryList, doAnimation: Boolean) {
        _allItems.clear()
        notifyAddHistoryItem(list)
        notifyAddItemAll(list)
        initNormalItems(list)
        notifyDataSetChanged(doAnimation, "initItemsWithAnimation")
    }

    private fun notifyAddHistoryItem(list: CategoryList) {
        if (list.history != null) {
            _histories = list.history.toMutableList()
        }
        _historyStartPosition = 0
        _allItems.addAll(0, _histories)
        _historyCount = _histories.size
        _parentStartPosition = _historyStartPosition + _historyCount
    }

    private fun notifyAddItemAll(list: CategoryList) {
        _parents = list.tree.selectedCategories!!.toMutableList()
        _allItems.addAll(_parents)
        _parentCount = _parents.size
        _normalStartPosition = _parentStartPosition + _parentCount
    }


    private fun initNormalItems(list: CategoryList) {
        _normals = list.tree.children!!.toMutableList()
        _allItems!!.addAll(_normals)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout: Int =
            if (viewType == CATEGORY_HISTORY_WITH_HEADER) R.layout.category_history_with_header
            else if (viewType == CATEGORY_HISTORY_WITH_HEADER_AND_FOOTER) R.layout.category_history_with_header_and_footer
            else if (viewType == CATEGORY_HISTORY_WITH_FOOTER) R.layout.category_history_with_footer
            else if (viewType == CATEGORY_HISTORY) R.layout.category_history
            else if (viewType == CATEGORY_WITH_LOADING_FOOTER) R.layout.category_with_loading_footer
            else if (viewType == CATEGORY_PARENT) R.layout.category_parent
            else R.layout.category_item
        return Holder(
            _layoutInflater.inflate(layout, parent, false),
            _clickListener,
            _deleteHistoryOnClickListener,
            mViewModel
        )
    }

    private val _deleteHistoryOnClickListener: View.OnClickListener =
        object : View.OnClickListener {
            override fun onClick(v: View) {
                hideHistories()
            }
        }

    fun hideHistories() {
    }

    var _searchCounts: Map<Int?, Int> = mapOf();

    @SuppressLint("NotifyDataSetChanged")
    fun updateSearchCount(sc: Map<Int?, Int>){
        _searchCounts= sc;
        notifyDataSetChanged();
    }

    fun isRoot(cat: CategoryListItem?): Boolean {
        return cat == null
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val hld = holder as Holder
        val cat: CategoryListItem? = _allItems!![position]
        val isChecked =
            _selectedHistoryPosition != null && _selectedHistoryPosition == position
                    || _selectedHistoryPosition == null && !isRoot(_selectedCat)
                    && !isRoot(cat) && _selectedCat != null && _selectedCat!!.id === cat!!.id
                    && !isHistory(position) || _selectedHistoryPosition == null
                    && isRoot(_selectedCat) && isRoot(cat)
        hld.ivCheck.setVisibility(if (isChecked) View.VISIBLE else View.GONE)
        val hasChildren = cat == null || cat.hasChildren
        hld.ivExpand.setVisibility(
            if (!isChecked && hasChildren && position >= _parentStartPosition) View.VISIBLE
            else View.GONE)
        if (hld.vSubPadding != null) {
            val hasSubPadding = position >= _normalStartPosition
            hld.vSubPadding!!.visibility =
                if (hasSubPadding) View.VISIBLE else View.GONE
        }
        setCatCount(cat, _searchCounts[cat?.id], hld.catText, isChecked)
        if (hld.catPath != null && cat != null) {
            val catPath = cat.name
            val visible: Boolean = !TextUtils.isEmpty(catPath)
            if (visible) hld.catPath?.setText(catPath)
            hld.catPath?.setVisibility(if (visible) View.VISIBLE else View.GONE)
        }
        doAnimation(hld)
    }

    fun doAnimation(hld: Holder) {
        val position = hld.adapterPosition - _normalStartPosition
        if (_animationEnable && position >= 0) {
            val hyperspaceJumpAnimation: Animation = AnimationUtils.loadAnimation(
                _context, R.anim.slide_in_category
            )
            hyperspaceJumpAnimation.setInterpolator(AnibisUtils.sQuinticInterpolator)
            hld.itemView.startAnimation(hyperspaceJumpAnimation)
        }
    }

    private fun hasDividerAndLoading(position: Int): Boolean {
        return false;
    }

    private fun isParent(position: Int): Boolean {
        return position >= 0 && position <= _normalStartPosition - 1
    }

    fun isHistory(position: Int): Boolean {
        return position >= 0 && position >= _historyStartPosition && position < _historyStartPosition + _historyCount
    }


    override fun getItemViewType(position: Int): Int {
        return if (isHistory(position) && position == 0 && _historyCount == 1)
            CATEGORY_HISTORY_WITH_HEADER_AND_FOOTER else if (isHistory(position) && position == 0
        ) CATEGORY_HISTORY_WITH_HEADER else if (isHistory(position) && position == _historyStartPosition + _historyCount - 1)
            CATEGORY_HISTORY_WITH_FOOTER else if (isHistory(position)
        ) CATEGORY_HISTORY else
            if (isParent(position) && !hasDividerAndLoading(position)) CATEGORY_PARENT else if (hasDividerAndLoading(
                    position
                )
            ) CATEGORY_WITH_LOADING_FOOTER else CATEGORY_ITEM
    }

    fun getItem(position: Int): CategoryListItem? {
        return if (_allItems != null && _allItems.size >= position) _allItems[position] else null
    }

    private fun setCatCount(o: CategoryListItem?, count: Int?, catText: TextView?, isChecked: Boolean) {
        var o: CategoryListItem? = o
        if (catText != null) {
            catText.setTextColor(
                _context.getResources().getColor(
                    if (o != null && o.isErotic() && !_eroticEnable) R.color.banned_category else R.color.normal_category
                )
            )
            val showTextAll: Boolean = TextUtils.isEmpty(o?.name)
            var reducedName: String? = null
            if(o !=null && o.name.contains("/")) {
                reducedName = o.name.split("/").toTypedArray().lastOrNull();
            }
            o?.name?.split(",")?.toTypedArray()

            val name = String.format(
                if (isChecked || !isBlueFont) BLACK_CAT_NAME_PATTERN else BLUE_CAT_NAME_PATTERN,
                java.lang.String.format(
                    if (isChecked) BOLD_CAT_NAME_PATTERN else CAT_NAME_PATTERN,
                    if (showTextAll) mViewModel.translate("apps.home.new.inallcategories")
                    else if (reducedName != null) reducedName else o?.name
                )
            )
            val numberStr =
                if (count != null) AnibisUtils.formatCount(count.toString()) else COUNT_EMPTY
            if (!TextUtils.isEmpty(numberStr)) {
                val namePlusNumber = name + String.format(COUNT_PATTERN, numberStr)
                catText.setText(Html.fromHtml(namePlusNumber), TextView.BufferType.SPANNABLE)
            } else {
                catText.setText(Html.fromHtml(name), TextView.BufferType.SPANNABLE)
            }
        }
    }

    class Holder internal constructor(
        val v: View,
        var clickListener: OnItemClickListener?,
        historyDeleteClick: View.OnClickListener?,
        mViewModel: ICategoryListViewModel,
    ) : RecyclerView.ViewHolder(v) {
        var ivCheck: ImageView
        var ivExpand: ImageView
        var catText: TextView
        var catPath: TextView?
        var tvDeleteHistory: TextView?
        var titleLayout: RelativeLayout?
        var pbLoading: ProgressBar?
        var llCategory: LinearLayout?
        var vSubPadding: View?
        var dummyClickListener = View.OnClickListener { }
        var itemClickListener = View.OnClickListener { handleClick() }

        init {
            catText = v.findViewById<View>(R.id.cattext) as TextView
            catPath = v.findViewById<View>(R.id.catPath) as TextView?
            titleLayout = v.findViewById<View>(R.id.title_layout) as RelativeLayout?
            llCategory = v.findViewById<View>(R.id.llCategory) as LinearLayout?
            ivCheck = v.findViewById<View>(R.id.ivCheck) as ImageView
            ivExpand = v.findViewById<View>(R.id.ivExpand) as ImageView
            pbLoading = v.findViewById<View>(R.id.pbLoading) as ProgressBar?
            vSubPadding = v.findViewById<View>(R.id.vSubPadding) as View?
            tvDeleteHistory = v.findViewById<View>(R.id.tvDeleteHistory) as TextView?

            pbLoading?.let{
                it.setClickable(true)
                it.setOnClickListener(dummyClickListener)
            }

            titleLayout?.let {
                it.setClickable(true)
                it.setOnClickListener(dummyClickListener)
            }
            llCategory?.let {
                it.setClickable(true)
                it.setOnClickListener(itemClickListener)
            }
            tvDeleteHistory?.let {
                it.setClickable(true)
                it.setOnClickListener(historyDeleteClick)
            }

            mViewModel.translateView(v)
        }

        fun handleClick() {
            clickListener?.onItemClick(null, v, getAdapterPosition(), -1)
        }
    }

    private var _animationEnable = true

    init {
        _eroticEnable = eroticEnable
        _layoutInflater =
            _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        _btnSearchCount = btnSearchCount
        _clickListener = clickListener
        _rv = rv
    }

    override fun getItemId(pos: Int): Long {
        val id = if (_allItems[pos] == null) 0 else _allItems[pos]!!.id
        return if (isHistory(pos)) Long.MAX_VALUE - id else id.toLong()
    }

    override fun getItemCount(): Int {
        return _allItems!!.size
    }

    fun setAnimationEnable(enable: Boolean) {
        _animationEnable = enable
    }

    fun notifyDataSetChanged(doAnimation: Boolean, debug: String) {
        setAnimationEnable(doAnimation)
        notifyDataSetChanged()
    }

    companion object {
        private const val CATEGORY_WITH_LOADING_FOOTER = 0
        private const val CATEGORY_PARENT = 1
        private const val CATEGORY_ITEM = 2
        private const val CATEGORY_HISTORY = 3
        private const val CATEGORY_HISTORY_WITH_HEADER = 5
        private const val CATEGORY_HISTORY_WITH_HEADER_AND_FOOTER = 6
        private const val CATEGORY_HISTORY_WITH_FOOTER = 7
        private const val COUNT_PATTERN = "&#160&#160<font color='#9B9B9B'>%s</font>"
        private const val CAT_NAME_PATTERN = "%s"
        private const val BOLD_CAT_NAME_PATTERN = "<b>%s</b>"
        private const val BLACK_CAT_NAME_PATTERN = "<font color='#000000'>%s</font>"
        private const val BLUE_CAT_NAME_PATTERN = "<font color='#3266cc'>%s</font>"
        private const val COUNT_EMPTY = ""
    }
}