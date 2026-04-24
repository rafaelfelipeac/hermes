package com.rafaelfelipeac.hermes.features.events.presentation

import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

data class EventsUiState(
    val events: List<WorkoutUi> = emptyList(),
    val categories: List<CategoryUi> = emptyList(),
)
