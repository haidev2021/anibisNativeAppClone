package com.haidev.kanibis.shared.parameter.service

import com.haidev.kanibis.shared.parameter.model.LightAdvertDetail
import com.haidev.kanibis.shared.parameter.model.SearchCountCategory
import com.haidev.kanibis.shared.parameter.model.SearchResultBody
import io.reactivex.rxjava3.core.Single

interface IParamService {
    fun getSearchCount(query: String): Single<List<SearchCountCategory>>
    fun getSearchLightDetails(ids: List<String>): Single<List<LightAdvertDetail>>
    fun getSearchAdvertIds(query: String): Single<List<String>>
}