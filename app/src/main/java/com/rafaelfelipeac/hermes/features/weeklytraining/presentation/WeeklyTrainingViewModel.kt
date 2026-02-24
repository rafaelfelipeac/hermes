@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
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
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
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
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT as WORKOUT_EVENT

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeeklyTrainingViewModel
    @Inject
    constructor(
        private val repository: WeeklyTrainingRepository,
        private val userActionLogger: UserActionLogger,
        private val categoryRepository: CategoryRepository,
        private val categorySeeder: CategorySeeder,
        private val settingsRepository: SettingsRepository,
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

        val baseStateFlow =
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
                    slotModePolicy = settingsRepository.initialSlotModePolicy(),
                )
            }

        val state: StateFlow<WeeklyTrainingState> =
            combine(baseStateFlow, settingsRepository.slotModePolicy) { base, slotModePolicy ->
                base.copy(slotModePolicy = slotModePolicy)
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
                        slotModePolicy = settingsRepository.initialSlotModePolicy(),
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
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = WORKOUT_EVENT,
                    categoryId = categoryId,
                    categories = currentState.categories,
                )
            val categoryName =
                currentState.categories.firstOrNull { it.id == normalizedCategoryId }?.name

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
                        mutableMapOf(
                            WEEK_START_DATE to currentState.weekStartDate.toString(),
                            DAY_OF_WEEK to UNPLANNED,
                            NEW_ORDER to nextOrder.toString(),
                            NEW_TYPE to type,
                            NEW_DESCRIPTION to description,
                        ).apply {
                            if (!categoryName.isNullOrBlank()) {
                                put(CATEGORY_NAME, categoryName)
                            }
                        },
                )
            }
        }

        fun addRest() {
            addNonWorkoutEvent(REST)
        }

        fun addBusy() {
            addNonWorkoutEvent(BUSY)
        }

        fun addSick() {
            addNonWorkoutEvent(SICK)
        }

        private fun addNonWorkoutEvent(eventType: EventType) {
            val currentState = state.value
            val nextOrder = nextUnplannedOrder(currentState)

            viewModelScope.launch {
                val eventId =
                    repository.addEvent(
                        weekStartDate = currentState.weekStartDate,
                        dayOfWeek = null,
                        eventType = eventType,
                        order = nextOrder,
                    )

                userActionLogger.log(
                    actionType = CREATE_REST_DAY,
                    entityType = eventType.toUserActionEntityType(),
                    entityId = eventId,
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
            newTimeSlot: TimeSlot?,
            newOrder: Int,
        ) {
            val currentWorkouts = state.value.workouts
            val changes =
                resolveWorkoutChanges(
                    currentWorkouts = currentWorkouts,
                    workoutId = workoutId,
                    newDayOfWeek = newDayOfWeek,
                    newTimeSlot = newTimeSlot,
                    newOrder = newOrder,
                )
            val originalWorkout = currentWorkouts.firstOrNull { it.id == workoutId }
            val undoPositions =
                changes.mapNotNull { workout ->
                    currentWorkouts.firstOrNull { it.id == workout.id }?.let { original ->
                        WorkoutPosition(
                            id = original.id,
                            dayOfWeek = original.dayOfWeek,
                            timeSlot = original.timeSlot,
                            order = original.order,
                        )
                    }
                }
            val movedEventType = originalWorkout?.eventType ?: WORKOUT_EVENT

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
                                movedEventType = movedEventType,
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

            if (workout.eventType != WORKOUT_EVENT || original?.eventType != WORKOUT_EVENT) {
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
            eventType: EventType,
            categoryId: Long?,
        ) = viewModelScope.launch {
            val original = state.value.workouts.firstOrNull { it.id == workoutId }
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = eventType,
                    categoryId = categoryId,
                    categories = state.value.categories,
                )
            val (oldCategoryName, newCategoryName) =
                resolveCategoryNames(
                    eventType = eventType,
                    normalizedCategoryId = normalizedCategoryId,
                    categories = state.value.categories,
                    original = original,
                )

            repository.updateWorkoutDetails(
                workoutId = workoutId,
                type = type,
                description = description,
                eventType = eventType,
                categoryId = normalizedCategoryId,
            )

            val entityType =
                when {
                    eventType != WORKOUT_EVENT -> eventType.toUserActionEntityType()
                    else -> original?.eventType?.toUserActionEntityType() ?: WORKOUT
                }
            val actionType =
                when {
                    original == null -> UPDATE_WORKOUT
                    original.eventType != eventType ->
                        if (eventType != WORKOUT_EVENT) {
                            CONVERT_WORKOUT_TO_REST_DAY
                        } else {
                            CONVERT_REST_DAY_TO_WORKOUT
                        }
                    eventType != WORKOUT_EVENT -> UPDATE_REST_DAY
                    else -> UPDATE_WORKOUT
                }

            userActionLogger.log(
                actionType = actionType,
                entityType = entityType,
                entityId = workoutId,
                metadata =
                    buildWorkoutUpdateMetadata(
                        WorkoutUpdateMetadataInput(
                            weekStartDate = state.value.weekStartDate.toString(),
                            original = original,
                            type = type,
                            description = description,
                            isRestDay = eventType != WORKOUT_EVENT,
                            oldCategoryName = oldCategoryName,
                            newCategoryName = newCategoryName,
                        ),
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
                            .asSequence()
                            .filter { workout -> workout.dayOfWeek == it.dayOfWeek }
                            .filter { workout -> workout.timeSlot == it.timeSlot }
                            .sortedBy { workout -> workout.order }
                            .filter { workout -> workout.id != workoutId }
                            .map { workout ->
                                WorkoutPosition(
                                    id = workout.id,
                                    dayOfWeek = workout.dayOfWeek,
                                    timeSlot = workout.timeSlot,
                                    order = workout.order,
                                )
                            }
                            .toList()
                    }.orEmpty()

                repository.deleteWorkout(workoutId)

                if (original != null) {
                    normalizeOrdersAfterDelete(
                        deletedWorkoutId = workoutId,
                        dayOfWeek = original.dayOfWeek,
                        timeSlot = original.timeSlot,
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
                    original?.eventType?.toUserActionEntityType() ?: WORKOUT
                val actionType =
                    if (original?.eventType == WORKOUT_EVENT) DELETE_WORKOUT else DELETE_REST_DAY

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
                    is PendingUndoAction.MoveOrReorder ->
                        undoMoveOrReorder(
                            action = action,
                            currentWorkouts = state.value.workouts,
                            repository = repository,
                            userActionLogger = userActionLogger,
                        )
                    is PendingUndoAction.Delete ->
                        undoDelete(
                            action = action,
                            repository = repository,
                            userActionLogger = userActionLogger,
                        )
                    is PendingUndoAction.Completion ->
                        undoCompletion(
                            action = action,
                            repository = repository,
                            userActionLogger = userActionLogger,
                        )
                    is PendingUndoAction.ReplaceWeek ->
                        undoReplaceWeek(
                            action = action,
                            repository = repository,
                            userActionLogger = userActionLogger,
                        )
                }

                undoState.value = null
            }
        }

        fun clearUndo() {
            clearUndoTimeout()

            undoState.value = null
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

        companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
            private const val UNDO_TIMEOUT_MS = 4_000L
        }
    }

private fun mapWorkoutsToUi(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
): List<WorkoutUi> {
    val categoriesById = categories.associateBy { it.id }
    val fallbackCategory = categoriesById[UNCATEGORIZED_ID]

    return workouts.map { workout ->
        val category =
            if (workout.eventType != WORKOUT_EVENT) {
                null
            } else {
                workout.categoryId?.let(categoriesById::get) ?: fallbackCategory
            }
        workout.toUi(category)
    }
}

private suspend fun undoCompletion(
    action: PendingUndoAction.Completion,
    repository: WeeklyTrainingRepository,
    userActionLogger: UserActionLogger,
) {
    repository.updateWorkoutCompletion(
        workoutId = action.workout.id,
        isCompleted = action.previousCompleted,
    )

    val entityType =
        action.workout.eventType.toUserActionEntityType()
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

private fun normalizeCategoryId(
    eventType: EventType,
    categoryId: Long?,
): Long? {
    return if (eventType != WORKOUT_EVENT) {
        null
    } else {
        categoryId ?: UNCATEGORIZED_ID
    }
}

private fun resolveCategoryId(
    eventType: EventType,
    categoryId: Long?,
    categories: List<CategoryUi>,
): Long? {
    val normalized = normalizeCategoryId(eventType, categoryId) ?: return null
    return if (categories.any { it.id == normalized }) normalized else UNCATEGORIZED_ID
}

private fun resolveCategoryNames(
    eventType: EventType,
    normalizedCategoryId: Long?,
    categories: List<CategoryUi>,
    original: WorkoutUi?,
): Pair<String?, String?> {
    val oldCategoryName = original?.takeIf { it.eventType == WORKOUT_EVENT }?.categoryName
    val newCategoryName =
        if (eventType != WORKOUT_EVENT || normalizedCategoryId == null) {
            null
        } else {
            categories.firstOrNull { it.id == normalizedCategoryId }?.name
        }

    return oldCategoryName to newCategoryName
}

private fun buildWorkoutUpdateMetadata(input: WorkoutUpdateMetadataInput): Map<String, String> {
    return mutableMapOf(
        WEEK_START_DATE to input.weekStartDate,
        OLD_TYPE to (input.original?.type ?: EMPTY),
        NEW_TYPE to input.type,
        OLD_DESCRIPTION to (input.original?.description ?: EMPTY),
        NEW_DESCRIPTION to input.description,
    ).apply {
        if (!input.isRestDay) {
            if (!input.oldCategoryName.isNullOrBlank()) {
                put(OLD_CATEGORY_NAME, input.oldCategoryName)
            }
            if (!input.newCategoryName.isNullOrBlank()) {
                put(NEW_CATEGORY_NAME, input.newCategoryName)
                put(CATEGORY_NAME, input.newCategoryName)
            }
        }
    }
}

private fun nextUnplannedOrder(state: WeeklyTrainingState): Int {
    return state.workouts.count { it.dayOfWeek == null }
}
