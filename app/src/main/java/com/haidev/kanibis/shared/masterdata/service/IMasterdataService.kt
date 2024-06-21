package com.haidev.kanibis.shared.masterdata.service

import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.masterdata.category.model.XBAttributeEntry
import io.reactivex.rxjava3.core.Single

interface IMasterdataService {
    fun getChildrenOfCategory(categoryId: Int?): Single<List<Category>>
    fun getCategory(categoryId: Int?): Single<Category>
    fun getRootCategories(): Single<List<Category>>
    fun getCategories(categoryIds: List<Int>?): Single<List<Category>>
    fun filterHasChildren(categoryIds: List<Int>): Single<List<Int>>
    fun getCategoryAttribute(categoryId: Int?): Single<List<XBAttribute>>
    fun getAttributeEntries(attributeId: Int?): Single<List<XBAttributeEntry>>
    fun getEntries(ids: List<Int>): Single<List<XBAttributeEntry>>
}