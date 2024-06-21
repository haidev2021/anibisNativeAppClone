package com.haidev.kanibis.shared.services;

import android.app.Application
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton

@Module public class BaseModule(val application: Application) {

  @Provides
  fun providesApplication(): Application {
    return application;
  }
}
