package com.haidev.kanibis.shared.masterdata.category.model

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [XBCategories::class, XBCategoryAttributes::class, XBAttributes::class, XBAttributeEntries::class], version = 11)
abstract class MasterdataDatabase : RoomDatabase() {
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getAttributeDao(): AttributeDao
}
