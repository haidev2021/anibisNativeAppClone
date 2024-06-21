package com.haidev.kanibis.ui.main.views

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.controller.BaseActivity
import com.haidev.kanibis.shared.controller.BaseFragment
import com.haidev.kanibis.shared.masterdata.category.model.PopularCategoryIds
import com.haidev.kanibis.ui.categoryList.controllers.HomeCategoryListActivity
import com.haidev.kanibis.ui.main.controllers.MainActivity
import com.haidev.kanibis.ui.main.viewmodels.IDashboardHeaderViewModel


class DashboardHeaderViewHolder(
    v: View?, parentFragment: Fragment?,
) : ABaseFragmentViewHolder(v, parentFragment), OnPageChangeListener {

    override val fragment
        get() = DashboardHeaderFragment()

    override fun update() {
    }

    override val fragmentSimpleName: String = DashboardHeaderFragment::class.java.getSimpleName()
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}

    class DashboardHeaderFragment : BaseFragment<IDashboardHeaderViewModel>(), View.OnClickListener {
        lateinit var _cv: CardView
        lateinit private var _vOpenSearch: View
        lateinit  var _ivContextMenu: ImageView
        lateinit private var _vContextAnchor: View
        lateinit private var _llSearchBox: LinearLayout

        fun initInlineContextMenu() {
            if (_ivContextMenu != null && _vContextAnchor != null) {
                _ivContextMenu!!.setOnClickListener {
                    val popup = PopupMenu(requireActivity(), _vContextAnchor!!)
                    popup.getMenuInflater().inflate(R.menu.common, popup.menu)
                    (activity as BaseActivity<*>?)?.updateOptionsMenuText(popup.getMenu())
                    popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(item: MenuItem?): Boolean {
                            return activity!!.onOptionsItemSelected(item!!)
                        }
                    })
                    popup.show()
                }
            }
        }

        private fun initSearchViews(view: View) {
            Log.v("", "0508 DashboardHeaderFragment initSearchViews");
            _llSearchBox = view.findViewById(R.id.llSearchBox)
            _cv = view.findViewById(R.id.cvSearch)
            _ivContextMenu = view.findViewById(R.id.ivContextMenu)
            _vContextAnchor = view.findViewById(R.id.vContextAnchor)
            _vOpenSearch = view.findViewById(R.id.vOpenSearch)
            if (_vOpenSearch != null) _vOpenSearch!!.setOnClickListener {
                (activity as MainActivity).focusSearchBox(
                    _llSearchBox.getTop()
                )
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setRetainInstance(false)
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view: View = inflater.inflate(R.layout.fragment_dashboard, container, false)
            initViews(view)
            return view
        }

        override fun createViewModel() {
            (activity as MainActivity).mMainScreenComponent!!.inject(this)
        }

        private fun initViews(view: View) {
            initSearchViews(view)
            translateView(view)
            initInlineContextMenu()
            initCategoryView(
                view,
                R.id.llCatImmoRent,
                R.drawable.ic_menu_home_rent,
                "apps.home.new.realestate.rent"
            )
            initCategoryView(
                view,
                R.id.llCatImmoBuy,
                R.drawable.ic_menu_home_buy,
                "apps.home.new.realestate.buy"
            )
            initCategoryView(view, R.id.llCatAuto, R.drawable.ic_menu_car, "apps.home.new.cars")
            initCategoryView(view, R.id.llCatJob, R.drawable.ic_menu_job, "apps.home.new.jobs")
            initCategoryView(
                view,
                R.id.llCatAll,
                R.drawable.ic_menu_allcat,
                "apps.home.new.inallcategories"
            )
        }

        private fun initCategoryView(root: View, layoutId: Int, iconID: Int, textId: String) {
            val ll = root.findViewById<LinearLayout>(layoutId)
            (ll.findViewById<View>(R.id.tvCatName) as TextView).setText(translate(textId))
            (ll.findViewById<View>(R.id.ivCatImage) as ImageView).setImageResource(iconID)
            ll.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            showCategoryBrowsingActivity(getCategoryIdFromView(v.id))
        }

        fun showCategoryBrowsingActivity(categoryId: Int?) {
            val myIntent = Intent(activity, HomeCategoryListActivity::class.java)
            myIntent.putExtra("CATEGORY_ID", categoryId)
            this.startActivity(myIntent)
        }

        private fun getCategoryIdFromView(viewId: Int): Int? {
            return when (viewId) {
                R.id.llCatImmoRent -> PopularCategoryIds.HOUSE_RENT_ID.id
                R.id.llCatImmoBuy -> PopularCategoryIds.HOUSE_BUY_ID.id
                R.id.llCatAuto -> PopularCategoryIds.CAR_USED_AND_NEW_ID.id
                R.id.llCatJob -> PopularCategoryIds.JOB_ID.id
                else -> null
            }
        }
    }
}
