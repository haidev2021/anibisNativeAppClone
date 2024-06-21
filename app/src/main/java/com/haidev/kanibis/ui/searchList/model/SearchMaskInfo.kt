package com.haidev.kanibis.ui.searchList.model

import com.haidev.kanibis.shared.category.model.CategoryListItem

class SearchMaskInfo {
    var _editSearchQuery: String? = null

    var _newSearchQuery: String? = null

    var _isEdit: Boolean = false

    var _lostQuery: String? = ""

    var _hasPrice = false

    var _hasPriceType = false

    var category: CategoryListItem? = null
        set

    val isSearch: Boolean
        get() = true
}