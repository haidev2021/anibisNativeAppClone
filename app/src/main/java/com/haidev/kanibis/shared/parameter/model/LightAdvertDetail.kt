package com.haidev.kanibis.shared.parameter.model
data class LightAdvertDetail (
    val _id: String,
    val language: String,
    val categoryId: Int,
    val contactAddress: LightContactAddress,
    val title: String,
    val price: String,
    val thumbnail: String,
    val posted: String,
    val expiring: String,
    val hits: Int,
    val contacts: Int,
    val state: Int,
    val briefDescription: String,
)

data class AdvertDetail (
    val _id: String,
    val userId: String,
    val language: String,
    val categoryId: Int,
    val attributes: List<DetailAttribute>,
    val pictures: List<DetailPicture>,
    val contactAddress: DetailContactAddress,
    val title: String,
    val description: String,
    val price: String,
    val categoryPath: List<Int>,
    val thumbnail: String,
    val modified: String,
    val posted: String,
    val expiring: String,
    val hits: Int,
    val contacts: Int,
    val user: DetailUser,
    val state: Int,
    val categoryName: String,
    val briefDescription: String,
)

data class DetailAttribute (
    val attributeId: Int,
    val attributeEntryIds: List<Int>?,
    val attributeEntryId: Int?,
    val inputText: String?,
    val inputInt: Int?,
    val inputDate: String?,
)

data class DetailPicture (
    val blogPhotosThumbnail: String,
    val blogPhotosResized: String
)

data class LightContactAddress (
    val street: String,
    val zip: Int,
    val city: String,
    val phone: String,
    val countryCode: String,
    val allowEMail: Boolean,
    val latitude: Int,
    val longitude: Int,
    val contactType: Int,
)

data class DetailContactAddress (
    val zip: Int,
    val city: String,
)

data class DetailUser (
    val _id: String,
    val email: String,
    val memberSince: String
)