package com.haidev.kanibis.shared.userData.service

import android.app.Application
import com.haidev.kanibis.shared.userData.model.UserData
import io.reactivex.rxjava3.core.Single

class UserDataService(val app: Application): IUserDataService {
    override fun getUserData(): Single<UserData> {
        return Single.just(UserData("de", arrayListOf<Int>(15, 113, 410, 438, 305, 1114)))
    }
}