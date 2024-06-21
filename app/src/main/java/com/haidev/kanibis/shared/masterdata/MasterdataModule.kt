package com.haidev.kanibis.shared.masterdata//for creating Dagger types

import android.app.Application;
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.masterdata.service.MasterdataService
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module class MasterdataModule {
    @Provides @Singleton fun providesMasterdataService(application: Application, localizationService: ILocalizationService): IMasterdataService {
        return MasterdataService(application, localizationService);
    }
}