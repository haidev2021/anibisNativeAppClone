package com.haidev.kanibis.shared.category.model

import com.haidev.kanibis.shared.masterdata.category.model.Category
import com.haidev.kanibis.shared.masterdata.category.model.PopularCategoryIds


data class CategoryListItem(val id: Int, val name: String, val parentId: Int?, val sortNumber: Int, val hasChildren: Boolean) {
    fun isErotic(): Boolean {
        return id == PopularCategoryIds.EROTIC.id;
    }
    companion object {
        fun fromCategory(category: Category, hasChildren: Boolean): CategoryListItem {
            return CategoryListItem(category.id, category.name, category.parentId, category.sortNumber, hasChildren)
        }
        fun cloneWithNewName(category: CategoryListItem, newName: String): CategoryListItem {
            return CategoryListItem(category.id, newName, category.parentId, category.sortNumber, category.hasChildren)
        }
    }
}