package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

data class WeeklyHeaderSummaryUi(
    val plannedWorkouts: Int,
    val completedWorkouts: Int,
    val plannedRestEvents: Int,
    val plannedBusyEvents: Int,
    val plannedSickEvents: Int,
    val progress: Float,
)
