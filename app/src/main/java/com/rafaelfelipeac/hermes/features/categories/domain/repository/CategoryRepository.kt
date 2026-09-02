package com.rafaelfelipeac.hermes.features.categories.domain.repository

import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategories(): List<Category>

    suspend fun getCategory(id: Long): Category?

    suspend fun getCount(): Int

    suspend fun insertCategory(category: Category): Long

    suspend fun insertCategories(categories: List<Category>): List<Long>

    suspend fun updateCategory(category: Category)

    suspend fun updateCategoryName(
        id: Long,
        name: String,
    )

    suspend fun updateCategoryColor(
        id: Long,
        colorId: String,
    )

    suspend fun updateCategoryVisibility(
        id: Long,
        isHidden: Boolean,
    )

    suspend fun updateCategorySortOrder(
        id: Long,
        sortOrder: Int,
    )

    suspend fun deleteCategory(id: Long)
}
