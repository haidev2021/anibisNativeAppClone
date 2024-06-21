package com.haidev.kanibis.shared.controller

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.haidev.kanibis.shared.viewmodels.IBaseViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

abstract class BaseFragment<T : IBaseViewModel> : Fragment() {
    @Inject lateinit var mViewModel: T
    private lateinit var mDisposables: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mDisposables = CompositeDisposable()
        createViewModel()
    }

    override fun onResume() {
        super.onResume()
        (activity as BaseActivity<*>).registerSubscriptionViewModel(mViewModel)
        mViewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        mViewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.onDetach()
        if (!mDisposables.isDisposed) mDisposables.dispose()
    }

    val baseActivity: BaseActivity<*>
        get() = getActivity() as BaseActivity<*>

    protected open fun <M> addDisposable(observable: Observable<M>, onNext: Consumer<M>?
    ) {
        addDisposable(observable, onNext
        ) { throwable ->
            Log.e("addDisposable", throwable.getLocalizedMessage())
        }
    }

    protected open fun addDisposable(disposable: Disposable?) {
        if (mDisposables == null) mDisposables = CompositeDisposable()
        mDisposables!!.add(disposable)
    }

    protected open fun <M> addDisposable(observable: Observable<M>, onNext: Consumer<M>?, onError: Consumer<Throwable>
    ) {
        if (mDisposables == null) mDisposables = CompositeDisposable()
        mDisposables!!.add(
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, onError)
        )
    }

    protected abstract fun createViewModel()

    fun translate(textKey: String?): String? {
        return mViewModel.translate(textKey)
    }

    fun translateView(v: View) {
        return mViewModel.translateView(v)
    }
}
