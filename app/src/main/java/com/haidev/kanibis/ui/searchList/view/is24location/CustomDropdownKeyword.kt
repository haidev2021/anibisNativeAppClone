package com.haidev.kanibis.ui.searchList.view.is24location

import android.text.TextUtils
import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.util.AnibisUtils
import com.haidev.kanibis.shared.util.AnibisUtils.safeIntegerFromString

class CustomDropdownKeyword : BaseAutoCompleteDropDown {
    var keyword: String? = null
    var categoryId = 0
    var id: String

    constructor(keyword: String, categoryId: Int) {
        this.keyword = keyword
        this.categoryId = categoryId
        id = keyword + AnibisUtils.SPECTATOR_CHARACTER + categoryId
    }

    constructor(keyword: String, category: Category?) {
        this.keyword = keyword
        categoryId = category?.id ?: 0
        id = keyword + AnibisUtils.SPECTATOR_CHARACTER + categoryId
    }

    constructor(id: String) {
        if (!TextUtils.isEmpty(id)) {
            val split =
                id.split(AnibisUtils.SPECTATOR_CHARACTER.toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (split.size > 1) {
                keyword = split[0]
                categoryId = safeIntegerFromString(split[1])!!
            }
        }
        this.id = id
    }

    override fun toString(): String {
        return keyword!!
    }

    override fun isPrefixOnly(): Boolean {
        return true
    }

    override fun isValid(): Boolean {
        return !TextUtils.isEmpty(keyword) && categoryId >= 0
    }

    override fun isHistory(): Boolean {
        return true
    }

    override fun equals(item: BaseAutoCompleteDropDown): Boolean {
        return if (item is CustomDropdownKeyword) TextUtils.equals(
            keyword,
            item.keyword
        ) &&
                categoryId == item.categoryId else false
    }

    override fun getMatchedMap(): List<Int>? {
        return null
    }

    override fun getText(): String {
        return keyword!!
    }

    override fun getSubMatched(): String? {
        return null
    }
}
