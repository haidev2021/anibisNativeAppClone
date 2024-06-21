package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.category.model.CategoryListItem
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import java.util.AbstractMap
import java.util.TreeMap
import java.util.TreeSet

class CategoryAttributeViewFactory(context: Context, var mViewModel: ISearchListViewModel) {
    var _context: Context
    val _categoryAttViews: MutableList<LinearLayout?> = ArrayList()
    val _commonAttViews: MutableList<LinearLayout> = ArrayList()
    var _priceView: ABaseAttributeItemView? = null
    var _priceTypeView: ABaseAttributeItemView? = null
    lateinit var _stateCode: XBAttStateView
    val OFFER_ENTRY: AttributeEntryListItem? =
    AttributeEntryListItem.getOrCreateAttributeEntry(AttributeEntryListItem.ATTRIBUTE_ENTRY_ID_OFFER)

    fun createCategoryAttributeViewsInLayout(layout: LinearLayout?, list: List<AttributeListItem>) {
        if (layout == null) return
        var insertionAdvertTypeView: XBAttSelectView? = null
        var isHaveInsertionAdvertType = false
        resetSpecViewList()
        layout.removeAllViews()
        if (mViewModel._searchMaskInfo.category != null &&
            mViewModel._searchMaskInfo.category!!.id != 0 &&
            mViewModel._searchMaskInfo.category!!.id != -1) {
          for (catAtt in list) {
                if (catAtt.isSearchable == 1 || !mViewModel._searchMaskInfo.isSearch) {
                    var shown = isShownCaAttViews(mViewModel._searchMaskInfo.category)
                    if (catAtt.isPriceAttribute()) {
                    } else if (!mViewModel._searchMaskInfo.isSearch && catAtt.isPriceTypeAttribute()) {
                    } else if (isShownCaAttViews(mViewModel._searchMaskInfo.category)) {
                        val newAttView = getView(catAtt)
                        notifyViewDepending(catAtt, newAttView, layout)
                        _categoryAttViews.add(newAttView)
                        if (!mViewModel._searchMaskInfo.isSearch && catAtt.id === AttributeListItem.ATTRIBUTE_ID_ADVERT_TYPE &&
                            newAttView is XBAttSelectView
                        ) {
                            insertionAdvertTypeView = newAttView
                            isHaveInsertionAdvertType = true
                        }
                    } else if (mViewModel._searchMaskInfo._hasPrice && mViewModel._searchMaskInfo._hasPriceType) {
                        break
                    }
                }
            }
        }
        setPriceViewVisibility(mViewModel._searchMaskInfo._hasPrice)
        setPriceTypeViewVisibility(mViewModel._searchMaskInfo._hasPriceType)
        if (!SearchMaskViewHolder._isRestorePhase) {
            SearchMaskViewHolder._isCategorySelectPhase = true
            if (!mViewModel._searchMaskInfo.isSearch) {
                if (mViewModel._searchMaskInfo._hasPriceType) (getPriceTypeView(false) as XBAttSelectView?)!!.syncValue()
                if (isHaveInsertionAdvertType) insertionAdvertTypeView!!.updateValue(
                    OFFER_ENTRY,
                    "OFFER_ENTRY"
                )
            } else {
                if (mViewModel._searchMaskInfo._hasPrice) {
                    (getPriceView(false) as XBAttRangeView?)!!.syncValue()
                } else {
                    if (_isLastCategoryHadGratis) {
                    }
                }
                _isLastCategoryHadGratis = mViewModel._searchMaskInfo._hasPrice
            }
            for (view in _categoryAttViews) {
                if (view is XBAttSelectView) {
                    view.checkUpdatingChild()
                } else if (view is XBAttCheckView) {
                    view.checkUpdatingChild()
                } else if (view is XBAttDateView) {
                    view.checkUpdatingChild()
                } else if (view is XBAttInputView) {
                    view.checkUpdatingChild()
                } else if (view is XBAttRangeView) {
                    view.checkUpdatingChild()
                }
            }
            SearchMaskViewHolder._isCategorySelectPhase = false
        }
        layout.visibility = if (isShownCaAttViews(mViewModel._searchMaskInfo.category)) View.VISIBLE else View.GONE
        layout.visibility = if (layout.childCount > 0) View.VISIBLE else View.GONE
    }

