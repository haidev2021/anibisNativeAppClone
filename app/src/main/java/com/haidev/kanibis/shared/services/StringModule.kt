package com.haidev.kanibis.shared.services;

import android.app.Application
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton

@Module public class StringModule(val string: String) {

  @Provides fun providesString(): String {
    return string;
  }
}
