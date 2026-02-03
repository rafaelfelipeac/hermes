package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

fun Workout.toUi(): WorkoutUi {
    return WorkoutUi(
        id = id,
        dayOfWeek = dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        order = order,
    )
}
