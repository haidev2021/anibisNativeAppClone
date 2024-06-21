package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.attribute.controller.BaseAttributeEditorFragment
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.ui.searchList.controller.ListResultActivity
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel

abstract class XBCommonAttSelectView : ABaseAttributeItemView {
    var _valueMap: HashMap<String?, String?>? = null
    var _valueUIMap: HashMap<String?, String>? = null
    var _valueMapReversed: HashMap<String?, String?>? = null
    val _selectedKeys: MutableList<String?>? = ArrayList()
    var _keyList: List<String?>? = ArrayList()
    private var _adapter: ValueAdapter? = null

    constructor(context: Context, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context, label: String?) : super(context!!, null) {
        setLabel(label!!)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    )

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context, viewModel)

    constructor(
        context: Context?, label: String?,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context!!, null, factory, viewModel) {
        setLabel(label!!)
    }

    fun setContent(
        keyList: List<String?>?,
        valueMap: HashMap<String?, String?>?,
        valueMapReversed: HashMap<String?, String?>?,
        valueUIMap: HashMap<String?, String>?
    ) {
        setContent(keyList, valueMap, valueMapReversed)
        _valueUIMap = valueUIMap
    }

    fun setContent(
        keyList: List<String?>?, valueMap: HashMap<String?, String?>?,
        valueMapReversed: HashMap<String?, String?>?
    ) {
        setContent(keyList, valueMap, valueMapReversed, true)
    }

    fun setContent(
        keyList: List<String?>?, valueMap: HashMap<String?, String?>?,
        valueMapReversed: HashMap<String?, String?>?, cleanupInvalidSelection: Boolean
    ) {
        _keyList = keyList
        _valueMap = valueMap
        _valueMapReversed = valueMapReversed
        if (cleanupInvalidSelection && value != null && !_keyList!!.contains(value)) {
            updateValue(null)
        }
    }

    fun updateValue(entry: String?) {
        var entry = entry
        if (!TextUtils.isEmpty(entry) && !_keyList!!.contains(entry) && _valueMapReversed != null && _valueMapReversed!!.containsKey(
                entry
            )
        ) {
            entry = _valueMapReversed!![entry]
        }
        if (!TextUtils.isEmpty(entry) && _keyList!!.contains(entry) && !_selectedKeys!!.contains(
                entry
            )
        ) {
            _selectedKeys.clear()
            _selectedKeys.add(entry)
            onValueChanged(params)
            updateUIStringValue(makeUIValue(entry))
            if (this is RefineUIListener) (this as RefineUIListener).onRefineUI()
        } else if (TextUtils.isEmpty(entry) && _selectedKeys!!.size > 0) {
            _selectedKeys.clear()
            onValueChanged(params)
            updateUIStringValue(makeUIValue(entry))
            if (this is RefineUIListener) (this as RefineUIListener).onRefineUI()
        }
    }

    val selectedPos: Int
        get() = if (_keyList != null && _selectedKeys != null && _selectedKeys.size > 0 && _keyList!!.contains(
                _selectedKeys[0]
            )
        ) _keyList!!.indexOf(_selectedKeys[0]) else 0

    fun makeUIValue(entry: String?): String? {
        return if (_valueUIMap == null) _valueMap!![entry] else _valueUIMap!![entry]
    }

    override val value: String?
        get() = if (_selectedKeys!!.size > 0) _selectedKeys[0] else null

    override fun onValueClick() {
        if (_keyList!!.size > 0) {
            if (!isFragmentShown) {
                _adapter = ValueAdapter(
                    this@XBCommonAttSelectView.context,
                    R.layout.category_item, this@XBCommonAttSelectView
                )
                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle(this@XBCommonAttSelectView.getLabel()).setIcon(null)
                    .setAdapter(_adapter, null)
                    .create()
                val list: ListView = dialog.getListView()
                list.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
                dialog.setCancelable(true)
                list.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                    updateValue(_adapter!!.getItem(position))
                    dialog.dismiss()
                }
                dialog.show()
                Handler(Looper.getMainLooper()).post(object : Runnable {
                    override fun run() {
                        try {
                            list.setSelection(selectedPos)
                        } catch (e: Exception) {
                        }
                    }
                })
            } else {
                val f = ContentFragment()
                f.setAttributeItemView(this)
                showAttributeFragmentDialog(f)
            }
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
        ): View {
            val root: View = inflater.inflate(R.layout.fragment_att_selector, container, false)
            super.initViews(root)
            val l = root.findViewById<ListView>(R.id.lvAttribute)
            val v = _v as XBCommonAttSelectView
            val _adapter = ValueAdapter(getActivity(), R.layout.category_item, v)
            l.setAdapter(_adapter)
            l.onItemClickListener = OnItemClickListener { parent, view, position, id ->
                v.updateValue(_adapter.getItem(position))
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
            return root
        }
    }

    override fun onXClick() {
        updateValue(null)
    }

    protected class ValueAdapter internal constructor(
        context: Context?,
        textViewResourceId: Int,
        private val view: XBCommonAttSelectView
    ) : ArrayAdapter<String?>(
        context!!, textViewResourceId, view._keyList!!
    ) {
        var footerText: String?

        init {
            footerText = extractFooterFromKeyList()
        }

        fun extractFooterFromKeyList(): String? {
            return if (view._keyList!!.size - view._valueMap!!.size == 1) view._keyList!![view._keyList!!.size - 1] else null
        }

        fun hasFooter(): Boolean {
            return !TextUtils.isEmpty(footerText)
        }

        
        override fun getView(position: Int, convertView: View?,  parent: ViewGroup): View {
            var v = convertView
            val hld: Holder?
            if (v == null) {
                val vi =
                    this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = vi.inflate(
                    if (position == view._keyList!!.size - 1 && hasFooter()) R.layout.category_item_footer else R.layout.category_item,
                    null
                )
                hld = Holder()
                hld.imgCheck = v!!.findViewById<ImageView>(R.id.ivCheck)
                hld.tvLabel = v.findViewById<TextView>(R.id.cattext)
                hld.tvFooter = v.findViewById<TextView>(R.id.tvFooter)
                if (hld.tvFooter != null && hasFooter()) hld.tvFooter!!.text = footerText
                v.setTag(hld)
            } else {
                hld = if (v.tag != null && v.tag is Holder) v.tag as Holder else null
            }
            if (hld != null) {
                val o = view._keyList!![position]
                var checked = false
                if (!TextUtils.isEmpty(o)) {
                    for (entry in view._selectedKeys!!) if (entry == o) checked = true
                } else {
                    if (view._selectedKeys!!.isEmpty()) checked = true else {
                        for (entry in view._selectedKeys) if (TextUtils.isEmpty(entry)) checked =
                            true
                    }
                }
                if (hld.tvLabel != null) {
                    hld.tvLabel!!.text = view._valueMap!![o]
                    hld.tvLabel!!.setTypeface(if (checked) Typeface.DEFAULT_BOLD else Typeface.DEFAULT)
                }
                if (hld.imgCheck != null) {
                    hld.imgCheck!!.setVisibility(if (checked) VISIBLE else GONE)
                }
                if (hld.tvFooter != null) {
                    hld.tvFooter!!.visibility =
                        if (position == view._keyList!!.size - 1) VISIBLE else GONE
                }
            }
            return v!!
        }

        override fun getDropDownView(
            position: Int,
            convertView: View,
             parent: ViewGroup
        ): View {
            return getView(position, convertView, parent)
        }

        private class Holder {
            var imgCheck: ImageView? = null
            var tvLabel: TextView? = null
            var tvFooter: TextView? = null
        }
    }

    interface RefineUIListener {
        fun onRefineUI()
    }

    override val isEmpty: Boolean
        get() = value == null
}