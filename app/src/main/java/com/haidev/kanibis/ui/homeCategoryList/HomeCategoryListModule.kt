package com.haidev.kanibis.ui.homeCategoryList

import android.util.Log
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.masterdata.service.MasterdataService
import com.haidev.kanibis.shared.parameter.service.IParamService
import com.haidev.kanibis.shared.parameter.service.ParamService
import com.haidev.kanibis.shared.services.ActivityScope
import com.haidev.kanibis.shared.userData.service.IUserDataService
import com.haidev.kanibis.shared.userData.service.UserDataService
import com.haidev.kanibis.ui.homeCategoryList.viewmodels.HomeCategoryListViewModel
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module class HomeCategoryListModule {

    @ActivityScope @Provides
    fun providesParamService(retrofit: Retrofit): IParamService {
        return ParamService(retrofit);
    }

    @ActivityScope @Provides
    fun providesHomeCategoryListViewModel(paramService: IParamService, localizationService: ILocalizationService,
                                          masterdataService: IMasterdataService, userDataService: IUserDataService
    ): ICategoryListViewModel {
        return HomeCategoryListViewModel(paramService, localizationService, masterdataService, userDataService);
    }
}