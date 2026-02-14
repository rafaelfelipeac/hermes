@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.categories.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.CATEGORY_HIDDEN
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.CATEGORY_VISIBLE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_CATEGORY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_COLOR
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CATEGORY_VISIBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel
    @Inject
    constructor(
        private val repository: CategoryRepository,
        private val workoutRepository: WeeklyTrainingRepository,
        private val categorySeeder: CategorySeeder,
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        val state: StateFlow<CategoriesState> =
            repository
                .observeCategories()
                .map { categories ->
                    CategoriesState(categories = categories.map { it.toUi() })
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                    initialValue = CategoriesState(categories = emptyList()),
                )

        init {
            viewModelScope.launch {
                categorySeeder.ensureSeeded()
            }
        }

        fun addCategory(
            name: String,
            colorId: String,
        ) {
            val nextOrder = (state.value.categories.maxOfOrNull { it.sortOrder } ?: -1) + 1

            viewModelScope.launch {
                val id =
                    repository.insertCategory(
                        Category(
                            id = 0L,
                            name = name,
                            colorId = colorId,
                            sortOrder = nextOrder,
                            isHidden = false,
                            isSystem = false,
                        ),
                    )

                userActionLogger.log(
                    actionType = CREATE_CATEGORY,
                    entityType = CATEGORY,
                    entityId = id,
                    metadata = mapOf(CATEGORY_NAME to name),
                )
            }
        }

        fun renameCategory(
            categoryId: Long,
            newName: String,
        ) {
            val category = state.value.categories.firstOrNull { it.id == categoryId }

            viewModelScope.launch {
                repository.updateCategoryName(categoryId, newName)

                userActionLogger.log(
                    actionType = UPDATE_CATEGORY_NAME,
                    entityType = CATEGORY,
                    entityId = categoryId,
                    metadata =
                        mapOf(
                            CATEGORY_NAME to newName,
                            OLD_VALUE to (category?.name ?: ""),
                            NEW_VALUE to newName,
                        ),
                )
            }
        }

        fun updateCategoryColor(
            categoryId: Long,
            colorId: String,
        ) {
            val category = state.value.categories.firstOrNull { it.id == categoryId }

            viewModelScope.launch {
                repository.updateCategoryColor(categoryId, colorId)

                userActionLogger.log(
                    actionType = UPDATE_CATEGORY_COLOR,
                    entityType = CATEGORY,
                    entityId = categoryId,
                    metadata =
                        mapOf(
                            CATEGORY_NAME to (category?.name ?: ""),
                            OLD_VALUE to (category?.colorId ?: ""),
                            NEW_VALUE to colorId,
                        ),
                )
            }
        }

        fun updateCategoryVisibility(
            categoryId: Long,
            isHidden: Boolean,
        ) {
            if (categoryId == UNCATEGORIZED_ID) return
            val category = state.value.categories.firstOrNull { it.id == categoryId }

            viewModelScope.launch {
                repository.updateCategoryVisibility(categoryId, isHidden)

                userActionLogger.log(
                    actionType = UPDATE_CATEGORY_VISIBILITY,
                    entityType = CATEGORY,
                    entityId = categoryId,
                    metadata =
                        mapOf(
                            CATEGORY_NAME to (category?.name ?: ""),
                            OLD_VALUE to if (isHidden) CATEGORY_VISIBLE else CATEGORY_HIDDEN,
                            NEW_VALUE to if (isHidden) CATEGORY_HIDDEN else CATEGORY_VISIBLE,
                        ),
                )
            }
        }

        fun moveCategoryUp(categoryId: Long) {
            moveCategory(categoryId, -1)
        }

        fun moveCategoryDown(categoryId: Long) {
            moveCategory(categoryId, 1)
        }

        fun deleteCategory(categoryId: Long) {
            if (categoryId == UNCATEGORIZED_ID) return

            val category = state.value.categories.firstOrNull { it.id == categoryId }

            viewModelScope.launch {
                workoutRepository.reassignCategory(categoryId, UNCATEGORIZED_ID)
                repository.deleteCategory(categoryId)

                userActionLogger.log(
                    actionType = DELETE_CATEGORY,
                    entityType = CATEGORY,
                    entityId = categoryId,
                    metadata = mapOf(CATEGORY_NAME to (category?.name ?: "")),
                )
            }
        }

        private fun moveCategory(
            categoryId: Long,
            delta: Int,
        ) {
            val ordered = state.value.categories.sortedBy { it.sortOrder }
            val index = ordered.indexOfFirst { it.id == categoryId }
            val swapIndex = index + delta

            if (index == -1 || swapIndex !in ordered.indices) return

            val current = ordered[index]
            val target = ordered[swapIndex]

            viewModelScope.launch {
                repository.updateCategorySortOrder(current.id, target.sortOrder)
                repository.updateCategorySortOrder(target.id, current.sortOrder)

                userActionLogger.log(
                    actionType = REORDER_CATEGORY,
                    entityType = CATEGORY,
                    entityId = current.id,
                    metadata = mapOf(CATEGORY_NAME to current.name),
                )
            }
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
