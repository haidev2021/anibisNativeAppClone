package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.attribute.controller.BaseAttributeEditorFragment
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.shared.masterdata.category.model.AttributeListItem
import com.haidev.kanibis.shared.masterdata.category.model.AttributeEntryListItem
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class XBAttSelectView(
    context: Context, attribute: AttributeListItem,
    factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
) : ABaseAttributeItemView(
    context, null, factory, viewModel
) {
    val _selectedItems: MutableList<AttributeEntryListItem> = ArrayList()
    var _allItems: List<AttributeEntryListItem?> = ArrayList()
    var _removeAtt: AttributeEntryListItem? = null

    init {
        _attribute = attribute
        if (attribute != null) setLabel(attribute.name)
        this.tag = attribute
    }

    fun updateValue(entry: AttributeEntryListItem?, debug: String) {
        if (entry != null && !_selectedItems.contains(entry)) {
            if (_selectedItems.size > 0) _removeAtt = _selectedItems[0]
            _selectedItems.clear()
            _selectedItems.add(entry)
            onValueChanged(params)
            super.updateUIStringValue(entry.name)
            return
        } else if (entry == null && _selectedItems.size > 0) {
            _removeAtt = _selectedItems[0]
            _selectedItems.clear()
            onValueChanged(params)
            super.updateUIStringValue("")
            return
        }
    }

    override fun onValueClick() {
        _allItems = ArrayList<AttributeEntryListItem?>(_attribute!!.entries)
        if (isSearchLayout) {
            (_allItems as ArrayList<AttributeEntryListItem?>).add(0, null)
        }
        if (_allItems.size == 0 && _attribute!!.getParent() != null) {
            return
        }
        if (!isFragmentShown) {
            val _adapter = AttributeEntryAdapter(
                this@XBAttSelectView.context,
                R.layout.category_item,
                _allItems,
                _selectedItems,
                mViewModel
            )
            val title =
                if (_attribute!!.id != AttributeListItem.getPriceTypeAttributeId()) _attribute!!.name else getLabel()
            val dialog: AlertDialog = AlertDialog.Builder(context)
                .setTitle(title).setIcon(null).setAdapter(_adapter, null)
                .create()
            val list: ListView = dialog.getListView()
            list.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
            if (_attribute!!.isMultiSelectionForSearch()) list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE)
            dialog.setCancelable(true)
            list.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                updateValue(_adapter.getItem(position), "dialog setOnItemClickListener")
                dialog.dismiss()
            }
            dialog.show()
        } else {
            val f = ContentFragment()
            f.setAttributeItemView(this)
            showAttributeFragmentDialog(f)
        }
    }

    class ContentFragment : BaseAttributeEditorFragment<ISearchListViewModel>() {
        override fun createViewModel() {
            (requireActivity() as ListResultActivity).getListResultComponent().inject(this)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
             container: ViewGroup?,
             savedInstanceState: Bundle?
        ): View? {
            val root: View = inflater.inflate(R.layout.fragment_att_selector, container, false)
            super.initViews(root)
            val l = root.findViewById<View>(R.id.lvAttribute) as ListView
            val v = _v as XBAttSelectView
            val _adapter = AttributeEntryAdapter(
                activity,
                R.layout.category_item, v._allItems, v._selectedItems, mViewModel
            )
            l.setAdapter(_adapter)
            l.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                v.updateValue(_adapter.getItem(position), "ContentFragment setOnItemClickListener")
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            return root
        }
    }

    override fun onXClick() {
        updateValue(null, "onXClick")
    }

    class AttributeEntryAdapter(
        context: Context?, textViewResourceId: Int,
        private val _items: List<AttributeEntryListItem?>?,
        private val _selectedItems: List<AttributeEntryListItem>,
        private val mViewModel: IBaseViewModel
    ) : ArrayAdapter<AttributeEntryListItem?>(context!!, textViewResourceId, _items!!) {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var v = convertView
            if (v == null) {
                val vi = this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(R.layout.category_item, null)
            }
            val o = _items!![position]
            var checked = false
            var value: String? = ""
            if (o != null) {
                for (entry in _selectedItems) if (entry != null && o != null && entry.id == o.id) checked =
                    true
                value = o.name
            } else {
                if (_selectedItems.isEmpty()) checked = true else {
                    for (entry in _selectedItems) if (entry == null && o == null) checked = true
                }
                value = mViewModel.translate(TextId.APPS_ALL)
            }
            val catText = v!!.findViewById<View>(R.id.cattext) as TextView
            if (catText != null) {
                catText.text = value
                catText.setTypeface(if (checked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT)
            }
            val ivCheck = v.findViewById<View>(R.id.ivCheck) as ImageView
            ivCheck?.setVisibility(if (checked) VISIBLE else GONE)
            return v
        }

        override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
            return getView(position, convertView, parent)
        }
    }

   override fun onMainValueChanged(params: XBSearchParameters) {
        if (!_selectedItems.isEmpty()) {
            params.attributeFilter.removeAttributeEntry(_removeAtt)
            for (entry in _selectedItems) {
                params.attributeFilter.addAttributeEntry(entry)
            }
        } else {
            params.attributeFilter.removeAttributeEntry(_removeAtt)
        }
    }

    fun checkUpdatingChild() {
        if (_attribute!!.children != null && _attribute!!.children!!.size > 0) factory!!.updateChildGrayOut(
            this.tag as LinearLayout,
            !_selectedItems.isEmpty()
        )
    }

    fun syncValue() {
        if (!SearchMaskViewHolder._isRestorePhase && _attribute!!.id == AttributeListItem.getPriceTypeAttributeId()) {
            for (entry in _selectedItems) {
                Log.v("reloadValue", "setAdvertDetailAttribute entry = " + entry.name)
            }
            onValueChanged(params)
        }
    }

    val selectedItem: AttributeEntryListItem?
        get() = if (!_selectedItems.isEmpty()) _selectedItems[0] else null
}