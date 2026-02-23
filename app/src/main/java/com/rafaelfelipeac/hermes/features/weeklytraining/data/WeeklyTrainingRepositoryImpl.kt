package com.rafaelfelipeac.hermes.features.weeklytraining.data

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutDetailsUpdate
import com.rafaelfelipeac.hermes.features.weeklytraining.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class WeeklyTrainingRepositoryImpl
    @Inject
    constructor(
        private val workoutDao: WorkoutDao,
    ) : WeeklyTrainingRepository {
        override suspend fun getWorkoutsForWeek(weekStartDate: LocalDate): List<Workout> {
            return workoutDao.getWorkoutsForWeek(weekStartDate).map { it.toDomain() }
        }

        override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>> {
            return workoutDao.observeWorkoutsForWeek(weekStartDate).map { entities ->
                entities.map { it.toDomain() }
            }
        }

        override suspend fun addWorkout(request: AddWorkoutRequest): Long {
            val entity =
                WorkoutEntity(
                    weekStartDate = request.weekStartDate,
                    dayOfWeek = request.dayOfWeek?.value,
                    type = request.type,
                    description = request.description,
                    isCompleted = false,
                    isRestDay = false,
                    eventType = EventType.WORKOUT.name,
                    timeSlot = null,
                    categoryId = request.categoryId,
                    sortOrder = request.order,
                )

            return workoutDao.insert(entity)
        }

        override suspend fun addEvent(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            eventType: EventType,
            order: Int,
        ): Long {
            val entity =
                WorkoutEntity(
                    weekStartDate = weekStartDate,
                    dayOfWeek = dayOfWeek?.value,
                    type = EMPTY,
                    description = EMPTY,
                    isCompleted = false,
                    isRestDay = eventType == EventType.REST,
                    eventType = eventType.name,
                    timeSlot = null,
                    categoryId = null,
                    sortOrder = order,
                )

            return workoutDao.insert(entity)
        }

        override suspend fun insertWorkout(workout: Workout): Long {
            return workoutDao.insertOrReplace(workout.toEntity())
        }

        override suspend fun updateWorkoutDayAndOrder(
            workoutId: Long,
            dayOfWeek: DayOfWeek?,
            timeSlot: TimeSlot?,
            order: Int,
        ) = workoutDao.updateDayAndOrder(workoutId, dayOfWeek?.value, timeSlot?.name, order)

        override suspend fun updateWorkoutCompletion(
            workoutId: Long,
            isCompleted: Boolean,
        ) = workoutDao.updateCompletion(workoutId, isCompleted)

        override suspend fun updateWorkoutDetails(
            workoutId: Long,
            type: String,
            description: String,
            eventType: EventType,
            categoryId: Long?,
        ) = workoutDao.updateDetails(
            WorkoutDetailsUpdate(
                id = workoutId,
                type = type,
                description = description,
                isRestDay = eventType == EventType.REST,
                eventType = eventType.name,
                categoryId = categoryId,
            ),
        )

        override suspend fun deleteWorkout(workoutId: Long) = workoutDao.deleteById(workoutId)

        override suspend fun deleteWorkoutsForWeek(weekStartDate: LocalDate) {
            workoutDao.deleteByWeekStartDate(weekStartDate)
        }

        override suspend fun assignNullCategoryTo(uncategorizedId: Long) {
            workoutDao.assignNullCategoryTo(uncategorizedId)
        }

        override suspend fun reassignCategory(
            deletedCategoryId: Long,
            uncategorizedId: Long,
        ) {
            workoutDao.reassignCategory(deletedCategoryId, uncategorizedId)
        }

        override suspend fun replaceWorkoutsForWeek(
            weekStartDate: LocalDate,
            sourceWorkouts: List<Workout>,
        ) {
            workoutDao.replaceWorkoutsForWeek(
                weekStartDate = weekStartDate,
                workouts = buildReplacementEntities(weekStartDate, sourceWorkouts),
            )
        }
    }

private fun buildReplacementEntities(
    weekStartDate: LocalDate,
    sourceWorkouts: List<Workout>,
): List<WorkoutEntity> {
    return sourceWorkouts
        .sortedWith(
            compareBy(
                { it.dayOfWeek?.value ?: Int.MAX_VALUE },
                { it.order },
                { it.id },
            ),
        ).map { workout ->
            val isRestDay = workout.isRestDay
            WorkoutEntity(
                weekStartDate = weekStartDate,
                dayOfWeek = workout.dayOfWeek?.value,
                type = if (isRestDay) EMPTY else workout.type,
                description = if (isRestDay) EMPTY else workout.description,
                isCompleted = false,
                isRestDay = isRestDay,
                eventType = workout.eventType.name,
                timeSlot = workout.timeSlot?.name,
                categoryId = if (isRestDay) null else workout.categoryId,
                sortOrder = workout.order,
            )
        }
}

private fun WorkoutEntity.toDomain(): Workout {
    return Workout(
        id = id,
        weekStartDate = weekStartDate,
        dayOfWeek = dayOfWeek?.let(DayOfWeek::of),
        type = type,
        description = description,
        isCompleted = isCompleted,
        isRestDay = isRestDay,
        eventType =
            runCatching { EventType.valueOf(eventType) }
                .getOrDefault(if (isRestDay) EventType.REST else EventType.WORKOUT),
        timeSlot = timeSlot?.let { raw -> runCatching { TimeSlot.valueOf(raw) }.getOrNull() },
        categoryId = categoryId,
        order = sortOrder,
    )
}
