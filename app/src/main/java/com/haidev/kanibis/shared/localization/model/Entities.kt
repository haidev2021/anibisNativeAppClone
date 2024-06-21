package com.haidev.kanibis.shared.localization.model
import androidx.room.*
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single

@Entity
data class XBResources(
    @PrimaryKey val resourceId: Int,
    val resourceKey: String,
)

@Entity(indices = [Index(value = ["resourceId"], unique = false, orders = [Index.Order.ASC])])
data class XBTextResources(
    @PrimaryKey val id: String,
    val text: String,
    val language: String,
    val resourceId: Int,
    val isHtml: Int,
)

data class Translation(val textKey: String?, val text: String?)

@Dao
interface TranslationDao {
    @Transaction
    @Query("select XBResources.resourceKey as textKey, XBTextResources.text as text " +
            "from XBTextResources, XBResources " +
            "where XBTextResources.resourceId = XBResources.resourceId and " +
            "XBTextResources.language = :language and XBResources.resourceKey IN (:keys)")

    fun getTranslationList(language: String, keys: List<String?>?): Single<List<Translation>>
}

@Database(entities = [XBResources::class, XBTextResources::class], version = 6)
abstract class TranslationDatabase : RoomDatabase() {
    abstract fun getTranslationDao(): TranslationDao
}