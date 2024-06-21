package com.haidev.kanibis.shared.masterdata.category.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.rxjava3.core.Single

enum class PopularCategoryIds(val id: Int) {
    ALL_ID (0),
    CAR_ID (4),
    CAR_USED_AND_NEW_ID (113),
    MOTO_ID (11),
    HOUSE_ID (16),
    HOUSE_RENT_ID (410),
    HOUSE_BUY_ID (438),
    JOB_ID (305),
    EROTIC(15),
}

data class Category(val id: Int, val name: String, val parentId: Int?, val sortNumber: Int) {
    fun isErotic(): Boolean {
        return id == PopularCategoryIds.EROTIC.id;
    }
}

@Entity(indices = [Index(value = ["parentId"], unique = false, orders = [Index.Order.ASC])])
data class XBCategories (
    @PrimaryKey val id: Int,
    val parentId: Int?,
    val sortNumber: Int,
    val nameDe: String,
    val nameFr: String,
    val nameIt: String,
    val nameEn: String?,
    val topSortOrder: Int?,
)

const val NAME_COLUMN = "CASE WHEN :language = 'de' THEN nameDe " +
                                "WHEN :language = 'fr' THEN nameFr " +
                                "WHEN :language = 'it' THEN nameIt " +
                                "WHEN :language = 'en' THEN nameEn " +
                                "ELSE 'noName' END as name"

@Dao
interface CategoryDao {
    @Transaction
    @Query(
        "select id, " + NAME_COLUMN + ", parentId, sortNumber " +
        "from XBCategories " +
        "where parentId = :categoryId"
    )
    fun getChildren(categoryId: Int, language: String): Single<List<Category>>

    @Transaction
    @Query(
        "select id, " + NAME_COLUMN + ", parentId, sortNumber " +
        "from XBCategories " +
        "where id = :categoryId"
    )
    fun getCategory(categoryId: Int, language: String): Single<Category>


    @Transaction
    @Query(
        "select id, " + NAME_COLUMN + ", parentId, sortNumber " +
                "from XBCategories " +
                "where id in (:categoryIds)"
    )
    fun getCategories(categoryIds: List<Int>, language: String): Single<List<Category>>

    @Transaction
    @Query(
        "select id, " + NAME_COLUMN + ", parentId, sortNumber " +
        "from XBCategories " +
        "where parentId is NULL"
    )
    fun getRootCategories(language: String): Single<List<Category>>

    @Transaction
    @Query(
        "select distinct parentId " +
        "from XBCategories " +
        "where parentId in (:categoryIds) "
    )
    fun filterHasChildren(categoryIds: List<Int>): Single<List<Int>>
}