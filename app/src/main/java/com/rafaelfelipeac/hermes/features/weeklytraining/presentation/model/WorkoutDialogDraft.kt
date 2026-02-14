package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

data class WorkoutDialogDraft(
    val workoutId: Long?,
    val type: String,
    val description: String,
    val categoryId: Long?,
)
