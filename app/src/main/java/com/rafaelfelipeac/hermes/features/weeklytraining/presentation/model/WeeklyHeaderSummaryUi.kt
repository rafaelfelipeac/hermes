package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

data class WeeklyHeaderSummaryUi(
    val plannedItems: Int,
    val completedItems: Int,
    val plannedWorkouts: Int,
    val completedWorkouts: Int,
    val plannedRaceEvents: Int,
    val completedRaceEvents: Int,
    val plannedRestEvents: Int,
    val plannedBusyEvents: Int,
    val plannedSickEvents: Int,
    val progress: Float,
)
