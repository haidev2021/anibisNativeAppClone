package com.haidev.kanibis.ui.searchList.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import ch.xmedia.mobile.xbasesdk.model.XBSearchParameters
import com.haidev.kanibis.shared.localization.model.TextId
import com.haidev.kanibis.ui.searchList.view.XBCommonAttSelectView.RefineUIListener
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import java.util.Locale

class XBAttStateView : XBCommonAttSelectView, RefineUIListener {
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initItems()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initItems()
    }

    constructor(context: Context, viewModel: ISearchListViewModel) : super(context, viewModel) {
        initItems()
    }

    constructor(
        context: Context?, label: String?,
        factory: CategoryAttributeViewFactory?, viewModel: ISearchListViewModel
    ) : super(context, label, factory, viewModel) {
        initItems()
    }

    private fun initItems() {
        val _valueMap = HashMap<String?, String?>()
        val _valueMapReversed = HashMap<String?, String?>()
        val _valueList: MutableList<String?> = ArrayList()
        val allLabel = mViewModel.translate(TextId.APPS_ALL)
        _valueList.add(ALL_VALUE)
        _valueMap[ALL_VALUE] = allLabel
        _valueMapReversed[allLabel] = ALL_VALUE
        for (s in APPS_CANTON_ARRAY) {
            val code = s.replace(CANTON_PREFIX, "").uppercase(Locale.getDefault())
            val name: String = mViewModel.translate(s)!!
            _valueList.add(code)
            _valueMap[code] = name
            _valueMapReversed[name] = code
        }
        setContent(_valueList, _valueMap, _valueMapReversed)
    }

    override fun onMainValueChanged(params: XBSearchParameters) {
        params.stateCode = value
    }

    override fun onRefineUI() {
        if (value == null) updateUIStringValue("")
    }

    companion object {
        const val ALL_VALUE = ""
        val APPS_CANTON_ARRAY = arrayOf(
            "apps.canton.ag",
            "apps.canton.ar",
            "apps.canton.ai",
            "apps.canton.bl",
            "apps.canton.bs",
            "apps.canton.be",
            "apps.canton.fr",
            "apps.canton.ge",
            "apps.canton.gl",
            "apps.canton.gr",
            "apps.canton.ju",
            "apps.canton.lu",
            "apps.canton.ne",
            "apps.canton.nw",
            "apps.canton.ow",
            "apps.canton.sh",
            "apps.canton.sz",
            "apps.canton.so",
            "apps.canton.sg",
            "apps.canton.ti",
            "apps.canton.tg",
            "apps.canton.ur",
            "apps.canton.vd",
            "apps.canton.vs",
            "apps.canton.zg",
            "apps.canton.zh"
        )
        const val CANTON_PREFIX = "apps.canton."
    }
}