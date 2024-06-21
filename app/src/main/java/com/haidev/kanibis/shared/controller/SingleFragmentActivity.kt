package com.haidev.kanibis.shared.controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel

abstract class SingleFragmentActivity<T: IBaseViewModel>: BaseActivity<T>() {

    protected abstract val mContentFragment: BaseDialogFragment<T>?
    protected abstract val mActionBarTitle: String?
    protected abstract val mLayoutResourceId: Int

    override fun onCreate(arg0: Bundle?) {
        super.onCreate(arg0)
            setContentView(mLayoutResourceId)
        if (arg0 == null)
            addFragment();
        else
            finish();
        initActionBar()
    }

    private fun addFragment() {
        if(mContentFragment != null) {
            val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
            ft.add(
                R.id.vgFragmentContent,
                mContentFragment!!,
                mContentFragment!!.javaClass.getSimpleName()
            )
            ft.commit()
            invalidateOptionsMenu()
        }
    }


    private fun initActionBar() {
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
            it.setTitle(mActionBarTitle)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("restored", true)
    }
}