@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CONVERT_REST_DAY_TO_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CONVERT_WORKOUT_TO_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.OPEN_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.REORDER_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
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
class WeeklyTrainingViewModel
    @Inject
    constructor(
        private val repository: WeeklyTrainingRepository,
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val weekStartDate =
            selectedDate
                .map { it.with(TemporalAdjusters.previousOrSame(MONDAY)) }
                .distinctUntilChanged()

        private val workoutsForWeek =
            weekStartDate.flatMapLatest { weekStart ->
                repository.observeWorkoutsForWeek(weekStart).map { workouts ->
                    workouts.map { it.toUi() }
                }
            }

        val state: StateFlow<WeeklyTrainingState> =
            combine(
                selectedDate,
                weekStartDate,
                workoutsForWeek,
            ) { selected, weekStart, workouts ->
                WeeklyTrainingState(
                    selectedDate = selected,
                    weekStartDate = weekStart,
                    workouts = workouts,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue =
                    WeeklyTrainingState(
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
            val previousWeekStartDate = state.value.weekStartDate
            val newWeekStartDate = newSelectedDate.with(TemporalAdjusters.previousOrSame(MONDAY))

            selectedDate.value = newSelectedDate

            viewModelScope.launch {
                userActionLogger.log(
                    actionType = OPEN_WEEK,
                    entityType = WEEK,
                    metadata =
                        mapOf(
                            OLD_WEEK_START_DATE to previousWeekStartDate.toString(),
                            NEW_WEEK_START_DATE to newWeekStartDate.toString(),
                        ),
                )
            }
        }

        fun addWorkout(
            type: String,
            description: String,
        ) {
            val (currentState, nextOrder) = getNextOrder()

            viewModelScope.launch {
                val workoutId =
                    repository.addWorkout(
                        weekStartDate = currentState.weekStartDate,
                        dayOfWeek = null,
                        type = type,
                        description = description,
                        order = nextOrder,
                    )

                userActionLogger.log(
                    actionType = CREATE_WORKOUT,
                    entityType = WORKOUT,
                    entityId = workoutId,
                    metadata =
                        mapOf(
                            WEEK_START_DATE to currentState.weekStartDate.toString(),
                            DAY_OF_WEEK to UNPLANNED,
                            NEW_ORDER to nextOrder.toString(),
                            NEW_TYPE to type,
                            NEW_DESCRIPTION to description,
                        ),
                )
            }
        }

        fun addRestDay() {
            val (currentState, nextOrder) = getNextOrder()

            viewModelScope.launch {
                val restDayId =
                    repository.addRestDay(
                        weekStartDate = currentState.weekStartDate,
                        dayOfWeek = null,
                        order = nextOrder,
                    )

                userActionLogger.log(
                    actionType = CREATE_REST_DAY,
                    entityType = REST_DAY,
                    entityId = restDayId,
                    metadata =
                        mapOf(
                            WEEK_START_DATE to currentState.weekStartDate.toString(),
                            DAY_OF_WEEK to UNPLANNED,
                            NEW_ORDER to nextOrder.toString(),
                        ),
                )
            }
        }

        fun moveWorkout(
            workoutId: Long,
            newDayOfWeek: DayOfWeek?,
            newOrder: Int,
        ) {
            val currentWorkouts = state.value.workouts
            val changes =
                resolveWorkoutChanges(
                    currentWorkouts = currentWorkouts,
                    workoutId = workoutId,
                    newDayOfWeek = newDayOfWeek,
                    newOrder = newOrder,
                )

            viewModelScope.launch {
                persistWorkoutChanges(changes, currentWorkouts)
            }
        }

        fun updateWorkoutCompletion(
            workout: WorkoutUi,
            isCompleted: Boolean,
        ) = viewModelScope.launch {
            val original = state.value.workouts.firstOrNull { it.id == workout.id }

            repository.updateWorkoutCompletion(workout.id, isCompleted)

            val actionType =
                if (isCompleted) COMPLETE_WORKOUT else INCOMPLETE_WORKOUT
            val entityType =
                if (original?.isRestDay == true) REST_DAY else WORKOUT

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = workout.id,
                metadata =
                    mapOf(
                        WEEK_START_DATE to state.value.weekStartDate.toString(),
                        WAS_COMPLETED to (original?.isCompleted?.toString() ?: "false"),
                        IS_COMPLETED to isCompleted.toString(),
                        NEW_TYPE to workout.type,
                        NEW_DESCRIPTION to workout.description,
                    ),
            )
        }

        fun updateWorkoutDetails(
            workoutId: Long,
            type: String,
            description: String,
            isRestDay: Boolean,
        ) = viewModelScope.launch {
            val original = state.value.workouts.firstOrNull { it.id == workoutId }

            repository.updateWorkoutDetails(workoutId, type, description, isRestDay)

            val entityType =
                if (isRestDay) REST_DAY else WORKOUT
            val actionType =
                when {
                    original == null -> UPDATE_WORKOUT
                    original.isRestDay != isRestDay ->
                        if (isRestDay) {
                            CONVERT_WORKOUT_TO_REST_DAY
                        } else {
                            CONVERT_REST_DAY_TO_WORKOUT
                        }
                    isRestDay -> UPDATE_REST_DAY
                    else -> UPDATE_WORKOUT
                }

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = workoutId,
                metadata =
                    mapOf(
                        WEEK_START_DATE to state.value.weekStartDate.toString(),
                        OLD_TYPE to (original?.type ?: ""),
                        NEW_TYPE to type,
                        OLD_DESCRIPTION to (original?.description ?: ""),
                        NEW_DESCRIPTION to description,
                    ),
            )
        }

        fun deleteWorkout(workoutId: Long) =
            viewModelScope.launch {
                val original = state.value.workouts.firstOrNull { it.id == workoutId }
                repository.deleteWorkout(workoutId)
                val entityType =
                    if (original?.isRestDay == true) REST_DAY else WORKOUT
                val actionType =
                    if (original?.isRestDay == true) DELETE_REST_DAY else DELETE_WORKOUT

                userActionLogger.log(
                    actionType = actionType,
                    entityType = entityType,
                    entityId = workoutId,
                    metadata =
                        mapOf(
                            WEEK_START_DATE to state.value.weekStartDate.toString(),
                            OLD_TYPE to (original?.type ?: ""),
                            OLD_DESCRIPTION to (original?.description ?: ""),
                        ),
                )
            }

        private fun getNextOrder(): Pair<WeeklyTrainingState, Int> {
            val currentState = state.value
            val nextOrder = currentState.workouts.count { it.dayOfWeek == null }

            return Pair(currentState, nextOrder)
        }

        private fun resolveWorkoutChanges(
            currentWorkouts: List<WorkoutUi>,
            workoutId: Long,
            newDayOfWeek: DayOfWeek?,
            newOrder: Int,
        ): List<WorkoutUi> {
            val updated =
                updateWorkoutOrderWithRestDayRules(
                    currentWorkouts,
                    workoutId,
                    newDayOfWeek,
                    newOrder,
                )

            return updated.mapNotNull { workout ->
                val original =
                    currentWorkouts.firstOrNull { it.id == workout.id } ?: return@mapNotNull null

                if (original.dayOfWeek != workout.dayOfWeek || original.order != workout.order) {
                    workout
                } else {
                    null
                }
            }
        }

        private suspend fun persistWorkoutChanges(
            changes: List<WorkoutUi>,
            currentWorkouts: List<WorkoutUi>,
        ) {
            changes.forEach { workout ->
                val original =
                    currentWorkouts.firstOrNull { it.id == workout.id } ?: return@forEach
                repository.updateWorkoutDayAndOrder(
                    workoutId = workout.id,
                    dayOfWeek = workout.dayOfWeek,
                    order = workout.order,
                )
                logWorkoutChange(original, workout)
            }
        }

        private suspend fun logWorkoutChange(
            original: WorkoutUi,
            workout: WorkoutUi,
        ) {
            val entityType =
                if (workout.isRestDay) REST_DAY else WORKOUT
            val actionType =
                if (original.dayOfWeek != workout.dayOfWeek) {
                    MOVE_WORKOUT_BETWEEN_DAYS
                } else {
                    REORDER_WORKOUT
                }

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = workout.id,
                metadata =
                    mapOf(
                        WEEK_START_DATE to state.value.weekStartDate.toString(),
                        OLD_DAY_OF_WEEK to (original.dayOfWeek?.value?.toString() ?: UNPLANNED),
                        NEW_DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                        OLD_ORDER to original.order.toString(),
                        NEW_ORDER to workout.order.toString(),
                        NEW_TYPE to workout.type,
                        NEW_DESCRIPTION to workout.description,
                    ),
            )
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

    val adjusted =
        remaining.map { workout ->
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
        newOrder.coerceIn(WeeklyTrainingViewModel.MIN_WORKOUT_ORDER, destinationList.size)
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
