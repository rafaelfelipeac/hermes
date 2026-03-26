package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityCategoryFilterUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityFiltersUi
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilterUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityWeekFilterUi
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel
    @Inject
    constructor(
        repository: UserActionRepository,
        categoryRepository: CategoryRepository,
        private val stringProvider: StringProvider,
    ) : ViewModel() {
        private val locale = MutableStateFlow(Locale.getDefault())
        private val selectedPrimaryFilter = MutableStateFlow(ActivityPrimaryFilter.ALL)
        private val selectedCategoryId = MutableStateFlow<Long?>(null)
        private val selectedWeekStartDate = MutableStateFlow<LocalDate?>(null)
        private val formatter = ActivityUiFormatter(stringProvider)
        private val categoriesFlow = categoryRepository.observeCategories().map { categories -> categories.map { it.toUi() } }
        private val actionsAndCategories =
            combine(repository.observeActions(), categoriesFlow) { actions, categories ->
                actions to categories
            }
        private val filterSelection =
            combine(selectedPrimaryFilter, selectedCategoryId, selectedWeekStartDate) { primaryFilter, categoryId, weekStartDate ->
                FilterSelection(primaryFilter, categoryId, weekStartDate)
            }

        val state: StateFlow<ActivityState> =
            combine(
                actionsAndCategories,
                locale,
                filterSelection,
            ) { (actions, categories), currentLocale, filterSelection ->
                val primaryFilter = filterSelection.primaryFilter
                val categoryId = filterSelection.categoryId
                val weekStartDate = filterSelection.weekStartDate
                val categoryFilters = buildCategoryFilters(categories, categoryId, primaryFilter)
                val weekFilters = buildWeekFilters(actions, currentLocale, weekStartDate, primaryFilter)
                val resolvedCategoryId = resolveSelectedCategoryId(categoryFilters, categoryId, primaryFilter)
                val resolvedWeekStartDate = resolveSelectedWeekStartDate(weekFilters, weekStartDate, primaryFilter)
                val categoryAliasesById = buildCategoryAliasesById(actions, categories)
                val filteredActions =
                    filterActions(
                        actions = actions,
                        primaryFilter = primaryFilter,
                        categoryId = resolvedCategoryId,
                        categories = categories,
                        categoryAliasesById = categoryAliasesById,
                        weekStartDate = resolvedWeekStartDate,
                    )

                ActivityState(
                    sections = buildSections(filteredActions, currentLocale),
                    filters =
                        ActivityFiltersUi(
                            primaryFilters = buildPrimaryFilters(),
                            selectedPrimaryFilter = primaryFilter,
                            categoryFilters = categoryFilters,
                            weekFilters = weekFilters,
                            isAnyFilterActive = primaryFilter != ActivityPrimaryFilter.ALL,
                        ),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = ActivityState(),
            )

        fun updateLocale(currentLocale: Locale) {
            locale.value = currentLocale
        }

        fun selectPrimaryFilter(filter: ActivityPrimaryFilter) {
            selectedPrimaryFilter.value = filter
        }

        fun selectCategoryFilter(categoryId: Long) {
            selectedCategoryId.value = categoryId
            selectedPrimaryFilter.value = ActivityPrimaryFilter.CATEGORY
        }

        fun selectWeekFilter(weekStartDate: LocalDate) {
            selectedWeekStartDate.value = weekStartDate
            selectedPrimaryFilter.value = ActivityPrimaryFilter.WEEK
        }

        fun clearFilters() {
            selectedPrimaryFilter.value = ActivityPrimaryFilter.ALL
            selectedCategoryId.value = null
            selectedWeekStartDate.value = null
        }

        private fun buildSections(
            actions: List<UserActionRecord>,
            currentLocale: Locale,
        ): List<ActivitySectionUi> {
            if (actions.isEmpty()) return emptyList()

            val zoneId = ZoneId.systemDefault()
            val grouped =
                actions.groupBy { action ->
                    Instant.ofEpochMilli(action.timestamp).atZone(zoneId).toLocalDate()
                }

            return grouped.entries
                .sortedByDescending { it.key }
                .map { (date, records) ->
                    ActivitySectionUi(
                        date = date,
                        items = records.map { record -> toUi(record, zoneId, currentLocale) },
                    )
                }
        }

        private fun toUi(
            record: UserActionRecord,
            zoneId: ZoneId,
            currentLocale: Locale,
        ): ActivityItemUi {
            val metadata = formatter.parseMetadata(record.metadata)
            val time = formatter.formatTime(record.timestamp, zoneId, currentLocale)
            val title = formatter.buildTitle(record, metadata)
            val subtitle = formatter.buildSubtitle(record, metadata, currentLocale)

            return ActivityItemUi(
                id = record.id,
                title = title,
                subtitle = subtitle,
                time = time,
            )
        }

        private fun buildPrimaryFilters(): List<ActivityPrimaryFilterUi> {
            return listOf(
                ActivityPrimaryFilterUi(ActivityPrimaryFilter.ALL, stringProvider.get(R.string.activity_filter_all)),
                ActivityPrimaryFilterUi(
                    ActivityPrimaryFilter.COMPLETIONS,
                    stringProvider.get(R.string.activity_filter_completions),
                ),
                ActivityPrimaryFilterUi(
                    ActivityPrimaryFilter.PLANNING,
                    stringProvider.get(R.string.activity_filter_planning),
                ),
                ActivityPrimaryFilterUi(
                    ActivityPrimaryFilter.CATEGORIES,
                    stringProvider.get(R.string.settings_categories),
                ),
                ActivityPrimaryFilterUi(
                    ActivityPrimaryFilter.CATEGORY,
                    stringProvider.get(R.string.workout_dialog_add_workout_category),
                ),
                ActivityPrimaryFilterUi(
                    ActivityPrimaryFilter.SETTINGS,
                    stringProvider.get(R.string.settings_nav_label),
                ),
                ActivityPrimaryFilterUi(ActivityPrimaryFilter.WEEK, stringProvider.get(R.string.activity_filter_week)),
            )
        }

        private fun buildCategoryFilters(
            categoryNames: List<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi>,
            selectedCategoryId: Long?,
            primaryFilter: ActivityPrimaryFilter,
        ): List<ActivityCategoryFilterUi> {
            val activeCategories =
                categoryNames
                    .sortedWith(compareBy<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi> { it.id == UNCATEGORIZED_ID }.thenBy { it.sortOrder })
                    .filter { !it.isHidden }
            val activeSelection =
                resolveSelectedCategoryId(
                    categoryFilters = emptyList(),
                    selectedCategoryId = selectedCategoryId,
                    primaryFilter = primaryFilter,
                    availableIds = activeCategories.map { it.id },
                )

            return activeCategories.map { category ->
                ActivityCategoryFilterUi(
                    id = category.id,
                    name = category.name,
                    isSelected = primaryFilter == ActivityPrimaryFilter.CATEGORY && category.id == activeSelection,
                )
            }
        }

        private fun buildWeekFilters(
            actions: List<UserActionRecord>,
            currentLocale: Locale,
            selectedWeekStartDate: LocalDate?,
            primaryFilter: ActivityPrimaryFilter,
        ): List<ActivityWeekFilterUi> {
            val pattern = stringProvider.get(R.string.activity_week_date_pattern)
            val formatter =
                DateTimeFormatterBuilder()
                    .appendPattern(pattern)
                    .toFormatter(currentLocale)
            val weekStartDates =
                actions
                    .mapNotNull(::weekStartDateForAction)
                    .distinct()
                    .sortedDescending()
            val activeSelection =
                resolveSelectedWeekStartDate(
                    weekFilters = emptyList(),
                    selectedWeekStartDate = selectedWeekStartDate,
                    primaryFilter = primaryFilter,
                    availableWeekStartDates = weekStartDates,
                )

            return weekStartDates.map { weekStartDate ->
                ActivityWeekFilterUi(
                    weekStartDate = weekStartDate,
                    label = weekStartDate.format(formatter),
                    isSelected = primaryFilter == ActivityPrimaryFilter.WEEK && weekStartDate == activeSelection,
                )
            }
        }

        private fun resolveSelectedCategoryId(
            categoryFilters: List<ActivityCategoryFilterUi>,
            selectedCategoryId: Long?,
            primaryFilter: ActivityPrimaryFilter,
            availableIds: List<Long> = categoryFilters.map { it.id },
        ): Long? {
            if (primaryFilter != ActivityPrimaryFilter.CATEGORY) return null

            return when {
                selectedCategoryId in availableIds -> selectedCategoryId
                else -> availableIds.firstOrNull()
            }
        }

        private fun resolveSelectedWeekStartDate(
            weekFilters: List<ActivityWeekFilterUi>,
            selectedWeekStartDate: LocalDate?,
            primaryFilter: ActivityPrimaryFilter,
            availableWeekStartDates: List<LocalDate> = weekFilters.map { it.weekStartDate },
        ): LocalDate? {
            if (primaryFilter != ActivityPrimaryFilter.WEEK) return null

            return when {
                selectedWeekStartDate in availableWeekStartDates -> selectedWeekStartDate
                else -> availableWeekStartDates.firstOrNull()
            }
        }

        private fun filterActions(
            actions: List<UserActionRecord>,
            primaryFilter: ActivityPrimaryFilter,
            categoryId: Long?,
            categories: List<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi>,
            categoryAliasesById: Map<Long, Set<String>>,
            weekStartDate: LocalDate?,
        ): List<UserActionRecord> {
            val categoryNameById = categories.associate { it.id to it.name }

            return actions.filter { action ->
                when (primaryFilter) {
                    ActivityPrimaryFilter.ALL -> true
                    ActivityPrimaryFilter.COMPLETIONS -> isCompletionAction(action)
                    ActivityPrimaryFilter.PLANNING -> isPlanningAction(action)
                    ActivityPrimaryFilter.CATEGORIES -> isCategoryAction(action)
                    ActivityPrimaryFilter.CATEGORY -> {
                        val selectedCategoryName = categoryId?.let(categoryNameById::get)
                        val categoryIds = categoryIdsForAction(action)
                        val categoryNames = categoryNamesForAction(action)
                        val aliases = categoryId?.let(categoryAliasesById::get).orEmpty()

                        (categoryId != null && categoryId in categoryIds) ||
                            (selectedCategoryName != null && selectedCategoryName in categoryNames) ||
                            aliases.any { it in categoryNames }
                    }
                    ActivityPrimaryFilter.SETTINGS -> isSettingsAction(action)
                    ActivityPrimaryFilter.WEEK -> weekStartDate != null && weekStartDateForAction(action) == weekStartDate
                }
            }
        }

        private fun buildCategoryAliasesById(
            actions: List<UserActionRecord>,
            categories: List<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi>,
        ): Map<Long, Set<String>> {
            val aliases =
                categories.associate { category ->
                    category.id to mutableSetOf(category.name)
                }.toMutableMap()

            actions.forEach { action ->
                if (action.entityType.toUserActionEntityTypeOrNull() != UserActionEntityType.CATEGORY) return@forEach
                val categoryId = action.entityId ?: return@forEach
                val metadata = formatter.parseMetadata(action.metadata)
                val names = aliases.getOrPut(categoryId) { mutableSetOf() }

                metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() }?.let(names::add)

                if (action.actionType.toUserActionTypeOrNull() == UserActionType.UPDATE_CATEGORY_NAME) {
                    metadata[OLD_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
                    metadata[NEW_VALUE]?.takeIf { it.isNotBlank() }?.let(names::add)
                }
            }

            return aliases.mapValues { (_, names) -> names.toSet() }
        }

        private fun isCompletionAction(action: UserActionRecord): Boolean {
            val actionType = action.actionType.toUserActionTypeOrNull() ?: return false

            return actionType in completionActions
        }

        private fun isPlanningAction(action: UserActionRecord): Boolean {
            val actionType = action.actionType.toUserActionTypeOrNull() ?: return false

            return actionType in planningActions
        }

        private fun isCategoryAction(action: UserActionRecord): Boolean {
            val entityType = action.entityType.toUserActionEntityTypeOrNull()

            return entityType == UserActionEntityType.CATEGORY
        }

        private fun isSettingsAction(action: UserActionRecord): Boolean {
            val entityType = action.entityType.toUserActionEntityTypeOrNull()

            return entityType == UserActionEntityType.SETTINGS || entityType == UserActionEntityType.APP
        }

        private fun categoryNamesForAction(action: UserActionRecord): Set<String> {
            val metadata = formatter.parseMetadata(action.metadata)

            return setOfNotNull(
                metadata[CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                metadata[NEW_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
                metadata[OLD_CATEGORY_NAME]?.takeIf { it.isNotBlank() },
            )
        }

        private fun categoryIdsForAction(action: UserActionRecord): Set<Long> {
            val metadata = formatter.parseMetadata(action.metadata)

            return setOfNotNull(
                metadata[CATEGORY_ID]?.toLongOrNull(),
                metadata[OLD_CATEGORY_ID]?.toLongOrNull(),
                metadata[NEW_CATEGORY_ID]?.toLongOrNull(),
            )
        }

        private fun weekStartDateForAction(action: UserActionRecord): LocalDate? {
            val metadata = formatter.parseMetadata(action.metadata)

            return metadata[WEEK_START_DATE]?.let { value -> runCatching { LocalDate.parse(value) }.getOrNull() }
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L

            val completionActions =
                setOf(
                    UserActionType.COMPLETE_WORKOUT,
                    UserActionType.COMPLETE_WEEK_WORKOUTS,
                    UserActionType.UNDO_COMPLETE_WORKOUT,
                    UserActionType.INCOMPLETE_WORKOUT,
                    UserActionType.UNDO_INCOMPLETE_WORKOUT,
                )

            val planningActions =
                setOf(
                    UserActionType.CREATE_WORKOUT,
                    UserActionType.UPDATE_WORKOUT,
                    UserActionType.DELETE_WORKOUT,
                    UserActionType.UNDO_DELETE_WORKOUT,
                    UserActionType.REORDER_WORKOUT,
                    UserActionType.MOVE_WORKOUT_BETWEEN_DAYS,
                    UserActionType.UNDO_REORDER_WORKOUT_SAME_DAY,
                    UserActionType.UNDO_MOVE_WORKOUT_BETWEEN_DAYS,
                    UserActionType.CREATE_REST_DAY,
                    UserActionType.CREATE_BUSY,
                    UserActionType.CREATE_SICK,
                    UserActionType.UPDATE_REST_DAY,
                    UserActionType.UPDATE_BUSY,
                    UserActionType.UPDATE_SICK,
                    UserActionType.DELETE_REST_DAY,
                    UserActionType.DELETE_BUSY,
                    UserActionType.DELETE_SICK,
                    UserActionType.UNDO_DELETE_REST_DAY,
                    UserActionType.UNDO_DELETE_BUSY,
                    UserActionType.UNDO_DELETE_SICK,
                    UserActionType.CONVERT_WORKOUT_TO_REST_DAY,
                    UserActionType.CONVERT_REST_DAY_TO_WORKOUT,
                    UserActionType.COPY_LAST_WEEK,
                    UserActionType.UNDO_COPY_LAST_WEEK,
                    UserActionType.OPEN_WEEK,
                )
        }
    }

private fun String.toUserActionTypeOrNull(): UserActionType? {
    return runCatching { UserActionType.valueOf(this) }.getOrNull()
}

private fun String.toUserActionEntityTypeOrNull(): UserActionEntityType? {
    return runCatching { UserActionEntityType.valueOf(this) }.getOrNull()
}
