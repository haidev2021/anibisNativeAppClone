package com.haidev.kanibis.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.haidev.kanibis.App
import com.haidev.kanibis.ui.main.controllers.MainActivity
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class SplashActivity : AppCompatActivity() {

    private lateinit var mDisposable: Disposable

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent multiple instances of an activity when it is launched with different intents
        // https://code.google.com/p/android/issues/detail?id=26658
        if (!isTaskRoot) {
            val intent = intent
            val intentAction = intent.action
            if (intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                && intentAction != null && intentAction == Intent.ACTION_MAIN
            ) {
                finish()
                return
            }
        }

       var splashScreen =
           installSplashScreen()
        splashScreen.setKeepOnScreenCondition {true}

        mDisposable = (application as App).getAppComponent().localizationService().onInitComplete()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { completed ->
                Log.v("", "completed ${completed}");
                if (completed) startMainActivity()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        mDisposable.dispose();
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
        finish()
    }
}