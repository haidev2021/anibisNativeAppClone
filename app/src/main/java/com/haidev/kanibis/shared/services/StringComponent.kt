package com.haidev.kanibis.shared.services;

import android.app.Application
import dagger.Component;

@Component(modules = [ StringModule::class ])
public interface StringComponent {
    fun getString(): String
}
