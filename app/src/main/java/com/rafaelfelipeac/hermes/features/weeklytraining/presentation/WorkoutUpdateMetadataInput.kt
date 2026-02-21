package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

data class WorkoutUpdateMetadataInput(
    val weekStartDate: String,
    val original: WorkoutUi?,
    val type: String,
    val description: String,
    val isRestDay: Boolean,
    val oldCategoryName: String?,
    val newCategoryName: String?,
)
