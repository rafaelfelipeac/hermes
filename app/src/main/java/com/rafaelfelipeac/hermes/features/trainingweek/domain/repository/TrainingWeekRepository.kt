package com.rafaelfelipeac.hermes.features.trainingweek.domain.repository

import com.rafaelfelipeac.hermes.features.trainingweek.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

interface TrainingWeekRepository {

    fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>>

    suspend fun addWorkout(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        type: String,
        description: String,
        isRestDay: Boolean,
        order: Int
    ): Long

    suspend fun addRestDay(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        order: Int
    ): Long

    suspend fun updateWorkoutDayAndOrder(
        workoutId: Long,
        dayOfWeek: DayOfWeek?,
        order: Int
    )

    suspend fun updateWorkoutCompletion(
        workoutId: Long,
        isCompleted: Boolean
    )
}
