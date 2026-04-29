package com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek
import java.time.LocalDate

@Suppress("TooManyFunctions")
interface WeeklyTrainingRepository {
    fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>>

    fun observeAllWorkouts(): Flow<List<Workout>>

    fun observeWorkoutsByEventType(eventType: EventType): Flow<List<Workout>>

    fun observeWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): Flow<List<Workout>>

    suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout>

    suspend fun getWorkoutsForWeekStarts(weekStartDates: List<LocalDate>): List<Workout>

    suspend fun addWorkout(request: AddWorkoutRequest): Long

    suspend fun addEvent(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        eventType: EventType,
        order: Int,
    ): Long

    @Deprecated("Use addEvent with EventType")
    suspend fun addRestDay(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        order: Int,
    ): Long = addEvent(weekStartDate = weekStartDate, dayOfWeek = dayOfWeek, eventType = EventType.REST, order = order)

    suspend fun insertWorkout(workout: Workout): Long

    suspend fun updateWorkoutDayAndOrder(
        workoutId: Long,
        dayOfWeek: DayOfWeek?,
        timeSlot: TimeSlot?,
        order: Int,
    )

    suspend fun updateWorkoutSchedule(
        workoutId: Long,
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        timeSlot: TimeSlot?,
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
        eventType: EventType,
        categoryId: Long?,
    )

    @Deprecated("Use updateWorkoutDetails with EventType")
    suspend fun updateWorkoutDetails(
        workoutId: Long,
        type: String,
        description: String,
        isRestDay: Boolean,
        categoryId: Long?,
    ) = updateWorkoutDetails(
        workoutId = workoutId,
        type = type,
        description = description,
        eventType = if (isRestDay) EventType.REST else EventType.WORKOUT,
        categoryId = categoryId,
    )

    suspend fun assignNullCategoryTo(uncategorizedId: Long)

    suspend fun reassignCategory(
        deletedCategoryId: Long,
        uncategorizedId: Long,
    )

    suspend fun deleteWorkout(workoutId: Long)

    suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate)

    suspend fun replaceWorkoutsForWeek(
        weekStartDate: LocalDate,
        sourceWorkouts: List<Workout>,
    )

    suspend fun replaceWorkoutsForDisplayWeek(
        targetStorageWeekStarts: List<LocalDate>,
        targetDisplayWeekStart: LocalDate,
        targetUnassignedStorageWeekStart: LocalDate,
        replacementWorkouts: List<Workout>,
    ): Result<List<Workout>> =
        runCatching {
            val targetDates = (0L until DAYS_IN_WEEK).map { offset -> targetDisplayWeekStart.plusDays(offset) }.toSet()
            val targetWorkouts =
                getWorkoutsForWeekStarts(targetStorageWeekStarts).filter { workout ->
                    val dayOfWeek = workout.dayOfWeek

                    if (dayOfWeek == null) {
                        workout.weekStartDate == targetUnassignedStorageWeekStart
                    } else {
                        workout.weekStartDate.plusDays((dayOfWeek.value - 1).toLong()) in targetDates
                    }
                }

            targetWorkouts.forEach { workout ->
                deleteWorkout(workout.id)
            }

            replacementWorkouts.forEach { workout ->
                insertWorkout(workout)
            }

            targetWorkouts
        }
}

private const val DAYS_IN_WEEK = 7L
