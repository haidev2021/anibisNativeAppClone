package com.haidev.kanibis.shared.userData.service
import com.haidev.kanibis.shared.userData.model.UserData
import io.reactivex.rxjava3.core.Single

interface IUserDataService {
    fun getUserData(): Single<UserData>
}