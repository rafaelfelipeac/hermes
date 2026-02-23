package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout

internal fun Workout.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        weekStartDate = weekStartDate,
        dayOfWeek = dayOfWeek?.value,
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        eventType = eventType.name,
        timeSlot = timeSlot?.name,
        categoryId = categoryId,
        sortOrder = order,
    )
}
