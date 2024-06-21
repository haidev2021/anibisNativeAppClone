package com.haidev.kanibis.shared.parameter.service

import com.haidev.kanibis.shared.parameter.model.LightAdvertDetail
import com.haidev.kanibis.shared.parameter.model.SearchCountBody
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.parameter.model.SearchResultBody
import io.reactivex.rxjava3.core.Single
import retrofit2.Retrofit

class ParamService(retrofit: Retrofit): IParamService {
    private var mApi: IParamApi
    init {
        mApi = retrofit.create(IParamApi::class.java)
    }
    override fun getSearchCount(query: String): Single<List<SearchCountCategory>> {
        return mApi.getSearchCount(SearchCountBody(query))
    }

    override fun getSearchLightDetails(ids: List<String>): Single<List<LightAdvertDetail>> {
        return mApi.getSearchLightDetails(SearchResultBody("de", ids))
    }

    override fun getSearchAdvertIds(query: String): Single<List<String>> {
        return mApi.getSearchAdvertIds(SearchCountBody(query))
    }
}