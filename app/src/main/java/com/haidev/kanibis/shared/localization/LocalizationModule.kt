package com.haidev.kanibis.shared.localization

import android.app.Application;
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.localization.service.LocalizationService
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module class LocalizationModule {
    @Provides @Singleton fun providesLocalizationService(application: Application): ILocalizationService {
        return LocalizationService(application);
    }
}