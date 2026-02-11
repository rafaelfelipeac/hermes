package com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

interface WeeklyTrainingRepository {
    fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>>

    suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout>

    suspend fun addWorkout(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        type: String,
        description: String,
        order: Int,
    ): Long

    suspend fun addRestDay(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        order: Int,
    ): Long

    suspend fun insertWorkout(workout: Workout): Long

    suspend fun updateWorkoutDayAndOrder(
        workoutId: Long,
        dayOfWeek: DayOfWeek?,
        order: Int,
    )

    suspend fun updateWorkoutCompletion(
        workoutId: Long,
        isCompleted: Boolean,
    )

    suspend fun updateWorkoutDetails(
        workoutId: Long,
        type: String,
        description: String,
        isRestDay: Boolean,
    )

    suspend fun deleteWorkout(workoutId: Long)

    suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate)

    suspend fun replaceWorkoutsForWeek(
        weekStartDate: LocalDate,
        sourceWorkouts: List<Workout>,
    )
}
