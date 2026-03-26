package com.rafaelfelipeac.hermes.features.activity.presentation

import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityFiltersUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi

data class ActivityState(
    val sections: List<ActivitySectionUi> = emptyList(),
    val filters: ActivityFiltersUi = ActivityFiltersUi(),
)
