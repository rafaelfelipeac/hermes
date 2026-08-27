package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityCategoryFilterUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityFiltersUi
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
        private val categoriesFlow =
            categoryRepository.observeCategories().map { categories ->
                categories.map { it.toUi() }
            }
        private val actionsAndCategories =
            combine(repository.observeActions(), categoriesFlow) { actions, categories ->
                actions to categories
            }
        private val filterSelection =
            combine(
                selectedPrimaryFilter,
                selectedCategoryId,
                selectedWeekStartDate,
            ) { primaryFilter, categoryId, weekStartDate ->
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
                val categoryAliasesById =
                    buildCategoryAliasesById(
                        actions = actions,
                        categories = categories,
                        formatter = formatter,
                    )
                val filteredActions =
                    filterActions(
                        actions = actions,
                        context =
                            ActivityActionFilterContext(
                                primaryFilter = primaryFilter,
                                categoryId = resolvedCategoryId,
                                categories = categories,
                                categoryAliasesById = categoryAliasesById,
                                weekStartDate = resolvedWeekStartDate,
                            ),
                        formatter = formatter,
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
                    .sortedWith(
                        compareBy<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi> {
                            it.id == UNCATEGORIZED_ID
                        }.thenBy { it.sortOrder },
                    )
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
            val dateFormatter =
                DateTimeFormatterBuilder()
                    .appendPattern(pattern)
                    .toFormatter(currentLocale)
            val weekStartDates =
                actions
                    .mapNotNull { action ->
                        weekStartDateForAction(
                            action = action,
                            formatter = formatter,
                        )
                    }
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
                    label = weekStartDate.format(dateFormatter),
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

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }

internal fun String.toUserActionTypeOrNull(): UserActionType? {
    return runCatching { UserActionType.valueOf(this) }.getOrNull()
}

internal fun String.toUserActionEntityTypeOrNull(): UserActionEntityType? {
    return runCatching { UserActionEntityType.valueOf(this) }.getOrNull()
}
