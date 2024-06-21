package com.haidev.kanibis.shared.parameter.service

import com.haidev.kanibis.shared.parameter.model.LightAdvertDetail
import com.haidev.kanibis.shared.parameter.model.SearchCountBody
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.parameter.model.SearchResultBody
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface IParamApi {
    @POST("advert/searchCount")
    fun getSearchCount(@Body searchCountBody: SearchCountBody): Single<List<SearchCountCategory>>

    @POST("/advert/search")
    fun getSearchAdvertIds(@Body searchResultBody: SearchCountBody): Single<List<String>>

    @POST("/advert/searchResult")
    fun getSearchLightDetails(@Body searchResultBody: SearchResultBody): Single<List<LightAdvertDetail>>
}
