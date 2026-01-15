package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WorkoutUi
import com.rafaelfelipeac.hermes.features.trainingweek.domain.model.Workout
import com.rafaelfelipeac.hermes.features.trainingweek.domain.repository.TrainingWeekRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@HiltViewModel
class TrainingWeekViewModel @Inject constructor(
    private val repository: TrainingWeekRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val weekStartDate = selectedDate
        .map { it.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
        .distinctUntilChanged()

    private val workoutsForWeek = weekStartDate.flatMapLatest { weekStart ->
        repository.observeWorkoutsForWeek(weekStart).map { workouts ->
            workouts.map { it.toUi() }
        }
    }

    val state: StateFlow<TrainingWeekState> = combine(
        selectedDate,
        weekStartDate,
        workoutsForWeek
    ) { selected, weekStart, workouts ->
        TrainingWeekState(
            selectedDate = selected,
            weekStartDate = weekStart,
            workouts = workouts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TrainingWeekState(
            selectedDate = selectedDate.value,
            weekStartDate = selectedDate.value
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
            workouts = emptyList()
        )
    )

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onWeekChanged(newSelectedDate: LocalDate) {
        selectedDate.value = newSelectedDate
    }

    fun addWorkout(type: String, description: String) {
        val currentState = state.value
        val nextOrder = currentState.workouts.count { it.dayOfWeek == null }

        viewModelScope.launch {
            repository.addWorkout(
                weekStartDate = currentState.weekStartDate,
                dayOfWeek = null,
                type = type,
                description = description,
                order = nextOrder
            )
        }
    }

    fun moveWorkout(workoutId: Long, newDayOfWeek: DayOfWeek?, newOrder: Int) {
        val currentWorkouts = state.value.workouts
        val updated = updateWorkoutOrder(currentWorkouts, workoutId, newDayOfWeek, newOrder)
        val changes = updated.mapNotNull { workout ->
            val original = currentWorkouts.firstOrNull { it.id == workout.id } ?: return@mapNotNull null

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
                    order = workout.order
                )
            }
        }
    }

    fun updateWorkoutCompletion(workoutId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateWorkoutCompletion(workoutId, isCompleted)
        }
    }
}

private fun Workout.toUi(): WorkoutUi {
    return WorkoutUi(
        id = id,
        dayOfWeek = dayOfWeek,
        type = type,
        description = description,
        isCompleted = isCompleted,
        order = order
    )
}

private fun updateWorkoutOrder(
    workouts: List<WorkoutUi>,
    workoutId: Long,
    newDayOfWeek: DayOfWeek?,
    newOrder: Int
): List<WorkoutUi> {
    val target = workouts.firstOrNull { it.id == workoutId } ?: return workouts
    val remaining = workouts.filterNot { it.id == workoutId }
    val sourceDay = target.dayOfWeek
    val updatedByDay = mutableMapOf<DayOfWeek?, List<WorkoutUi>>()

    if (sourceDay == newDayOfWeek) {
        val reordered = remaining
            .filter { it.dayOfWeek == sourceDay }
            .sortedBy { it.order }
            .toMutableList()
        val clampedOrder = newOrder.coerceIn(0, reordered.size)

        reordered.add(clampedOrder, target.copy(dayOfWeek = newDayOfWeek))

        updatedByDay[sourceDay] = reordered.mapIndexed { index, workout ->
            workout.copy(order = index)
        }
    } else {
        val sourceList = remaining
            .filter { it.dayOfWeek == sourceDay }
            .sortedBy { it.order }

        updatedByDay[sourceDay] = sourceList.mapIndexed { index, workout ->
            workout.copy(order = index)
        }

        val destinationList = remaining
            .filter { it.dayOfWeek == newDayOfWeek }
            .sortedBy { it.order }
            .toMutableList()
        val clampedOrder = newOrder.coerceIn(0, destinationList.size)

        destinationList.add(clampedOrder, target.copy(dayOfWeek = newDayOfWeek))

        updatedByDay[newDayOfWeek] = destinationList.mapIndexed { index, workout ->
            workout.copy(order = index)
        }
    }

    val updatedDays = updatedByDay.keys
    val untouched = remaining.filterNot { updatedDays.contains(it.dayOfWeek) }

    return untouched + updatedByDay.values.flatten()
}
