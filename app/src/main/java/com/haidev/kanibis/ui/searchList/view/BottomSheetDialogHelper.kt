package com.haidev.kanibis.ui.searchList.view

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.haidev.kanibis.R

abstract class BottomSheetDialogHelper(
    var context: Context,
    title: String?,
    items: List<BottomSheetAdapter.BottomSheetItem>
) {
    var mBottomSheetDialog: BottomSheetDialog? = null
    var items: List<BottomSheetAdapter.BottomSheetItem>
    var title: String?

    init {
        this.items = items
        this.title = title
    }

    fun showBottomSheetDialog() {
        mBottomSheetDialog = BottomSheetDialog(context)
        val view: View =
            (context as Activity).getLayoutInflater().inflate(R.layout.bottom_sheet, null)
        val tvTitle: TextView = view.findViewById<View>(R.id.tvTitle) as TextView
        tvTitle.setText(title)
        tvTitle.setTextColor(titleColor)
        tvTitle.setVisibility(if (TextUtils.isEmpty(title)) View.GONE else View.VISIBLE)
        val rvList: RecyclerView = view.findViewById<View>(R.id.rvList) as RecyclerView
        rvList.setHasFixedSize(true)
        rvList.setLayoutManager(LinearLayoutManager(context))
        rvList.setAdapter(BottomSheetAdapter(this, items, mBottomSheetDialog!!))
        mBottomSheetDialog!!.setContentView(view)
        mBottomSheetDialog!!.show()
    }

    abstract val titleColor: Int
    abstract val itemColor: Int
    abstract val selectedItemColor: Int
    abstract val itemLayout: Int
}
