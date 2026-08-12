@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_COMPARISON_RULE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_ENTRY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_NORMALIZED_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PERSONAL_RECORD_UNIT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.PERSONAL_RECORD
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_PERSONAL_RECORD_FAMILY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_PERSONAL_RECORD_FAMILY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_PERSONAL_RECORD_ENTRY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_PERSONAL_RECORD_FAMILY
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.personalrecords.domain.PersonalRecordValueNormalizer
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit
import com.rafaelfelipeac.hermes.features.personalrecords.domain.repository.PersonalRecordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class PersonalRecordsViewModel
    @Inject
    constructor(
        private val repository: PersonalRecordsRepository,
        private val categoryRepository: CategoryRepository,
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        val state =
            combine(
                categoryRepository.observeCategories(),
                repository.observeFamilies(),
                repository.observeEntries(),
            ) { categories, families, entries ->
                PersonalRecordsState(
                    categories = categories,
                    families = families,
                    entries = entries,
                )
            }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                    initialValue = PersonalRecordsState(),
                )

        fun createFamily(
            categoryId: Long?,
            title: String,
            metricType: PersonalRecordMetricType,
            defaultUnit: PersonalRecordUnit,
            comparisonRule: PersonalRecordComparisonRule,
        ) {
            viewModelScope.launch {
                val families = repository.getFamilies()
                val sortOrder = (families.maxOfOrNull { it.sortOrder } ?: -1) + 1
                val now = Instant.now()
                val category = categoryById(categoryId)
                val familyId =
                    repository.insertFamily(
                        PersonalRecordFamily(
                            id = 0L,
                            categoryId = categoryId,
                            title = title,
                            metricType = metricType,
                            defaultUnit = defaultUnit,
                            comparisonRule = comparisonRule,
                            manualCurrentEntryId = null,
                            sortOrder = sortOrder,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )

                userActionLogger.log(
                    actionType = CREATE_PERSONAL_RECORD_FAMILY,
                    entityType = PERSONAL_RECORD,
                    entityId = familyId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to metricType.name,
                            PERSONAL_RECORD_UNIT to defaultUnit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to comparisonRule.name,
                        ),
                )
            }
        }

        fun updateFamily(
            familyId: Long,
            categoryId: Long?,
            title: String,
            comparisonRule: PersonalRecordComparisonRule,
        ) {
            viewModelScope.launch {
                val existingFamily = repository.getFamily(familyId) ?: return@launch
                val category = categoryById(categoryId)
                val now = Instant.now()
                repository.updateFamily(
                    existingFamily.copy(
                        categoryId = categoryId,
                        title = title,
                        comparisonRule = comparisonRule,
                        updatedAt = now,
                    ),
                )

                userActionLogger.log(
                    actionType = UPDATE_PERSONAL_RECORD_FAMILY,
                    entityType = PERSONAL_RECORD,
                    entityId = familyId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to existingFamily.metricType.name,
                            PERSONAL_RECORD_UNIT to existingFamily.defaultUnit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to comparisonRule.name,
                        ),
                )
            }
        }

        fun setManualCurrentEntry(
            familyId: Long,
            entryId: Long,
        ) {
            viewModelScope.launch {
                val family = repository.getFamily(familyId) ?: return@launch
                if (family.comparisonRule != PersonalRecordComparisonRule.MANUAL) return@launch
                if (family.manualCurrentEntryId == entryId) return@launch

                val entry = repository.getEntry(entryId) ?: return@launch
                if (entry.familyId != familyId) return@launch

                val category = categoryById(family.categoryId)
                repository.updateFamily(
                    family.copy(
                        manualCurrentEntryId = entryId,
                        updatedAt = Instant.now(),
                    ),
                )

                userActionLogger.log(
                    actionType = UPDATE_PERSONAL_RECORD_FAMILY,
                    entityType = PERSONAL_RECORD,
                    entityId = familyId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to family.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to category?.name.orEmpty(),
                            PERSONAL_RECORD_METRIC_TYPE to family.metricType.name,
                            PERSONAL_RECORD_UNIT to family.defaultUnit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to family.comparisonRule.name,
                        ),
                )
            }
        }

        fun deleteFamily(familyId: Long) {
            viewModelScope.launch {
                val family = repository.getFamily(familyId) ?: return@launch
                val category = categoryById(family.categoryId)
                repository.deleteFamily(familyId)

                userActionLogger.log(
                    actionType = DELETE_PERSONAL_RECORD_FAMILY,
                    entityType = PERSONAL_RECORD,
                    entityId = familyId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to family.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to family.metricType.name,
                            PERSONAL_RECORD_UNIT to family.defaultUnit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to family.comparisonRule.name,
                        ),
                )
            }
        }

        fun addEntry(input: PersonalRecordEntryInput) {
            viewModelScope.launch {
                val familyId = input.familyId
                val family = repository.getFamily(familyId) ?: return@launch
                val category = categoryById(family.categoryId)
                val normalizedValue = PersonalRecordValueNormalizer.normalize(input.value, input.unit)
                val now = Instant.now()
                val entryId =
                    repository.insertEntry(
                        PersonalRecordEntry(
                            id = 0L,
                            familyId = familyId,
                            value = input.value,
                            unit = input.unit,
                            customUnitLabel = input.customUnitLabel,
                            recordDate = input.recordDate,
                            note = input.note,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )

                userActionLogger.log(
                    actionType = CREATE_PERSONAL_RECORD_ENTRY,
                    entityType = PERSONAL_RECORD,
                    entityId = entryId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to family.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to family.metricType.name,
                            PERSONAL_RECORD_UNIT to input.unit.name,
                            PERSONAL_RECORD_COMPARISON_RULE to family.comparisonRule.name,
                            PERSONAL_RECORD_RECORD_DATE to input.recordDate.toString(),
                            PERSONAL_RECORD_NEW_VALUE to input.value.toString(),
                            PERSONAL_RECORD_NORMALIZED_VALUE to normalizedValue.toString(),
                        ),
                )
            }
        }

        fun updateEntry(
            entryId: Long,
            input: PersonalRecordEntryInput,
        ) {
            viewModelScope.launch {
                val existingEntry = repository.getEntry(entryId) ?: return@launch
                val familyId = input.familyId
                val family = repository.getFamily(familyId) ?: return@launch
                val category = categoryById(family.categoryId)
                val normalizedValue = PersonalRecordValueNormalizer.normalize(input.value, input.unit)
                val now = Instant.now()
                repository.updateEntry(
                    existingEntry.copy(
                        familyId = familyId,
                        value = input.value,
                        unit = input.unit,
                        customUnitLabel = input.customUnitLabel,
                        recordDate = input.recordDate,
                        note = input.note,
                        updatedAt = now,
                    ),
                )

                userActionLogger.log(
                    actionType = UPDATE_PERSONAL_RECORD_ENTRY,
                    entityType = PERSONAL_RECORD,
                    entityId = entryId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                            PERSONAL_RECORD_FAMILY_ID to familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to family.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to family.metricType.name,
                            PERSONAL_RECORD_UNIT to input.unit.name,
                            PERSONAL_RECORD_RECORD_DATE to input.recordDate.toString(),
                            PERSONAL_RECORD_OLD_VALUE to existingEntry.value.toString(),
                            PERSONAL_RECORD_NEW_VALUE to input.value.toString(),
                            PERSONAL_RECORD_NORMALIZED_VALUE to normalizedValue.toString(),
                        ),
                )
            }
        }

        fun deleteEntry(entryId: Long) {
            viewModelScope.launch {
                val entry = repository.getEntry(entryId) ?: return@launch
                val family = repository.getFamily(entry.familyId)
                val category = categoryById(family?.categoryId)
                repository.deleteEntry(entryId)
                if (family?.manualCurrentEntryId == entryId) {
                    repository.updateFamily(
                        family.copy(
                            manualCurrentEntryId = null,
                            updatedAt = Instant.now(),
                        ),
                    )
                }

                userActionLogger.log(
                    actionType = DELETE_PERSONAL_RECORD_ENTRY,
                    entityType = PERSONAL_RECORD,
                    entityId = entryId,
                    metadata =
                        mapOf(
                            PERSONAL_RECORD_ENTRY_ID to entryId.toString(),
                            PERSONAL_RECORD_FAMILY_ID to entry.familyId.toString(),
                            PERSONAL_RECORD_CATEGORY_ID to family?.categoryId?.toString().orEmpty(),
                            PERSONAL_RECORD_CATEGORY_NAME to (category?.name.orEmpty()),
                            PERSONAL_RECORD_METRIC_TYPE to (family?.metricType?.name.orEmpty()),
                            PERSONAL_RECORD_UNIT to entry.unit.name,
                            PERSONAL_RECORD_RECORD_DATE to entry.recordDate.toString(),
                            PERSONAL_RECORD_NEW_VALUE to entry.value.toString(),
                            PERSONAL_RECORD_NORMALIZED_VALUE to
                                PersonalRecordValueNormalizer
                                    .normalize(entry.value, entry.unit)
                                    .toString(),
                        ),
                )
            }
        }

        private suspend fun categoryById(categoryId: Long?): Category? {
            if (categoryId == null) return null
            return categoryRepository.getCategory(categoryId)
        }

        companion object {
            private const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
