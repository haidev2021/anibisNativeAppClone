package com.haidev.kanibis.shared.controller


import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import android.view.WindowManager.BadTokenException
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.haidev.kanibis.R
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import com.haidev.kanibis.shared.viewmodels.IToastMessageViewModel
import com.haidev.kanibis.ui.main.controllers.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

abstract class BaseActivity<T: IBaseViewModel>  : AppCompatActivity(){

    @Inject lateinit var mViewModel: T

    private var mProgressDialog: ProgressDialog? = null

    private lateinit var mDisposables: CompositeDisposable

    protected abstract fun createViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDisposables = CompositeDisposable()
        createViewModel()
        mViewModel.onAttach()
    }

    override fun onPostResume() {
        super.onPostResume()
        mViewModel.onPostResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
        if (!mDisposables.isDisposed) mDisposables.dispose();
    }

    override fun onPause()
    {
        super.onPause()
        mViewModel.onPause()
    }

    override fun onResume()
    {
        super.onResume()
        mViewModel.onResume()
        registerSubscriptionViewModel(mViewModel)
    }

    open fun registerSubscriptionViewModel(subscriptionViewModel: IBaseViewModel) {
        addDisposable(subscriptionViewModel.onAlertDialog()!!)

        label@{ viewModel ->
            if (isDestroyed) return@label
            try {
                AlertDialog.Builder(this@BaseActivity).setTitle(viewModel!!.title)
                    .setMessage(viewModel!!.message)
                    .setNegativeButton(
                        viewModel!!.negativeButtonText
                    ) { dialog, which -> viewModel.onNegativeButtonClick() }
                    .setPositiveButton(
                        viewModel!!.positiveButtonText
                    ) { dialog, which -> viewModel.onPositiveButtonClick() }
                    .setOnDismissListener { dialog -> viewModel.onDismiss() }
                    .setCancelable(viewModel!!.isCancelable)
                    .show()
                viewModel.onShow()
            } catch (e: BadTokenException) {
            }
        }
        addDisposable(subscriptionViewModel.onToastMessage()!!
        )
        label@{ viewModel ->
            if (isDestroyed) return@label
            if (viewModel != null && !TextUtils.isEmpty(viewModel.message)) {
                try {
                    val duration =
                        if (viewModel.duration === IToastMessageViewModel.LENGTH_LONG) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                    val toast: Toast =
                        Toast.makeText(this@BaseActivity, viewModel.message, duration)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                } catch (e: BadTokenException) {
                }
            }
        }
        addDisposable(subscriptionViewModel.onProgressDialog()!!
        ) label@{ viewModel ->
            if (isDestroyed) return@label
            try {
                val isLoading =
                    viewModel != null && !TextUtils.isEmpty(viewModel.message)
                if (isLoading) {
                    if (mProgressDialog == null || !mProgressDialog!!.isShowing) {
                        mProgressDialog =
                            ProgressDialog.show(this@BaseActivity, "", viewModel!!.message)
                    } else {
                        mProgressDialog!!.setMessage(viewModel!!.message)
                    }
                    mProgressDialog!!.setCancelable(viewModel!!.isCancelable)
                } else if (mProgressDialog != null) {
                    if (mProgressDialog!!.isShowing()) mProgressDialog!!.dismiss()
                    mProgressDialog = null
                }
            } catch (e: BadTokenException) {
            }
        }
        addDisposable(subscriptionViewModel.onAction()!!
        ) label@{ integer ->
            if (isDestroyed) return@label
            try {
                onExecuteAction(integer!!)
            } catch (e: BadTokenException) {
            }
        }
    }

    protected fun openGooglePlay() {
        var packageName = packageName
        try {
            val uri = Uri.parse(String.format("market://details?id=%s", packageName))
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            val uri = Uri.parse(
                String.format(
                    "https://play.google.com/store/apps/details?id=%s",
                    packageName
                )
            )
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }
    protected fun exitApp() {
        Process.killProcess(Process.myPid())
        System.exit(1)
    }

    protected fun restartApplication() {
        Log.v("", "0508 restartApplication");
        val intent = Intent(this@BaseActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    protected fun onExecuteAction(actionCode: Int) {
        when (actionCode) {
            IBaseViewModel.ACTION_FINISH -> finish()
            IBaseViewModel.ACTION_EXIT_APP -> exitApp()
            IBaseViewModel.ACTION_OPEN_GOOGLE_PLAY -> openGooglePlay()
            IBaseViewModel.ACTION_RESTART_APPLICATION -> restartApplication()
            else -> {}
        }
    }

    protected open fun <M> addDisposable(observable: Observable<M>, onNext: Consumer<M>
    ) {
        addDisposable(observable, onNext
        ) { throwable ->
            Log.e("addDisposable", throwable.getLocalizedMessage())
        }
    }

    open fun addDisposable(disposable: Disposable?) {
        mDisposables.add(disposable)
    }

    protected open fun <M> addDisposable(observable: Observable<M>, onNext: Consumer<M>, onError: Consumer<Throwable>
    ) {
        mDisposables.add(
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError)
        )
    }

    fun updateOptionsMenuText(menu: Menu?) {
        if (menu == null) return
        val size = menu.size()
        for (i in 0 until size) {
            val item = menu.getItem(i)
            item.setTitle (when (item.itemId) {
                R.id.mnuFeedback -> translate("apps.feedback")
                R.id.mnuLogin -> translate("apps.login")
                R.id.mnuDeleteAllSearchJobs -> translate("apps.action.deletesearchjobs")
                R.id.action_login -> translate("apps.action.login")
                R.id.action_reset_all_insertion -> translate("apps.action.reset")
                R.id.mnuSetting -> translate("apps.settings")
                R.id.mnuDebugSetting -> "Settings"
                R.id.mnuForceCrash -> "Crash the app!"
                else -> {""}
            }!!)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return if (this is ContextMenuController && !(this as ContextMenuController).isContextMenuShown) {
            super.onCreateOptionsMenu(menu)
        } else {
            menuInflater.inflate(R.menu.common, menu)
            val mnuLogin = menu.findItem(R.id.mnuLogin)
            val mnuFeedback = menu.findItem(R.id.mnuFeedback)
            updateOptionsMenuText(menu)
            super.onCreateOptionsMenu(menu)
        }
    }


    private fun translate(textKey: String?): String? {
        return mViewModel.translate(textKey)
    }

    private fun translateView(v: View) {
        return mViewModel.translateView(v)
    }

    interface ContextMenuController {
        val isContextMenuShown: Boolean
    }

    fun showDialog(fragment: BaseDialogFragment<*>) {
        try {
            fragment.show(supportFragmentManager, fragment.javaClass.getName())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showFullScreenDialog(fragment: BaseDialogFragment<*>) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.add(android.R.id.content, fragment)
            .addToBackStack(fragment.javaClass.getName()).commit()
    }
    companion object {
        const val CONTACT_INFO_REQUEST = 1
        const val ZIP_CANTON_REQUEST = 2
        const val REFINE_SEARCH_REQUEST_CODE = 3
        const val HOT_LOGIN_REQUEST_CODE = 4
        const val MENU_LOGIN_REQUEST_CODE = 5
        const val CONTACT_MORE_LISTING_REQUEST_CODE = 6
        const val EMAIL_SENT_REQUEST_CODE = 7
        const val EMPTY_INSERTION_CHECKING_REQUEST_CODE = 8
        const val EMPTY_FAVORITE_CHECKING_REQUEST_CODE = 9
        const val EMPTY_PUSH_SJ_CHECKING_REQUEST_CODE = 10
        const val EMPTY_SEARCH_JOB_CHECKING_REQUEST_CODE = 11
        const val PLAY_SERVICES_RESOLUTION_REQUEST = 12
        const val CAMERA_REQUEST_CODE = 13
        const val GALLERY_REQUEST_CODE = 14
        const val GALLERY_MULTI_REQUEST_CODE = 15
        const val PERMISSION_READ_REQUEST_CODE = 16
        const val PERMISSION_LOCATION_REQUEST_CODE = 17
        const val LOCATION_SETTING_REQUEST_CODE = 18
        const val PRODUCT_SHOP_REQUEST_CODE = 19
        const val LOGIN_FROM_MENU_EXTRA_KEY = "from-menu"
    }
}