package com.haidev.kanibis.ui.main.views

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.haidev.kanibis.R

abstract class ABaseFragmentViewHolder(
    v: View?, private val _parentFragment: Fragment?
) : RecyclerView.ViewHolder(v!!), KanibisFragmentViewHolder {

    protected abstract val fragment: Fragment
    protected abstract val fragmentSimpleName: String
    abstract override fun update()

    init {
        addFragment()
    }

    private fun addFragment() {
        val ft = _parentFragment!!.getChildFragmentManager().beginTransaction()
        ft.add(R.id.layout, fragment!!, fragmentSimpleName)
        ft.commitAllowingStateLoss()
    }

    val addedFragment: Fragment?
        get() = _parentFragment!!.getChildFragmentManager()
            .findFragmentByTag(fragmentSimpleName)

    override fun onPause() {}
    override fun onResume() {}
    override fun onDestroy() {}
}