    fun isShownCaAttViews(category: CategoryListItem?): Boolean {
        return mViewModel._searchMaskInfo.isSearch || category != null && !category.hasChildren
    }

    fun notifyViewDepending(
        att: AttributeListItem,
        currentView: ABaseGroupableAttView?,
        root: LinearLayout
    ) {
        var parent: ABaseGroupableAttView? = null
        var child: ABaseGroupableAttView? = null
        var found: ABaseGroupableAttView? = null
        for (v in _categoryAttViews) {
            if (v!!.tag is XBAttribute) {
                if (att.getParent() != null
                    && att.getParent()!!.id === (v.tag as XBAttribute).id
                ) {
                    parent = v as ABaseGroupableAttView?
                    child = currentView
                } else if (att.children != null && att.children!!.size > 0 && att.children!![0].id == (v.tag as XBAttribute).id) {
                    parent = currentView
                    child = v as ABaseGroupableAttView?
                }
                if (parent != null && child != null) {
                    found = v as ABaseGroupableAttView?
                    break
                }
            }
        }
        if (found != null) {
            parent!!.tag = child
            if (!mViewModel._searchMaskInfo.isSearch && parent._attribute != null &&
                AnibisUtils.isDuoGrouped(parent._attribute!!.id)
            ) {
                makeDuoLayout(root, found, child, parent)
            } else {
                root.addView(currentView)
            }
        } else {
            root.addView(currentView)
        }
    }

    fun makeDuoLayout(
        root: LinearLayout, found: ABaseGroupableAttView?,
        child: ABaseGroupableAttView?, parent: ABaseGroupableAttView?
    ): View {
        val pos = root.indexOfChild(found)
        root.removeView(found)
        val duoLayout = LayoutInflater.from(_context)
            .inflate(R.layout.item_insertion_price_duo, null) as LinearLayout //
        val parentHolder = duoLayout.findViewById<View>(R.id.flParentHolder) as FrameLayout
        parentHolder.addView(parent)
        parent!!.setPaddingDuo(ABaseGroupableAttView.LayoutType.DuoLeft)
        val childHolder = duoLayout.findViewById<View>(R.id.flChildHolder) as FrameLayout
        childHolder.addView(child)
        childHolder.tag = child
        child!!.setPaddingDuo(ABaseGroupableAttView.LayoutType.DuoRight)
        handleDependingDefinedByApp(parent, child, duoLayout)
        parent._childView = child
        parent._duoView = duoLayout
        root.addView(duoLayout, pos)
        duoLayout.tag = childHolder
        return duoLayout
    }

    fun handleDependingDefinedByApp(parent: View, child: View, duoView: View) {
        if (!mViewModel._searchMaskInfo.isSearch) {
            var foundView: ABaseGroupableAttView? = null
            var foundEntry: AttributeEntryListItem? = null
            if (parent is XBAttSelectView) {
                foundView = parent
                foundEntry = parent.selectedItem
            } else if (parent is XBAttCheckView) {
                foundView = parent
                foundEntry = parent.selectedItem
            }
            if (foundView != null) {
                val childHolder =
                    if (duoView.tag is FrameLayout) duoView.tag as View else duoView.findViewById<View>(
                        R.id.flChildHolder
                    )
                val oldVisible = childHolder.visibility == View.VISIBLE
                childHolder.visibility = if (AnibisUtils.isChildAttIgnored(
                        foundView._attribute!!.id, foundEntry
                    )
                ) View.GONE else View.VISIBLE
                foundView.setPaddingDuo(if (childHolder.visibility == View.GONE) ABaseGroupableAttView.LayoutType.Single else ABaseGroupableAttView.LayoutType.DuoLeft)
                notifySyncChildVIew(childHolder, oldVisible)
            }
        }
    }

    fun notifySyncChildVIew(childHolder: View, oldVisible: Boolean) {
        if (childHolder.visibility == View.VISIBLE && !oldVisible) {
            val tag = childHolder.tag
        }
    }

    fun resetSpecViewList() {
        mViewModel._searchMaskInfo._hasPrice = true
        resetSpecViewErrors()
        _categoryAttViews.clear()
        _categoryAttViews.add(getPriceView(false))
        if (!mViewModel._searchMaskInfo.isSearch) {
            mViewModel._searchMaskInfo._hasPriceType = true
            _categoryAttViews.add(getPriceTypeView(false))
        }
    }

    fun resetSpecViewErrors() {
    }

