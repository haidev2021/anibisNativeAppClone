package com.haidev.kanibis.shared.category.model

data class CategoryList(val history: List<CategoryListItem>?, val tree: CategoryTree, val searchCounts: Map<Int, Int>?)