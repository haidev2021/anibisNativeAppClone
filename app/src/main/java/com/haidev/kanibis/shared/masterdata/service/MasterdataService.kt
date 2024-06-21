package com.haidev.kanibis.shared.masterdata.service

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.room.Room
import com.haidev.kanibis.shared.localization.service.ILocalizationService
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.masterdata.category.model.MasterdataDatabase
import com.haidev.kanibis.shared.masterdata.category.model.XBAttribute
import com.haidev.kanibis.shared.masterdata.category.model.XBAttributeEntry
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.NullPointerException

class MasterdataService(val app: Application, val localizationService: ILocalizationService):
    IMasterdataService {
    var mDB: MasterdataDatabase
    init {
        mDB = Room.databaseBuilder(app, MasterdataDatabase::class.java, "Masterdata7.db")
            .createFromAsset("xbase.sqlite")
            .build()
        test()
    }

    @SuppressLint("CheckResult")
    private fun test() {
        getCategory(21)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ category ->
                Log.v("", "1904 getCategory size ${category.name}")
            }, {error -> Log.v("", "1904 getChildrenOfCategory error ${error}")})
        getChildrenOfCategory(21)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ list ->
                Log.v("", "1904 getChildrenOfCategory size ${list.size}")
            }, {error -> Log.v("", "1904 getChildrenOfCategory error ${error}")})
        getChildrenOfCategory(null)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ list ->
                Log.v("", "1904 getRootCategories size ${list.size}")
            }, {error -> Log.v("", "1904 getRootCategories error ${error}")})

        getCategoryAttribute(113)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ list ->
            }, {error -> Log.v("", "0905 getCategoryAttribute error ${error}")})
    }

    override fun getChildrenOfCategory(categoryId: Int?): Single<List<Category>>{
        if(categoryId == null) {
            return mDB.getCategoryDao().getRootCategories(localizationService.currentIsoLanguage)
        }else {
            return mDB.getCategoryDao()
                .getChildren(categoryId, localizationService.currentIsoLanguage)
        }
    }
    override fun getCategory(categoryId: Int?): Single<Category>{
        return if(categoryId == null)
            Single.error(NullPointerException())
        else
            mDB.getCategoryDao().getCategory(categoryId, localizationService.currentIsoLanguage)
    }
    override fun getCategories(categoryIds: List<Int>?): Single<List<Category>>{
        return if(categoryIds == null)
            Single.error(NullPointerException())
        else
            mDB.getCategoryDao().getCategories(categoryIds, localizationService.currentIsoLanguage)
    }
    override fun getRootCategories(): Single<List<Category>> {
        return mDB.getCategoryDao().getRootCategories(localizationService.currentIsoLanguage)
    }
    override fun filterHasChildren(categoryIds: List<Int>): Single<List<Int>> {
        return mDB.getCategoryDao().filterHasChildren(categoryIds)
    }
    override fun getCategoryAttribute(categoryId: Int?): Single<List<XBAttribute>> {
        return mDB.getAttributeDao().getCategoryAttributes(categoryId, localizationService.currentIsoLanguage)
    }
    override fun getAttributeEntries(attributeId: Int?): Single<List<XBAttributeEntry>> {
        return mDB.getAttributeDao().getAttributeEntries(attributeId, localizationService.currentIsoLanguage)
    }
    override fun getEntries(ids: List<Int>): Single<List<XBAttributeEntry>> {
        return mDB.getAttributeDao().getEntries(ids, localizationService.currentIsoLanguage)
    }
}