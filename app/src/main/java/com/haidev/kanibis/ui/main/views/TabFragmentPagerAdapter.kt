package com.haidev.kanibis.ui.main.views

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.haidev.kanibis.ui.main.controllers.TabDashboardFragment

enum class Tabs(val position: Int) {
    DASHBOARD(0),
    INSERTION(1),
    NOTIFICATION(2),
    MY_ANIBIS(3),
}

class TabFragmentPagerAdapter(fm: FragmentManager,
                              lifecycle: Lifecycle
) : FragmentStateAdapter(fm, lifecycle) {

    val count = 4
    var _fragments: Array<Fragment?> = arrayOfNulls(count)

    init {
        _fragments[Tabs.DASHBOARD.position] = TabDashboardFragment()
        _fragments[Tabs.INSERTION.position] = Fragment()
        _fragments[Tabs.MY_ANIBIS.position] = Fragment()
        _fragments[Tabs.NOTIFICATION.position] = Fragment()
    }

    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        return _fragments[position]!!;
    }

    val dashboardFragment: TabDashboardFragment?
        get() = _fragments[Tabs.DASHBOARD.position] as TabDashboardFragment
}
