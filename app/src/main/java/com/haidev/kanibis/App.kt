package com.haidev.kanibis

import android.app.Application
import android.util.Log
import com.haidev.kanibis.shared.services.*

class App : Application(){
    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        val stringComponent: StringComponent = DaggerStringComponent.builder().stringModule(StringModule("2")).build();
        val baseComponent: BaseComponent = DaggerBaseComponent.builder().baseModule(BaseModule(this)).build();
        appComponent =
            DaggerAppComponent.builder().stringComponent(stringComponent).baseComponent(baseComponent).build();
    }

    @JvmName("getAppComponent1")
    fun getAppComponent(): AppComponent {
        return appComponent
    }
}