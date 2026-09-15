package com.rafaelfelipeac.hermes.features.categories.domain.command

interface CategoryCommandRepository {
    suspend fun deleteCategory(categoryId: Long): CategoryCommandResult

    suspend fun moveCategory(
        categoryId: Long,
        delta: Int,
    ): CategoryCommandResult
}
