package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

import java.time.LocalDate

data class WorkoutDialogDraft(
    val workoutId: Long?,
    val type: String,
    val description: String,
    val categoryId: Long?,
    val eventDate: LocalDate? = null,
    val isRaceEvent: Boolean = false,
)
