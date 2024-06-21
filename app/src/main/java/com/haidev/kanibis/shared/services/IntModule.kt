package com.haidev.kanibis.shared.services;

import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module public class IntModule{

  @Provides @Singleton fun providesInt(string: String): Int {
    return Integer.parseInt(string)
  }
}