    fun getPriceView(reset: Boolean): ABaseAttributeItemView? {
        try {
            if (_priceView == null || reset) {
               _priceView = XBAttRangeView(
                    _context,
                    mViewModel.getOrCreatePriceAttribute(), this, mViewModel
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _priceView
    }

    fun getPriceTypeView(reset: Boolean): ABaseAttributeItemView? {
        try {
            if (_priceTypeView == null || reset) {
                _priceTypeView = object : XBAttSelectView(
                    _context,
                    AttributeListItem.getOrCreateAttribute(AttributeListItem.getPriceTypeAttributeId()), this@CategoryAttributeViewFactory, mViewModel
                ) {}
                _priceTypeView!!.setLabel(mViewModel.translate(TextId.APPS_PRICETYPE)!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return _priceTypeView
    }

    private fun setPriceViewVisibility(visible: Boolean) {
        getPriceView(false)!!.visibility = if (visible) View.VISIBLE else View.GONE
        if (!mViewModel._searchMaskInfo.isSearch) {
            _priceDuoLayout!!.visibility = getPriceView(false)!!.visibility
        }
    }

    private fun setPriceTypeViewVisibility(visible: Boolean) {
        getPriceTypeView(false)!!.visibility =
            if (visible) View.VISIBLE else View.GONE
        if (!mViewModel._searchMaskInfo.isSearch) {
            _priceDuoLayout!!.visibility = getPriceTypeView(false)!!.visibility
        }
    }
    var _priceDuoLayout: View? = null
    fun resolvePriceDuoPositions(root: LinearLayout) {
        val newPriceView = getPriceView(false)
        val newPriceTypeView = getPriceTypeView(false)
        root.addView(newPriceView)
        if (!mViewModel._searchMaskInfo.isSearch) {
            _priceDuoLayout = makeDuoLayout(root, newPriceView, newPriceView, newPriceTypeView)
        }
    }

    
    fun createCommonAttributeViewsInLayout(
        layout: LinearLayout?,
        priceLayout: LinearLayout? = null, languageLayout: LinearLayout? = null
    ) {
        if (layout == null) return
        resetSpecViewList()
        layout.removeAllViews()
        _commonAttViews.clear()

        resolvePriceDuoPositions(priceLayout ?: layout)

        _stateCode = XBAttStateView(_context, mViewModel.translate("apps.state"), this, mViewModel)
        layout.addView(_stateCode)
        _commonAttViews.add(_stateCode!!)
        _stateCode!!.visibility = if (mViewModel._searchMaskInfo.isSearch) View.VISIBLE else View.GONE
    }

    fun restoreCommonAttributes(): Boolean {
        return if (!mViewModel._searchMaskInfo.isSearch) false else try {
            if (SearchMaskViewHolder._isCategorySelectPhase) {
            } else if (SearchMaskViewHolder._isRestorePhase) {
                _stateCode.updateValue(
                    if (!TextUtils.isEmpty(mViewModel._searchMaskParams.stateCode))
                        mViewModel._searchMaskParams.stateCode?.uppercase()
                    else mViewModel._searchMaskParams.stateCode)
            }
            isHaveCommonData
        } catch (e: Exception) {
            false
        }
    }

    val isHaveCommonData: Boolean
        get() =(_stateCode.isHaveData)

    private var _isLastCategoryHadGratis = true

    init {
        _context = context
    }

    fun restoreCategoryAttributes(debug: String): Boolean {
        var isHaveData = false
        try {
            val attributeIdsTree: TreeMap<Int, TreeSet<Int>> = TreeMap()
            val attributeTextsList: List<AbstractMap.SimpleEntry<Int, String?>> = listOf()
            val attributeRangesList: List<AbstractMap.SimpleEntry<Int, AbstractMap.SimpleEntry<Double?, Double?>>> =listOf()
            var isMatched: Boolean
            for (view in _categoryAttViews) {
                isMatched = false
                if (view is XBAttSelectView) {
                    val v = view
                    val tree =
                        if (attributeIdsTree.size > 0) attributeIdsTree[v._attribute!!.id] else null
                    isMatched = tree != null && tree.size > 0
                    if (isMatched) {
                        try {
                            v.updateValue(
                                AttributeEntryListItem.getOrCreateAttributeEntry(tree!!.first()),
                                "restoreCategoryAttributes"
                            )
                            isHaveData = true
                        } catch (e1: Exception) {
                            e1.printStackTrace()
                        }
                    } else {
                        v.updateValue(null, "restoreCategoryAttributes")
                    }
                    if (v._attribute!!.children != null && v._attribute!!.children!!.size > 0) v.checkUpdatingChild()
                } else if (view is XBAttCheckView) {
                    val v = view
                    val tree =
                        if (attributeIdsTree.size > 0) attributeIdsTree[v._attribute!!.id] else null
                    isMatched = tree != null && tree.size > 0
                    if (isMatched) {
                        v.updateValue(true)
                        isHaveData = true
                    } else {
                        v.updateValue(false)
                    }
                    if (v._attribute!!.children != null && v._attribute!!.children!!.size > 0) v.checkUpdatingChild()
                } else if (view is XBAttDateView) {
                    val v = view
                    for ((key, value) in attributeTextsList) {
                        if (key == v._attribute!!.id) {
                            v.updateValue(AnibisUtils.getDateFromISO8601String(value))
                            isHaveData = true
                            isMatched = true
                            break
                        }
                    }
                    if (!isMatched) {
                        v.updateValue(null)
                    }
                    if (v._attribute!!.children != null && v._attribute!!.children!!.size > 0) v.checkUpdatingChild()
                } else if (view is XBAttInputView) {
                    val v = view
                    for ((key, value) in attributeTextsList) {
                        if (key === v._attribute!!.getDefaultSelectItemId()) {
                            v.updateValue(value)
                            isHaveData = true
                            isMatched = true
                            break
                        }
                    }
                    if (!isMatched) v.updateValue(null)
                    if (v._attribute!!.children != null && v._attribute!!.children!!.size > 0) v.checkUpdatingChild()
                } else if (view is XBAttRangeView) {
                    val v = view
                    for ((key, value) in attributeRangesList) {
                        if (key === v._attribute!!.numericRangeId) {
                            v.updateValue(value.key, value.value)
                            isHaveData = true
                            isMatched = true
                            break
                        }
                    }
                    if (!isMatched) {
                        v.updateValue(null, null)
                    }
                    if (v._attribute!!.children != null && v._attribute!!.children!!.size > 0) v.checkUpdatingChild()
                }
            }
        } catch (e: Exception) {
            return false
        }
        return isHaveData
    }

    fun getView(attribute: AttributeListItem): ABaseGroupableAttView? {
        if (attribute.isNumericInputType() && mViewModel._searchMaskInfo.isSearch) {
            val view = XBAttRangeView(_context, attribute, this, mViewModel)
            view.tag = attribute
            return view
        }else if (attribute.isDateInputType()) {
            val view: XBAttDateView = object : XBAttDateView(_context, attribute,
                this@CategoryAttributeViewFactory, mViewModel) {}
            view.tag = attribute
            return view
        } else if (attribute.isInputType()) {
            val view: XBAttInputView = object : XBAttInputView(_context, attribute,
                this@CategoryAttributeViewFactory, mViewModel) {}
            view.tag = attribute
            return view
        } else if (attribute.isSelectType()) {
            return if (attribute.type == AttributeListItem.XBAttributeType.Checkmark.ordinal) {
                val view: XBAttCheckView = object : XBAttCheckView(_context, attribute,
                    this@CategoryAttributeViewFactory, mViewModel) {}
                view.tag = attribute
                view
            } else {
                val view: XBAttSelectView = object : XBAttSelectView(_context, attribute,
                    this@CategoryAttributeViewFactory, mViewModel) {}
                view.tag = attribute
                view
            }
        }
        return null
    }

    val isHaveCommonSearchableData: Boolean
        get() {
            return (_stateCode!!.isHaveData || mViewModel._searchMaskInfo._hasPrice
                    && getPriceView(false)!!.isHaveData)
        }

    fun handleDepending() {
        SearchMaskViewHolder._isRestorePhase = true
        restoreCategoryAttributes("handleDepending")
        SearchMaskViewHolder._isRestorePhase = false
    }

    fun updateChildGrayOut(child: LinearLayout?, isParentHaveData: Boolean) {
        if (child != null
            && (child is XBAttCheckView
                    || child is XBAttInputView
                    || child is XBAttDateView
                    || child is XBAttRangeView
                    || child is XBAttSelectView)
        ) {
            if (child is ABaseAttributeItemView) {
                child.setEditable(isParentHaveData)
            }
        }
    }
}
