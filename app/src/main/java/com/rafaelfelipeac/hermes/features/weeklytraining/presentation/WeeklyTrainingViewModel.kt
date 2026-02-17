@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
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
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.OPEN_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_DELETE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
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
        private val categoryRepository: CategoryRepository,
        private val categorySeeder: CategorySeeder,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val weekStartDate =
            selectedDate
                .map { it.with(TemporalAdjusters.previousOrSame(MONDAY)) }
                .distinctUntilChanged()

        private val categoriesFlow =
            categoryRepository.observeCategories().map { categories ->
                categories.map { it.toUi() }
            }

        private val workoutsForWeek =
            weekStartDate.flatMapLatest { weekStart ->
                repository.observeWorkoutsForWeek(weekStart)
            }
                .combine(categoriesFlow) { workouts, categories ->
                    mapWorkoutsToUi(workouts, categories)
                }

        private val workoutsLoadedForWeek =
            weekStartDate.flatMapLatest {
                workoutsForWeek
                    .map { true }
                    .onStart { emit(false) }
            }

        private val undoState = MutableStateFlow<UndoState?>(null)
        private val messageEvents = MutableSharedFlow<WeeklyTrainingMessage>(extraBufferCapacity = 1)
        private var undoTimeoutJob: Job? = null
        private var undoCounter = 0L

        init {
            viewModelScope.launch {
                categorySeeder.ensureSeeded()
                repository.assignNullCategoryTo(UNCATEGORIZED_ID)
            }
        }

        val state: StateFlow<WeeklyTrainingState> =
            combine(
                selectedDate,
                weekStartDate,
                workoutsForWeek,
                workoutsLoadedForWeek,
                categoriesFlow,
            ) { selected, weekStart, workouts, isWeekLoaded, categories ->
                WeeklyTrainingState(
                    selectedDate = selected,
                    weekStartDate = weekStart,
                    workouts = workouts,
                    isWeekLoaded = isWeekLoaded,
                    categories = categories,
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
                        isWeekLoaded = false,
                        categories = emptyList(),
                    ),
            )

        val undoUiState: StateFlow<UndoState?> =
            undoState.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = null,
            )
        val messages: SharedFlow<WeeklyTrainingMessage> = messageEvents.asSharedFlow()

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
                            WEEK_START_DATE to newWeekStartDate.toString(),
                            OLD_WEEK_START_DATE to previousWeekStartDate.toString(),
                            NEW_WEEK_START_DATE to newWeekStartDate.toString(),
                        ),
                )
            }
        }

        fun addWorkout(
            type: String,
            description: String,
            categoryId: Long?,
        ) {
            val currentState = state.value
            val nextOrder = nextUnplannedOrder(currentState)
            val normalizedCategoryId = categoryId ?: UNCATEGORIZED_ID

            viewModelScope.launch {
                val workoutId =
                    repository.addWorkout(
                        AddWorkoutRequest(
                            weekStartDate = currentState.weekStartDate,
                            dayOfWeek = null,
                            type = type,
                            description = description,
                            categoryId = normalizedCategoryId,
                            order = nextOrder,
                        ),
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
            val currentState = state.value
            val nextOrder = nextUnplannedOrder(currentState)

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

        fun copyLastWeek() {
            val currentWeekStartDate = state.value.weekStartDate
            val previousWeekStartDate = currentWeekStartDate.minusWeeks(1)

            viewModelScope.launch {
                val sourceWorkouts = repository.getWorkoutsForWeek(previousWeekStartDate)

                if (sourceWorkouts.isEmpty()) {
                    messageEvents.emit(WeeklyTrainingMessage.NothingToCopyFromLastWeek)
                    return@launch
                }

                val targetWorkouts = repository.getWorkoutsForWeek(currentWeekStartDate)

                repository.replaceWorkoutsForWeek(
                    weekStartDate = currentWeekStartDate,
                    sourceWorkouts = sourceWorkouts,
                )

                userActionLogger.log(
                    actionType = COPY_LAST_WEEK,
                    entityType = WEEK,
                    metadata =
                        mapOf(
                            WEEK_START_DATE to currentWeekStartDate.toString(),
                            OLD_WEEK_START_DATE to previousWeekStartDate.toString(),
                            NEW_WEEK_START_DATE to currentWeekStartDate.toString(),
                        ),
                )

                setUndoAction(
                    action =
                        PendingUndoAction.ReplaceWeek(
                            weekStartDate = currentWeekStartDate,
                            previousWorkouts = targetWorkouts,
                        ),
                    message = UndoMessage.WeekCopied,
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
            val originalWorkout = currentWorkouts.firstOrNull { it.id == workoutId }
            val undoPositions =
                changes.mapNotNull { workout ->
                    currentWorkouts.firstOrNull { it.id == workout.id }?.let { original ->
                        WorkoutPosition(
                            id = original.id,
                            dayOfWeek = original.dayOfWeek,
                            order = original.order,
                        )
                    }
                }
            val isRestDay = originalWorkout?.isRestDay == true

            viewModelScope.launch {
                persistWorkoutChanges(
                    dependencies =
                        WorkoutChangeDependencies(
                            repository = repository,
                            userActionLogger = userActionLogger,
                            weekStartDate = state.value.weekStartDate,
                        ),
                    changes = changes,
                    currentWorkouts = currentWorkouts,
                    movedWorkoutId = workoutId,
                )

                if (undoPositions.isNotEmpty()) {
                    setUndoAction(
                        action =
                            PendingUndoAction.MoveOrReorder(
                                movedWorkoutId = workoutId,
                                isRestDay = isRestDay,
                                previousPositions = undoPositions,
                                weekStartDate = state.value.weekStartDate,
                            ),
                        message = UndoMessage.Moved,
                    )
                }
            }
        }

        fun updateWorkoutCompletion(
            workout: WorkoutUi,
            isCompleted: Boolean,
        ) = viewModelScope.launch {
            val original = state.value.workouts.firstOrNull { it.id == workout.id }

            if (workout.isRestDay || original?.isRestDay == true) {
                return@launch
            }

            repository.updateWorkoutCompletion(workout.id, isCompleted)

            val actionType =
                if (isCompleted) COMPLETE_WORKOUT else INCOMPLETE_WORKOUT

            userActionLogger.log(
                actionType = actionType,
                entityType = WORKOUT,
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

            if (original != null && original.isCompleted != isCompleted) {
                val message =
                    if (isCompleted) {
                        UndoMessage.Completed
                    } else {
                        UndoMessage.MarkedIncomplete
                    }
                setUndoAction(
                    action =
                        PendingUndoAction.Completion(
                            workout = original,
                            previousCompleted = original.isCompleted,
                            newCompleted = isCompleted,
                            weekStartDate = state.value.weekStartDate,
                        ),
                    message = message,
                )
            }
        }

        fun updateWorkoutDetails(
            workoutId: Long,
            type: String,
            description: String,
            isRestDay: Boolean,
            categoryId: Long?,
        ) = viewModelScope.launch {
            val original = state.value.workouts.firstOrNull { it.id == workoutId }
            val normalizedCategoryId =
                if (isRestDay) {
                    null
                } else {
                    categoryId ?: UNCATEGORIZED_ID
                }

            repository.updateWorkoutDetails(
                workoutId = workoutId,
                type = type,
                description = description,
                isRestDay = isRestDay,
                categoryId = normalizedCategoryId,
            )

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
                        OLD_TYPE to (original?.type ?: EMPTY),
                        NEW_TYPE to type,
                        OLD_DESCRIPTION to (original?.description ?: EMPTY),
                        NEW_DESCRIPTION to description,
                    ),
            )
        }

        fun deleteWorkout(workoutId: Long) =
            viewModelScope.launch {
                val currentWorkouts = state.value.workouts
                val original = currentWorkouts.firstOrNull { it.id == workoutId }
                val bucketPositions =
                    original?.let {
                        currentWorkouts
                            .filter { workout -> workout.dayOfWeek == it.dayOfWeek }
                            .sortedBy { workout -> workout.order }
                            .filter { workout -> workout.id != workoutId }
                            .map { workout ->
                                WorkoutPosition(
                                    id = workout.id,
                                    dayOfWeek = workout.dayOfWeek,
                                    order = workout.order,
                                )
                            }
                    }.orEmpty()

                repository.deleteWorkout(workoutId)

                if (original != null) {
                    normalizeOrdersAfterDelete(
                        deletedWorkoutId = workoutId,
                        dayOfWeek = original.dayOfWeek,
                        currentWorkouts = currentWorkouts,
                        repository = repository,
                    )

                    setUndoAction(
                        action =
                            PendingUndoAction.Delete(
                                workout = original,
                                weekStartDate = state.value.weekStartDate,
                                previousPositions = bucketPositions,
                            ),
                        message = UndoMessage.Deleted,
                    )
                }

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
                            OLD_TYPE to (original?.type ?: EMPTY),
                            OLD_DESCRIPTION to (original?.description ?: EMPTY),
                        ),
                )
            }

        fun undoLastAction() {
            val currentUndo = undoState.value ?: return

            clearUndoTimeout()

            viewModelScope.launch {
                when (val action = currentUndo.action) {
                    is PendingUndoAction.MoveOrReorder -> undoMoveOrReorder(action)
                    is PendingUndoAction.Delete -> undoDelete(action)
                    is PendingUndoAction.Completion -> undoCompletion(action)
                    is PendingUndoAction.ReplaceWeek -> undoReplaceWeek(action)
                }

                undoState.value = null
            }
        }

        fun clearUndo() {
            clearUndoTimeout()

            undoState.value = null
        }

        private suspend fun undoMoveOrReorder(action: PendingUndoAction.MoveOrReorder) {
            val currentWorkouts = state.value.workouts
            val movedWorkout = currentWorkouts.firstOrNull { it.id == action.movedWorkoutId }
            val previousPosition =
                action.previousPositions.firstOrNull { it.id == action.movedWorkoutId }
            val previousPositionsById = action.previousPositions.associateBy { it.id }

            action.previousPositions.forEach { position ->
                repository.updateWorkoutDayAndOrder(
                    workoutId = position.id,
                    dayOfWeek = position.dayOfWeek,
                    order = position.order,
                )
            }

            val updatedWorkouts =
                currentWorkouts.map { workout ->
                    val position = previousPositionsById[workout.id]

                    if (position == null) {
                        workout
                    } else {
                        workout.copy(
                            dayOfWeek = position.dayOfWeek,
                            order = position.order,
                        )
                    }
                }

            action.previousPositions
                .map { it.dayOfWeek }
                .distinct()
                .forEach { dayOfWeek ->
                    normalizeOrdersForDay(
                        dayOfWeek = dayOfWeek,
                        currentWorkouts = updatedWorkouts,
                        repository = repository,
                        forceUpdate = true,
                        skipIds = setOf(action.movedWorkoutId),
                    )
                }

            if (movedWorkout != null && previousPosition != null) {
                val updated =
                    movedWorkout.copy(
                        dayOfWeek = previousPosition.dayOfWeek,
                        order = previousPosition.order,
                    )

                logUndoWorkoutChange(
                    original = movedWorkout,
                    workout = updated,
                    weekStartDate = action.weekStartDate,
                    userActionLogger = userActionLogger,
                )
            }
        }

        private suspend fun undoDelete(action: PendingUndoAction.Delete) {
            val restoredId = restoreDeletedWorkout(repository, action)
            val workout = action.workout
            val entityType =
                if (workout.isRestDay) REST_DAY else WORKOUT
            val actionType =
                if (workout.isRestDay) UNDO_DELETE_REST_DAY else UNDO_DELETE_WORKOUT

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = restoredId,
                metadata =
                    mapOf(
                        WEEK_START_DATE to action.weekStartDate.toString(),
                        DAY_OF_WEEK to (workout.dayOfWeek?.value?.toString() ?: UNPLANNED),
                        NEW_ORDER to workout.order.toString(),
                        NEW_TYPE to workout.type,
                        NEW_DESCRIPTION to workout.description,
                    ),
            )
        }

        private suspend fun undoReplaceWeek(action: PendingUndoAction.ReplaceWeek) {
            repository.deleteWorkoutsForWeek(action.weekStartDate)

            action.previousWorkouts
                .sortedBy { it.id }
                .forEach { workout ->
                    repository.insertWorkout(workout)
                }

            userActionLogger.log(
                actionType = UNDO_COPY_LAST_WEEK,
                entityType = WEEK,
                metadata =
                    mapOf(
                        WEEK_START_DATE to action.weekStartDate.toString(),
                        OLD_WEEK_START_DATE to action.weekStartDate.minusWeeks(1).toString(),
                        NEW_WEEK_START_DATE to action.weekStartDate.toString(),
                    ),
            )
        }

        private suspend fun undoCompletion(action: PendingUndoAction.Completion) {
            repository.updateWorkoutCompletion(
                workoutId = action.workout.id,
                isCompleted = action.previousCompleted,
            )

            val entityType =
                if (action.workout.isRestDay) REST_DAY else WORKOUT
            val actionType =
                if (action.newCompleted) UNDO_COMPLETE_WORKOUT else UNDO_INCOMPLETE_WORKOUT

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = action.workout.id,
                metadata =
                    mapOf(
                        WEEK_START_DATE to action.weekStartDate.toString(),
                        WAS_COMPLETED to action.newCompleted.toString(),
                        IS_COMPLETED to action.previousCompleted.toString(),
                        NEW_TYPE to action.workout.type,
                        NEW_DESCRIPTION to action.workout.description,
                    ),
            )
        }

        private fun setUndoAction(
            action: PendingUndoAction,
            message: UndoMessage,
        ) {
            val newId = ++undoCounter

            undoState.value = UndoState(id = newId, message = message, action = action)

            scheduleUndoTimeout(newId)
        }

        private fun scheduleUndoTimeout(undoId: Long) {
            undoTimeoutJob?.cancel()
            undoTimeoutJob =
                viewModelScope.launch {
                    delay(UNDO_TIMEOUT_MS)

                    if (undoState.value?.id == undoId) {
                        undoState.value = null
                    }
                }
        }

        private fun clearUndoTimeout() {
            undoTimeoutJob?.cancel()
            undoTimeoutJob = null
        }

        private fun mapWorkoutsToUi(
            workouts: List<Workout>,
            categories: List<CategoryUi>,
        ): List<WorkoutUi> {
            val categoriesById = categories.associateBy { it.id }
            val fallbackCategory = categoriesById[UNCATEGORIZED_ID]

            return workouts.map { workout ->
                val category =
                    if (workout.isRestDay) {
                        null
                    } else {
                        workout.categoryId?.let(categoriesById::get) ?: fallbackCategory
                    }
                workout.toUi(category)
            }
        }

        companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
            private const val UNDO_TIMEOUT_MS = 4_000L
        }
    }

private fun nextUnplannedOrder(state: WeeklyTrainingState): Int {
    return state.workouts.count { it.dayOfWeek == null }
}
