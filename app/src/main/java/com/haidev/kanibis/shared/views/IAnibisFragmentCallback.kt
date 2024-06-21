package com.haidev.kanibis.shared.views

interface IAnibisFragmentCallback {
    fun openWebLink(url: String?)
    fun openMemberLink(id: Int, url: String?)
    fun openMemberAdvertLink(advertId: Int, url: String?)
    val isLargeLayout: Boolean
}
