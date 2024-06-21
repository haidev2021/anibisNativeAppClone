package com.haidev.kanibis.ui.main.controllers

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.haidev.kanibis.App
import com.haidev.kanibis.databinding.ActivityMainBinding
import com.haidev.kanibis.shared.controller.BaseActivity
import com.haidev.kanibis.ui.main.services.DaggerMainScreenComponent
import com.haidev.kanibis.ui.main.services.MainScreenComponent
import com.haidev.kanibis.ui.main.viewmodels.IDashboardHeaderViewModel
import com.haidev.kanibis.ui.main.views.TabFragmentPagerAdapter
import com.haidev.kanibis.ui.main.views.Tabs


class MainActivity : BaseActivity<IDashboardHeaderViewModel>() {

    var mMainScreenComponent: MainScreenComponent? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var _bnvTabs: BottomNavigationView
    private var _startKeywordResultFlag = false

    private lateinit var _adapter: TabFragmentPagerAdapter
    private lateinit var _viewPager: ViewPager2
    private lateinit var _appBar: AppBarLayout
    private lateinit var _toolBar: Toolbar
    private lateinit var _rlInsertionOnlineFooter: RelativeLayout
    private lateinit var _btnNotificationEdit: TextView

    private var _appBarHeight = 0

    fun getMainScreenComponent(): MainScreenComponent {
        if (mMainScreenComponent == null) {
            mMainScreenComponent = DaggerMainScreenComponent.builder()
                .appComponent((application as App).getAppComponent())
                .build()
        }
        return mMainScreenComponent!!
    }

    override fun createViewModel() {
        getMainScreenComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        _bnvTabs = binding.bottomNavigation
        init()
        initActionBar()
    }
    private fun init() {
        _appBarHeight = resources.getDimension(com.haidev.kanibis.R.dimen.home_app_bar_height).toInt()

        _appBar = binding.appbar
        _viewPager = binding.viewpager

        _rlInsertionOnlineFooter = binding.rlInsertionOnlineFooter
        _btnNotificationEdit = binding.btnNotificationEdit

        _adapter = TabFragmentPagerAdapter(supportFragmentManager, lifecycle)
        _viewPager.setAdapter(_adapter)
        _viewPager.setCurrentItem(Tabs.DASHBOARD.position, false)
        _viewPager.setOffscreenPageLimit(5)
        _toolBar = binding.toolbar
        setSupportActionBar(_toolBar)

        val params1 = _toolBar.getLayoutParams() as AppBarLayout.LayoutParams
        params1.scrollFlags = 0

        _appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            if (_startKeywordResultFlag && verticalOffset == -_appBar.getHeight()) {
                _startKeywordResultFlag = false
            }
        }

        initBottomNavigator()
    }
    
    fun initBottomNavigator() {
        val menu: Menu = _bnvTabs.getMenu()
        for (i in 0 until menu.size()) {
            val item: MenuItem = menu.getItem(i)
            item.setTitle((application as App).getAppComponent().localizationService().translate(item.getTitle() as String))
        }
    }

    private fun initActionBar(){

    }

    fun focusSearchBox(topScroll: Int) {
        _adapter.dashboardFragment?.focusSearchBox(topScroll)
    }
}