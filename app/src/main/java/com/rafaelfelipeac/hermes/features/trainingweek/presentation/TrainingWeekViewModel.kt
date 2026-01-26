@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.features.trainingweek.domain.model.Workout
import com.rafaelfelipeac.hermes.features.trainingweek.domain.repository.TrainingWeekRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrainingWeekViewModel @Inject constructor(
    private val repository: TrainingWeekRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val weekStartDate =
        selectedDate
            .map { it.with(TemporalAdjusters.previousOrSame(MONDAY)) }
            .distinctUntilChanged()

    private val workoutsForWeek = weekStartDate.flatMapLatest { weekStart ->
        repository.observeWorkoutsForWeek(weekStart).map { workouts ->
            workouts.map { it.toUi() }
        }
    }

    val state: StateFlow<TrainingWeekState> =
        combine(
            selectedDate,
            weekStartDate,
            workoutsForWeek,
        ) { selected, weekStart, workouts ->
            TrainingWeekState(
                selectedDate = selected,
                weekStartDate = weekStart,
                workouts = workouts,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
            initialValue = TrainingWeekState(
                selectedDate = selectedDate.value,
                weekStartDate =
                    selectedDate.value.with(TemporalAdjusters.previousOrSame(MONDAY)),
                workouts = emptyList(),
            ),
        )

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onWeekChanged(newSelectedDate: LocalDate) {
        selectedDate.value = newSelectedDate
    }

    fun addWorkout(
        type: String,
        description: String,
    ) {
        val (currentState, nextOrder) = getNextOrder()

        viewModelScope.launch {
            repository.addWorkout(
                weekStartDate = currentState.weekStartDate,
                dayOfWeek = null,
                type = type,
                description = description,
                order = nextOrder,
            )
        }
    }

    fun addRestDay() {
        val (currentState, nextOrder) = getNextOrder()

        viewModelScope.launch {
            repository.addRestDay(
                weekStartDate = currentState.weekStartDate,
                dayOfWeek = null,
                order = nextOrder,
            )
        }
    }

    fun moveWorkout(
        workoutId: Long,
        newDayOfWeek: DayOfWeek?,
        newOrder: Int,
    ) {
        val currentWorkouts = state.value.workouts
        val updated = updateWorkoutOrderWithRestDayRules(
            currentWorkouts,
            workoutId,
            newDayOfWeek,
            newOrder,
        )
        val changes = updated.mapNotNull { workout ->
            val original =
                currentWorkouts.firstOrNull { it.id == workout.id } ?: return@mapNotNull null

            if (original.dayOfWeek != workout.dayOfWeek || original.order != workout.order) {
                workout
            } else {
                null
            }
        }

        viewModelScope.launch {
            changes.forEach { workout ->
                repository.updateWorkoutDayAndOrder(
                    workoutId = workout.id,
                    dayOfWeek = workout.dayOfWeek,
                    order = workout.order,
                )
            }
        }
    }

    fun updateWorkoutCompletion(
        workoutId: Long,
        isCompleted: Boolean,
    ) = viewModelScope.launch {
        repository.updateWorkoutCompletion(workoutId, isCompleted)
    }

    fun updateWorkoutDetails(
        workoutId: Long,
        type: String,
        description: String,
        isRestDay: Boolean,
    ) = viewModelScope.launch {
        repository.updateWorkoutDetails(workoutId, type, description, isRestDay)
    }

    fun deleteWorkout(workoutId: Long) = viewModelScope.launch {
        repository.deleteWorkout(workoutId)
    }

    private fun getNextOrder(): Pair<TrainingWeekState, Int> {
        val currentState = state.value
        val nextOrder = currentState.workouts.count { it.dayOfWeek == null }

        return Pair(currentState, nextOrder)
    }

    companion object {
        const val STATE_SHARING_TIMEOUT_MS = 5_000L
        const val MIN_WORKOUT_ORDER = 0
    }
}

private fun Workout.toUi(): WorkoutUi {
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

private fun updateWorkoutOrderWithRestDayRules(
    workouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newOrder: Int,
): List<WorkoutUi> {
    val target = workouts.firstOrNull { it.id == workoutId } ?: return workouts
    val remaining = workouts.filterNot { it.id == workoutId }
    val sourceDay = target.dayOfWeek

    val adjusted = remaining.map { workout ->
        if (newDayOfWeek == workout.dayOfWeek && target.isRestDay) {
            workout.copy(dayOfWeek = null)
        } else if (!target.isRestDay && workout.isRestDay && workout.dayOfWeek == newDayOfWeek) {
            workout.copy(dayOfWeek = null)
        } else {
            workout
        }
    }.toMutableList()

    val updatedTarget = target.copy(dayOfWeek = newDayOfWeek)
    val destinationList =
        adjusted
            .filter { it.dayOfWeek == newDayOfWeek }
            .sortedBy { it.order }
            .toMutableList()
    val clampedOrder =
        newOrder.coerceIn(TrainingWeekViewModel.MIN_WORKOUT_ORDER, destinationList.size)
    destinationList.add(clampedOrder, updatedTarget)
    val normalizedDestination =
        destinationList.mapIndexed { index, workout ->
            workout.copy(order = index)
        }

    val sourceList =
        adjusted
            .filter { it.dayOfWeek == sourceDay }
            .sortedBy { it.order }
            .mapIndexed { index, workout -> workout.copy(order = index) }

    val tbdList =
        adjusted
            .filter { it.dayOfWeek == null && it.id != updatedTarget.id }
            .sortedBy { it.order }
            .mapIndexed { index, workout -> workout.copy(order = index) }

    val untouched =
        adjusted.filterNot {
            it.dayOfWeek == sourceDay || it.dayOfWeek == newDayOfWeek || it.dayOfWeek == null
        }

    return untouched + sourceList + tbdList + normalizedDestination
}
