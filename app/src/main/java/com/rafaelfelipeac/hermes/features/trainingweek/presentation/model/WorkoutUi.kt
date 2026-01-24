package com.rafaelfelipeac.hermes.features.trainingweek.presentation.model

import java.time.DayOfWeek

typealias WorkoutId = Long

data class WorkoutUi(
    val id: WorkoutId,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    val order: Int,
)
