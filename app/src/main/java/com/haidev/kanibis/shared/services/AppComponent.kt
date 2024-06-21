package com.haidev.kanibis.shared.services;

import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.localization.LocalizationModule
import com.haidev.kanibis.shared.masterdata.service.IMasterdataService
import com.haidev.kanibis.shared.masterdata.MasterdataModule
import com.haidev.kanibis.shared.network.NetworkModule
import com.haidev.kanibis.shared.userData.UserDataModule
import com.haidev.kanibis.shared.userData.service.IUserDataService
import dagger.Component;
import retrofit2.Retrofit
import javax.inject.Singleton;

@Singleton @Component(dependencies = [StringComponent::class, BaseComponent::class],
  modules = [ IntModule::class, LocalizationModule::class, MasterdataModule:: class, UserDataModule:: class, NetworkModule:: class])
interface AppComponent {
  fun getInt(): Int
  fun localizationService(): ILocalizationService
  fun masterdataService(): IMasterdataService
  fun userDataService(): IUserDataService
  fun retrofit(): Retrofit
}
