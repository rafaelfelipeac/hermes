package com.rafaelfelipeac.hermes.features.categories.data

import com.rafaelfelipeac.hermes.features.categories.data.local.CategoryDao
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl
    @Inject
    constructor(
        private val categoryDao: CategoryDao,
    ) : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> {
            return categoryDao.observeCategories().map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun getCategories(): List<Category> {
            return categoryDao.getCategories().map { it.toDomain() }
        }

        override suspend fun getCategory(id: Long): Category? {
            return categoryDao.getCategory(id)?.toDomain()
        }

        override suspend fun getCount(): Int {
            return categoryDao.getCount()
        }

        override suspend fun insertCategory(category: Category): Long {
            return categoryDao.insert(category.toEntity())
        }

        override suspend fun insertCategories(categories: List<Category>): List<Long> {
            return categoryDao.insertAll(categories.map { it.toEntity() })
        }

        override suspend fun updateCategory(category: Category) {
            categoryDao.update(category.toEntity())
        }

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) {
            categoryDao.updateName(id, name)
        }

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) {
            categoryDao.updateColor(id, colorId)
        }

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) {
            categoryDao.updateVisibility(id, isHidden)
        }

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) {
            categoryDao.updateSortOrder(id, sortOrder)
        }

        override suspend fun deleteCategory(id: Long) {
            categoryDao.deleteById(id)
        }
    }
