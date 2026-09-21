package com.rafaelfelipeac.hermes.features.categories.data

import androidx.room.withTransaction
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_CATEGORY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.command.CategoryCommandRepository
import com.rafaelfelipeac.hermes.features.categories.domain.command.CategoryCommandResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryCommandRepository
    @Inject
    constructor(
        private val database: HermesDatabase,
        private val userActionLogger: UserActionLogger,
    ) : CategoryCommandRepository {
        private val categoryDao = database.categoryDao()
        private val workoutDao = database.workoutDao()
        private val personalRecordDao = database.personalRecordDao()
        private val challengeDao = database.challengeDao()

        override suspend fun deleteCategory(categoryId: Long): CategoryCommandResult {
            if (categoryId == UNCATEGORIZED_ID) return CategoryCommandResult.NoChange

            return database.withTransaction {
                val category =
                    categoryDao.getCategory(categoryId)
                        ?: return@withTransaction CategoryCommandResult.NoChange

                workoutDao.reassignCategory(
                    deletedCategoryId = categoryId,
                    uncategorizedId = UNCATEGORIZED_ID,
                )
                personalRecordDao.reassignCategory(
                    categoryId = categoryId,
                    newCategoryId = null,
                )
                challengeDao.reassignCategory(
                    categoryId = categoryId,
                    newCategoryId = null,
                )
                categoryDao.deleteById(categoryId)

                userActionLogger.log(
                    actionType = DELETE_CATEGORY,
                    entityType = CATEGORY,
                    entityId = categoryId,
                    metadata = mapOf(CATEGORY_NAME to category.name),
                )

                CategoryCommandResult.Changed
            }
        }

        override suspend fun moveCategory(
            categoryId: Long,
            delta: Int,
        ): CategoryCommandResult {
            return database.withTransaction {
                val ordered = categoryDao.getCategories()
                val index = ordered.indexOfFirst { it.id == categoryId }
                val swapIndex = index + delta

                if (index == -1 || swapIndex !in ordered.indices) {
                    return@withTransaction CategoryCommandResult.NoChange
                }

                val current = ordered[index]
                val target = ordered[swapIndex]

                categoryDao.updateSortOrder(current.id, target.sortOrder)
                categoryDao.updateSortOrder(target.id, current.sortOrder)

                userActionLogger.log(
                    actionType = REORDER_CATEGORY,
                    entityType = CATEGORY,
                    entityId = current.id,
                    metadata = mapOf(CATEGORY_NAME to current.name),
                )

                CategoryCommandResult.Changed
            }
        }

        override suspend fun moveCategoryToPosition(
            categoryId: Long,
            targetIndex: Int,
        ): CategoryCommandResult {
            return database.withTransaction {
                val ordered = categoryDao.getCategories()
                val currentIndex = ordered.indexOfFirst { it.id == categoryId }

                if (currentIndex == -1 || targetIndex !in ordered.indices || currentIndex == targetIndex) {
                    return@withTransaction CategoryCommandResult.NoChange
                }

                val current = ordered[currentIndex]
                val reordered = ordered.toMutableList()
                reordered.removeAt(currentIndex)
                reordered.add(targetIndex, current)

                reordered.forEachIndexed { index, category ->
                    if (category.sortOrder != index) {
                        categoryDao.updateSortOrder(category.id, index)
                    }
                }

                userActionLogger.log(
                    actionType = REORDER_CATEGORY,
                    entityType = CATEGORY,
                    entityId = current.id,
                    metadata = mapOf(CATEGORY_NAME to current.name),
                )

                CategoryCommandResult.Changed
            }
        }
    }
