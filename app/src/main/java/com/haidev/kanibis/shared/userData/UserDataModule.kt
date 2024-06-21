package com.haidev.kanibis.shared.userData

import android.app.Application
import com.haidev.kanibis.shared.userData.service.IUserDataService
import com.haidev.kanibis.shared.userData.service.UserDataService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class UserDataModule {
    @Provides @Singleton
    fun providesUserDataService(application: Application): IUserDataService {
        return UserDataService(application)
    }
}