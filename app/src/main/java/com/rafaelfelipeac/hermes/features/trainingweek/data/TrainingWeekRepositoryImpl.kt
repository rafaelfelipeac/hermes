package com.rafaelfelipeac.hermes.features.trainingweek.data

import com.rafaelfelipeac.hermes.features.trainingweek.data.local.WorkoutDao
import com.rafaelfelipeac.hermes.features.trainingweek.data.local.WorkoutEntity
import com.rafaelfelipeac.hermes.features.trainingweek.domain.model.Workout
import com.rafaelfelipeac.hermes.features.trainingweek.domain.repository.TrainingWeekRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class TrainingWeekRepositoryImpl @Inject constructor(
    private val workoutDao: WorkoutDao
) : TrainingWeekRepository {

    override fun observeWorkoutsForWeek(weekStartDate: LocalDate): Flow<List<Workout>> {
        return workoutDao.observeWorkoutsForWeek(weekStartDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addWorkout(
        weekStartDate: LocalDate,
        dayOfWeek: DayOfWeek?,
        type: String,
        description: String,
        order: Int
    ): Long {
        val entity = WorkoutEntity(
            weekStartDate = weekStartDate,
            dayOfWeek = dayOfWeek?.value,
            type = type,
            description = description,
            isCompleted = false,
            sortOrder = order
        )

        return workoutDao.insert(entity)
    }

    override suspend fun updateWorkoutDayAndOrder(
        workoutId: Long,
        dayOfWeek: DayOfWeek?,
        order: Int
    ) {
        workoutDao.updateDayAndOrder(workoutId, dayOfWeek?.value, order)
    }

    override suspend fun updateWorkoutCompletion(
        workoutId: Long,
        isCompleted: Boolean
    ) {
        workoutDao.updateCompletion(workoutId, isCompleted)
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
        order = sortOrder
    )
}
