package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper

import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

fun Workout.toUi(category: CategoryUi?): WorkoutUi {
    return WorkoutUi(
        id = id,
        dayOfWeek = dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        categoryId = category?.id,
        categoryColorId = category?.colorId,
        categoryName = category?.name,
        order = order,
        eventType = eventType,
        timeSlot = timeSlot,
    )
}
