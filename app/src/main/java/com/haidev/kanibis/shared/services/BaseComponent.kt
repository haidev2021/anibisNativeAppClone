package com.haidev.kanibis.shared.services;

import android.app.Application
import dagger.Component;

@Component(modules = [ BaseModule::class ])
public interface BaseComponent {
    fun application(): Application
}
