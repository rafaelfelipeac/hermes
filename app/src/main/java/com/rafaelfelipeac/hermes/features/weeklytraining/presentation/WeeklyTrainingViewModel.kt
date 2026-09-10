@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.flow.stateInWhileSubscribed
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.OPEN_WEEK
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.CopyLastWeekCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.CreateWeeklyItemCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoCopyLastWeekCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoScheduleCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDetailsCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleChange
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutScheduleCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.storageWeekStartsForDisplayWeek
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.weekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
@Suppress("LargeClass", "TooManyFunctions")
class WeeklyTrainingViewModel
    @Inject
    constructor(
        private val repository: WeeklyTrainingRepository,
        private val userActionLogger: UserActionLogger,
        private val categoryRepository: CategoryRepository,
        private val categorySeeder: CategorySeeder,
        private val settingsRepository: SettingsRepository,
        private val weeklyTrainingCommandRepository: WeeklyTrainingCommandRepository,
    ) : ViewModel() {
        private val selectedDate = MutableStateFlow(LocalDate.now())
        private val weekStartDay = settingsRepository.weekStartDay
        private val weekStartDate =
            combine(selectedDate, weekStartDay) { selected, configuredStartDay ->
                weekStart(selected, configuredStartDay.dayOfWeek)
            }
                .distinctUntilChanged()
        private val storageWeekStarts =
            weekStartDate
                .map(::storageWeekStartsForDisplayWeek)
                .distinctUntilChanged()

        private val categoriesFlow =
            categoryRepository.observeCategories().map { categories ->
                categories.map { it.toUi() }
            }

        private val workoutsForDisplayWeek =
            combine(selectedDate, weekStartDate, storageWeekStarts) { selected, displayWeekStart, weekStarts ->
                WeekQuery(
                    displayWeekStart = displayWeekStart,
                    weekStarts = weekStarts,
                    unassignedStorageWeekStart = canonicalStorageWeekStart(selected),
                )
            }
                .flatMapLatest { query ->
                    repository.observeWorkoutsForWeekStarts(query.weekStarts).map { workouts ->
                        workoutsForDisplayWeek(
                            workouts = workouts,
                            displayWeekStart = query.displayWeekStart,
                            unassignedStorageWeekStart = query.unassignedStorageWeekStart,
                        )
                    }
                }
        private val workoutsForWeek =
            workoutsForDisplayWeek
                .combine(categoriesFlow) { workouts, categories ->
                    mapWorkoutsToUi(workouts, categories)
                }

        private val workoutsLoadedForWeek =
            combine(weekStartDate, weekStartDay) { weekStart, startDay ->
                weekStart to startDay
            }.flatMapLatest {
                workoutsForWeek
                    .map { true }
                    .onStart { emit(false) }
            }

        private val undoState = MutableStateFlow<UndoState?>(null)
        private val messageEvents = MutableSharedFlow<WeeklyTrainingMessage>(extraBufferCapacity = 1)
        private var undoTimeoutJob: Job? = null
        private var undoCounter = 0L
        private val completionUpdateMutex = Mutex()
        private val pendingCompletionById = mutableMapOf<Long, Boolean>()

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
                weekStartDay,
                workoutsForWeek,
                workoutsLoadedForWeek,
            ) { selected, weekStart, configuredWeekStartDay, workouts, isWeekLoaded ->
                WeeklyTrainingState(
                    selectedDate = selected,
                    weekStartDate = weekStart,
                    workouts = workouts,
                    isWeekLoaded = isWeekLoaded,
                    categories = emptyList(),
                    weekStartDay = configuredWeekStartDay,
                    slotModePolicy = settingsRepository.initialSlotModePolicy(),
                )
            }.combine(categoriesFlow) { base, categories ->
                WeeklyTrainingState(
                    selectedDate = base.selectedDate,
                    weekStartDate = base.weekStartDate,
                    workouts = base.workouts,
                    isWeekLoaded = base.isWeekLoaded,
                    categories = categories,
                    weekStartDay = base.weekStartDay,
                    slotModePolicy = base.slotModePolicy,
                )
            }

        val state: StateFlow<WeeklyTrainingState> =
            combine(
                baseStateFlow,
                settingsRepository.slotModePolicy,
                settingsRepository.weekStartDay,
            ) { base, slotModePolicy, configuredWeekStartDay ->
                base.copy(slotModePolicy = slotModePolicy, weekStartDay = configuredWeekStartDay)
            }.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue =
                    settingsRepository.initialWeekStartDay().let { initialWeekStartDay ->
                        WeeklyTrainingState(
                            selectedDate = selectedDate.value,
                            weekStartDate = weekStart(selectedDate.value, initialWeekStartDay.dayOfWeek),
                            workouts = emptyList(),
                            isWeekLoaded = false,
                            categories = emptyList(),
                            weekStartDay = initialWeekStartDay,
                            slotModePolicy = settingsRepository.initialSlotModePolicy(),
                        )
                    },
            )

        val undoUiState: StateFlow<UndoState?> =
            undoState.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = null,
            )
        val messages: SharedFlow<WeeklyTrainingMessage> = messageEvents.asSharedFlow()

        fun onDateSelected(date: LocalDate) {
            selectedDate.value = date
        }

        fun onWeekChanged(newSelectedDate: LocalDate) {
            val previousWeekStartDate = state.value.weekStartDate
            val newWeekStartDate = weekStart(newSelectedDate, state.value.weekStartDay.dayOfWeek)

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
            workoutDate: LocalDate? = null,
        ) {
            val currentState = state.value
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = EventType.WORKOUT,
                    categoryId = categoryId,
                    categories = currentState.categories,
                )

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(workoutDate ?: currentState.selectedDate)
                val dayOfWeek = workoutDate?.dayOfWeek

                weeklyTrainingCommandRepository.createItem(
                    CreateWeeklyItemCommand(
                        eventType = EventType.WORKOUT,
                        storageWeekStart = storageWeekStart,
                        displayWeekStart = storageWeekStart,
                        dayOfWeek = dayOfWeek,
                        timeSlot = null,
                        type = type,
                        description = description,
                        categoryId = normalizedCategoryId,
                    ),
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

        fun addRaceEvent(
            type: String,
            description: String,
            categoryId: Long?,
            eventDate: LocalDate,
        ) {
            val currentState = state.value
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = EventType.RACE_EVENT,
                    categoryId = categoryId,
                    categories = currentState.categories,
                )

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(eventDate)
                val dayOfWeek = eventDate.dayOfWeek

                weeklyTrainingCommandRepository.createItem(
                    CreateWeeklyItemCommand(
                        eventType = EventType.RACE_EVENT,
                        storageWeekStart = storageWeekStart,
                        displayWeekStart = storageWeekStart,
                        dayOfWeek = dayOfWeek,
                        timeSlot = null,
                        type = type,
                        description = description,
                        categoryId = normalizedCategoryId,
                    ),
                )
            }
        }

        private fun addNonWorkoutEvent(eventType: EventType) {
            val currentState = state.value

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(currentState.selectedDate)

                weeklyTrainingCommandRepository.createItem(
                    CreateWeeklyItemCommand(
                        eventType = eventType,
                        storageWeekStart = storageWeekStart,
                        displayWeekStart = currentState.weekStartDate,
                        dayOfWeek = null,
                        timeSlot = null,
                        type = EMPTY,
                        description = EMPTY,
                        categoryId = null,
                    ),
                )
            }
        }

        fun copyLastWeek() {
            val currentDisplayWeekStartDate = state.value.weekStartDate
            val previousDisplayWeekStartDate = currentDisplayWeekStartDate.minusWeeks(1)
            val currentUnassignedStorageWeekStart = canonicalStorageWeekStart(state.value.selectedDate)
            val previousUnassignedStorageWeekStart = currentUnassignedStorageWeekStart.minusWeeks(1)
            val currentStorageWeekStarts = storageWeekStartsForDisplayWeek(currentDisplayWeekStartDate)
            val previousStorageWeekStarts = storageWeekStartsForDisplayWeek(previousDisplayWeekStartDate)

            viewModelScope.launch {
                val sourceWorkouts =
                    workoutsForDisplayWeek(
                        workouts = repository.getWorkoutsForWeekStarts(previousStorageWeekStarts),
                        displayWeekStart = previousDisplayWeekStartDate,
                        unassignedStorageWeekStart = previousUnassignedStorageWeekStart,
                    )

                if (sourceWorkouts.isEmpty()) {
                    messageEvents.emit(WeeklyTrainingMessage.NothingToCopyFromLastWeek)
                    return@launch
                }

                val commandResult =
                    weeklyTrainingCommandRepository.copyLastWeek(
                        CopyLastWeekCommand(
                            targetStorageWeekStarts = currentStorageWeekStarts,
                            targetDisplayWeekStart = currentDisplayWeekStartDate,
                            targetUnassignedStorageWeekStart = currentUnassignedStorageWeekStart,
                            sourceDisplayWeekStart = previousDisplayWeekStartDate,
                            replacementWorkouts = sourceWorkouts.map(::copyWorkoutToNextWeek),
                        ),
                    )
                if (commandResult !is WeeklyTrainingCommandResult.WeekCopied) {
                    return@launch
                }

                val targetWorkouts = commandResult.previousWorkouts

                setUndoAction(
                    action =
                        PendingUndoAction.ReplaceWeek(
                            weekStartDate = currentDisplayWeekStartDate,
                            previousWorkouts = targetWorkouts,
                            unassignedStorageWeekStart = currentUnassignedStorageWeekStart,
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
                            weekStartDate = original.weekStartDate,
                            dayOfWeek = original.dayOfWeek,
                            timeSlot = original.timeSlot,
                            order = original.order,
                        )
                    }
                }
            val movedEventType = originalWorkout?.eventType ?: EventType.WORKOUT

            viewModelScope.launch {
                val commandResult =
                    weeklyTrainingCommandRepository.updateSchedule(
                        buildScheduleCommand(
                            movedWorkoutId = workoutId,
                            changes = changes,
                        ),
                    )
                if (commandResult !is WeeklyTrainingCommandResult.ScheduleChanged) {
                    return@launch
                }

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

        private fun buildScheduleCommand(
            movedWorkoutId: Long,
            changes: List<WorkoutUi>,
        ): WorkoutScheduleCommand {
            val currentState = state.value
            val unassignedStorageWeekStart = canonicalStorageWeekStart(currentState.selectedDate)

            return WorkoutScheduleCommand(
                movedWorkoutId = movedWorkoutId,
                displayWeekStart = currentState.weekStartDate,
                changes =
                    changes.map { workout ->
                        WorkoutScheduleChange(
                            workoutId = workout.id,
                            weekStartDate =
                                resolveStorageWeekStartDate(
                                    workout = workout,
                                    weekStartDate = currentState.weekStartDate,
                                    displayStartDay = currentState.weekStartDay,
                                    unassignedStorageWeekStart = unassignedStorageWeekStart,
                                ),
                            dayOfWeek = workout.dayOfWeek,
                            timeSlot = workout.timeSlot,
                            order = workout.order,
                        )
                    },
            )
        }

        @Suppress("LongMethod")
        fun updateWorkoutCompletion(
            workout: WorkoutUi,
            isCompleted: Boolean,
        ) = viewModelScope.launch {
            completionUpdateMutex.withLock {
                val currentWorkouts = state.value.workouts

                prunePendingCompletionOverrides(currentWorkouts)

                val optimisticWorkoutsBeforeChange = applyPendingCompletionOverrides(currentWorkouts)
                val originalEffective = optimisticWorkoutsBeforeChange.firstOrNull { it.id == workout.id }
                if (!workout.eventType.supportsCompletion() || originalEffective?.eventType != workout.eventType) {
                    return@withLock
                }

                if (originalEffective.isCompleted == isCompleted) {
                    return@withLock
                }

                pendingCompletionById[workout.id] = isCompleted
                val optimisticWorkouts = applyPendingCompletionOverrides(currentWorkouts)

                val commandResult =
                    weeklyTrainingCommandRepository.updateCompletion(
                        WorkoutCompletionCommand(
                            workoutId = workout.id,
                            isCompleted = isCompleted,
                            displayWeekStart = state.value.weekStartDate,
                        ),
                    )
                if (commandResult !is WeeklyTrainingCommandResult.CompletionChanged) {
                    pendingCompletionById.remove(workout.id)
                    return@withLock
                }
                val undoWorkout = originalEffective.copy(isCompleted = commandResult.previousCompleted)
                val persistedEventType = commandResult.eventType

                val message =
                    if (isCompleted) {
                        if (
                            persistedEventType.supportsCompletion() &&
                            shouldCelebrateAllWorkoutsCompleted(
                                currentWorkouts = optimisticWorkouts,
                                workoutId = workout.id,
                                previousIsCompleted = commandResult.previousCompleted,
                                newIsCompleted = true,
                            )
                        ) {
                            UndoMessage.CompletedWeek
                        } else {
                            UndoMessage.Completed
                        }
                    } else {
                        UndoMessage.MarkedIncomplete
                    }

                if (message == UndoMessage.CompletedWeek) {
                    logCompleteWeekWorkouts(
                        userActionLogger = userActionLogger,
                        weekStartDate = state.value.weekStartDate,
                    )
                }

                setUndoAction(
                    action =
                        PendingUndoAction.Completion(
                            workout = undoWorkout,
                            previousCompleted = commandResult.previousCompleted,
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
            workoutDate: LocalDate? = null,
        ) = viewModelScope.launch {
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = eventType,
                    categoryId = categoryId,
                    categories = state.value.categories,
                )

            weeklyTrainingCommandRepository.updateDetails(
                WorkoutDetailsCommand(
                    workoutId = workoutId,
                    type = type,
                    description = description,
                    eventType = eventType,
                    categoryId = normalizedCategoryId,
                    displayWeekStart = state.value.weekStartDate,
                    targetDate = workoutDate,
                ),
            )
        }

        fun updateRaceEvent(
            workoutId: Long,
            type: String,
            description: String,
            categoryId: Long?,
            eventDate: LocalDate,
        ) = viewModelScope.launch {
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = EventType.RACE_EVENT,
                    categoryId = categoryId,
                    categories = state.value.categories,
                )

            weeklyTrainingCommandRepository.updateDetails(
                WorkoutDetailsCommand(
                    workoutId = workoutId,
                    type = type,
                    description = description,
                    eventType = EventType.RACE_EVENT,
                    categoryId = normalizedCategoryId,
                    displayWeekStart = state.value.weekStartDate,
                    targetDate = eventDate,
                ),
            )
        }

        fun deleteWorkout(workoutId: Long) =
            viewModelScope.launch {
                val currentWorkouts = state.value.workouts
                val original = currentWorkouts.firstOrNull { it.id == workoutId }
                val bucketPositions =
                    buildDeleteBucketPositions(
                        workoutId = workoutId,
                        original = original,
                        currentWorkouts = currentWorkouts,
                    )

                val commandResult =
                    weeklyTrainingCommandRepository.deleteWorkout(
                        WorkoutDeleteCommand(
                            workoutId = workoutId,
                            displayWeekStart = state.value.weekStartDate,
                        ),
                    )

                if (commandResult !is WeeklyTrainingCommandResult.WorkoutDeleted) {
                    return@launch
                }

                if (original != null) {
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
            }

        fun undoLastAction() {
            val currentUndo = undoState.value ?: return

            clearUndoTimeout()

            viewModelScope.launch {
                when (val action = currentUndo.action) {
                    is PendingUndoAction.MoveOrReorder ->
                        weeklyTrainingCommandRepository.undoSchedule(action.toUndoScheduleCommand())
                    is PendingUndoAction.Delete ->
                        weeklyTrainingCommandRepository.undoDelete(action.toUndoDeleteCommand())
                    is PendingUndoAction.Completion ->
                        completionUpdateMutex.withLock {
                            pendingCompletionById[action.workout.id] = action.previousCompleted
                            weeklyTrainingCommandRepository.undoCompletion(
                                UndoCompletionCommand(
                                    workoutId = action.workout.id,
                                    previousCompleted = action.previousCompleted,
                                    newCompleted = action.newCompleted,
                                    displayWeekStart = action.weekStartDate,
                                ),
                            )
                        }
                    is PendingUndoAction.ReplaceWeek ->
                        weeklyTrainingCommandRepository.undoCopyLastWeek(action.toUndoCopyLastWeekCommand())
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
                    delay(UNDO_TIMEOUT_MS.milliseconds)

                    if (undoState.value?.id == undoId) {
                        undoState.value = null
                    }
                }
        }

        private fun clearUndoTimeout() {
            undoTimeoutJob?.cancel()
            undoTimeoutJob = null
        }

        private fun prunePendingCompletionOverrides(currentWorkouts: List<WorkoutUi>) {
            if (pendingCompletionById.isEmpty()) return

            val completionById = currentWorkouts.associate { it.id to it.isCompleted }

            pendingCompletionById.entries.removeAll { (workoutId, pendingCompletion) ->
                val currentCompletion = completionById[workoutId]
                currentCompletion == null || currentCompletion == pendingCompletion
            }
        }

        private fun applyPendingCompletionOverrides(currentWorkouts: List<WorkoutUi>): List<WorkoutUi> {
            if (pendingCompletionById.isEmpty()) return currentWorkouts

            return currentWorkouts.map { workout ->
                val pendingCompletion = pendingCompletionById[workout.id] ?: return@map workout

                if (workout.isCompleted == pendingCompletion) {
                    workout
                } else {
                    workout.copy(isCompleted = pendingCompletion)
                }
            }
        }

        private fun WorkoutUi.toWorkout(): Workout {
            return Workout(
                id = id,
                weekStartDate = weekStartDate,
                dayOfWeek = dayOfWeek,
                type = type,
                description = description,
                isCompleted = isCompleted,
                isRestDay = isRestDay,
                categoryId = categoryId,
                order = order,
                eventType = eventType,
                timeSlot = timeSlot,
            )
        }

        private fun PendingUndoAction.MoveOrReorder.toUndoScheduleCommand(): UndoScheduleCommand {
            return UndoScheduleCommand(
                movedWorkoutId = movedWorkoutId,
                displayWeekStart = weekStartDate,
                previousPositions = previousPositions.toScheduleChanges(),
            )
        }

        private fun PendingUndoAction.Delete.toUndoDeleteCommand(): UndoDeleteCommand {
            return UndoDeleteCommand(
                workout = workout.toWorkout(),
                displayWeekStart = weekStartDate,
                previousPositions = previousPositions.toScheduleChanges(),
            )
        }

        private fun PendingUndoAction.ReplaceWeek.toUndoCopyLastWeekCommand(): UndoCopyLastWeekCommand {
            return UndoCopyLastWeekCommand(
                targetStorageWeekStarts = storageWeekStartsForDisplayWeek(weekStartDate),
                targetDisplayWeekStart = weekStartDate,
                targetUnassignedStorageWeekStart = unassignedStorageWeekStart,
                previousWorkouts = previousWorkouts,
            )
        }

        private fun List<WorkoutPosition>.toScheduleChanges(): List<WorkoutScheduleChange> {
            return map { position ->
                WorkoutScheduleChange(
                    workoutId = position.id,
                    weekStartDate = position.weekStartDate,
                    dayOfWeek = position.dayOfWeek,
                    timeSlot = position.timeSlot,
                    order = position.order,
                )
            }
        }

        companion object {
            private const val UNDO_TIMEOUT_MS = 4_000L
        }
    }
