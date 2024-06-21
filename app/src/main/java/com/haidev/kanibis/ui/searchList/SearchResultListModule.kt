package com.haidev.kanibis.ui.searchList

import android.util.Log
import com.haidev.kanibis.shared.category.viewmodel.ICategoryListViewModel
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.parameter.service.IParamService
import com.haidev.kanibis.shared.parameter.service.ParamService
import com.haidev.kanibis.shared.services.ActivityScope
import com.haidev.kanibis.shared.services.SubActivityScope
import com.haidev.kanibis.shared.userData.service.IUserDataService
import com.haidev.kanibis.ui.homeCategoryList.viewmodels.HomeCategoryListViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.ISearchListViewModel
import com.haidev.kanibis.ui.searchList.viewmodel.SearchListViewModel
import dagger.Module
import dagger.Provides

@Module class SearchResultListModule {
    @SubActivityScope
    @Provides
    fun providesSearchListViewModel(paramService: IParamService, localizationService: ILocalizationService,
                                          masterdataService: IMasterdataService, userDataService: IUserDataService
    ): ISearchListViewModel {
        return SearchListViewModel(paramService, localizationService, masterdataService, userDataService);
    }
}