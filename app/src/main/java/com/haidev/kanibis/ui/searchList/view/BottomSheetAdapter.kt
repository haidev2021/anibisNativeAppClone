package com.haidev.kanibis.ui.searchList.view

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.haidev.kanibis.R

class BottomSheetAdapter(
    helper: BottomSheetDialogHelper,
    private val mItems: List<BottomSheetItem>,
    bottomSheetDialog: BottomSheetDialog
) : RecyclerView.Adapter<BottomSheetAdapter.ViewHolder?>() {
    private val mBottomSheetDialog: BottomSheetDialog
    private var mHelper: BottomSheetDialogHelper

    init {
        mBottomSheetDialog = bottomSheetDialog
        mHelper = helper
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.getContext())
                .inflate(mHelper.itemLayout, parent, false), mHelper
        )
    }

    override fun getItemCount(): Int {
        return  mItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(mItems[position])
    }

    inner class ViewHolder(itemView: View, helper: BottomSheetDialogHelper) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var imageView: ImageView?
        var textView: TextView
        var item: BottomSheetItem? = null

        init {
            itemView.setOnClickListener(this)
            imageView = itemView.findViewById<View>(R.id.imageView) as ImageView
            textView = itemView.findViewById<View>(R.id.textView) as TextView
            mHelper = helper
        }

        fun setData(item: BottomSheetItem) {
            this.item = item
            if (imageView != null) imageView!!.setImageResource(item.drawableResource)
            textView.setText(item.title)
            if (item.mSelected) textView.setTypeface(null, Typeface.BOLD)
            textView.setTextColor(if (item.mSelected) mHelper.selectedItemColor else mHelper.itemColor)
        }

        override fun onClick(v: View) {
            mBottomSheetDialog.dismiss()
            item!!.listener.onItemClick()
        }
    }

    interface ItemListener {
        fun onItemClick()
    }

    class BottomSheetItem {
        var drawableResource: Int
            private set
        var title: String
            private set
        var listener: ItemListener
        var mSelected: Boolean

        constructor(drawable: Int, title: String, listener: ItemListener, selected: Boolean) {
            drawableResource = drawable
            this.title = title
            this.listener = listener
            mSelected = selected
        }

        constructor(drawable: Int, title: String, listener: ItemListener) {
            drawableResource = drawable
            this.title = title
            this.listener = listener
            mSelected = false
        }
    }
}