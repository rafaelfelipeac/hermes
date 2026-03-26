package com.rafaelfelipeac.hermes.features.activity.presentation.model

data class ActivityFiltersUi(
    val primaryFilters: List<ActivityPrimaryFilterUi> = emptyList(),
    val selectedPrimaryFilter: ActivityPrimaryFilter = ActivityPrimaryFilter.ALL,
    val categoryFilters: List<ActivityCategoryFilterUi> = emptyList(),
    val weekFilters: List<ActivityWeekFilterUi> = emptyList(),
    val isAnyFilterActive: Boolean = false,
)
