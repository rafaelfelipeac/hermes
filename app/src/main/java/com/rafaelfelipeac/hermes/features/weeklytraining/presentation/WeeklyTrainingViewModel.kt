@file:OptIn(ExperimentalCoroutinesApi::class)

package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.IS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.UNPLANNED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CONVERT_REST_DAY_TO_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CONVERT_WORKOUT_TO_REST_DAY
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.DELETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.OPEN_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UNDO_INCOMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_WORKOUT
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.AddWorkoutRequest
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
@Suppress("LargeClass")
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
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
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
        ) {
            val currentState = state.value
            val nextOrder = nextUnplannedOrder(currentState)
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = EventType.WORKOUT,
                    categoryId = categoryId,
                    categories = currentState.categories,
                )
            val categoryName =
                currentState.categories.firstOrNull { it.id == normalizedCategoryId }?.name

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(currentState.selectedDate)
                val workoutId =
                    repository.addWorkout(
                        AddWorkoutRequest(
                            weekStartDate = storageWeekStart,
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
                            putWorkoutCategoryMetadata(
                                categoryId = normalizedCategoryId,
                                categoryName = categoryName,
                                newCategoryId = normalizedCategoryId,
                                newCategoryName = categoryName,
                            )
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
            val categoryName =
                currentState.categories.firstOrNull { it.id == normalizedCategoryId }?.name

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(eventDate)
                val dayOfWeek = eventDate.dayOfWeek
                val nextOrder =
                    repository.getWorkoutsForWeek(storageWeekStart)
                        .count { workout ->
                            workout.dayOfWeek == dayOfWeek &&
                                workout.timeSlot == null &&
                                workout.eventType == EventType.RACE_EVENT
                        }

                val eventId =
                    repository.insertWorkout(
                        Workout(
                            id = 0L,
                            weekStartDate = storageWeekStart,
                            dayOfWeek = dayOfWeek,
                            type = type,
                            description = description,
                            isCompleted = false,
                            isRestDay = false,
                            categoryId = normalizedCategoryId,
                            order = nextOrder,
                            eventType = EventType.RACE_EVENT,
                            timeSlot = null,
                        ),
                    )

                userActionLogger.log(
                    actionType = EventType.RACE_EVENT.toCreateActionType(),
                    entityType = EventType.RACE_EVENT.toUserActionEntityType(),
                    entityId = eventId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to storageWeekStart.toString(),
                            DAY_OF_WEEK to dayOfWeek.value.toString(),
                            NEW_ORDER to nextOrder.toString(),
                            NEW_TYPE to type,
                            NEW_DESCRIPTION to description,
                        ).apply {
                            putWorkoutCategoryMetadata(
                                categoryId = normalizedCategoryId,
                                categoryName = categoryName,
                                newCategoryId = normalizedCategoryId,
                                newCategoryName = categoryName,
                            )
                        },
                )
            }
        }

        private fun addNonWorkoutEvent(eventType: EventType) {
            val currentState = state.value
            val nextOrder = nextUnplannedOrder(currentState)

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(currentState.selectedDate)
                val eventId =
                    repository.addEvent(
                        weekStartDate = storageWeekStart,
                        dayOfWeek = null,
                        eventType = eventType,
                        order = nextOrder,
                    )

                userActionLogger.log(
                    actionType = eventType.toCreateActionType(),
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

                val targetWorkouts =
                    repository
                        .replaceWorkoutsForDisplayWeek(
                            targetStorageWeekStarts = currentStorageWeekStarts,
                            targetDisplayWeekStart = currentDisplayWeekStartDate,
                            targetUnassignedStorageWeekStart = currentUnassignedStorageWeekStart,
                            replacementWorkouts = sourceWorkouts.map(::copyWorkoutToNextWeek),
                        ).getOrElse {
                            return@launch
                        }

                userActionLogger.log(
                    actionType = COPY_LAST_WEEK,
                    entityType = WEEK,
                    metadata =
                        mapOf(
                            WEEK_START_DATE to currentDisplayWeekStartDate.toString(),
                            OLD_WEEK_START_DATE to previousDisplayWeekStartDate.toString(),
                            NEW_WEEK_START_DATE to currentDisplayWeekStartDate.toString(),
                        ),
                )

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
                persistWorkoutChanges(
                    dependencies =
                        WorkoutChangeDependencies(
                            repository = repository,
                            userActionLogger = userActionLogger,
                            weekStartDate = state.value.weekStartDate,
                            displayStartDay = state.value.weekStartDay,
                            unassignedStorageWeekStart = canonicalStorageWeekStart(state.value.selectedDate),
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
                if (workout.eventType != EventType.WORKOUT || originalEffective?.eventType != EventType.WORKOUT) {
                    return@withLock
                }

                if (originalEffective.isCompleted == isCompleted) {
                    return@withLock
                }

                pendingCompletionById[workout.id] = isCompleted
                val optimisticWorkouts = applyPendingCompletionOverrides(currentWorkouts)

                repository.updateWorkoutCompletion(workout.id, isCompleted)

                val actionType =
                    if (isCompleted) COMPLETE_WORKOUT else INCOMPLETE_WORKOUT

                userActionLogger.log(
                    actionType = actionType,
                    entityType = WORKOUT,
                    entityId = workout.id,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to state.value.weekStartDate.toString(),
                            WAS_COMPLETED to originalEffective.isCompleted.toString(),
                            IS_COMPLETED to isCompleted.toString(),
                            NEW_TYPE to workout.type,
                            NEW_DESCRIPTION to workout.description,
                        ).apply {
                            putWorkoutCategoryMetadata(
                                categoryId = workout.categoryId,
                                categoryName = workout.categoryName,
                                newCategoryName = workout.categoryName,
                            )
                        },
                )

                val message =
                    if (isCompleted) {
                        if (
                            shouldCelebrateAllWorkoutsCompleted(
                                currentWorkouts = optimisticWorkouts,
                                workoutId = workout.id,
                                previousIsCompleted = originalEffective.isCompleted,
                                newIsCompleted = isCompleted,
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
                            workout = originalEffective,
                            previousCompleted = originalEffective.isCompleted,
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
            val oldCategoryId =
                original?.takeIf {
                    it.eventType == EventType.WORKOUT || it.eventType == EventType.RACE_EVENT
                }?.categoryId

            repository.updateWorkoutDetails(
                workoutId = workoutId,
                type = type,
                description = description,
                eventType = eventType,
                categoryId = normalizedCategoryId,
            )

            val entityType =
                when {
                    eventType != EventType.WORKOUT -> eventType.toUserActionEntityType()
                    else -> original?.eventType?.toUserActionEntityType() ?: WORKOUT
                }
            val actionType =
                when {
                    original == null -> UPDATE_WORKOUT
                    original.eventType != eventType ->
                        when (eventType) {
                            EventType.WORKOUT -> CONVERT_REST_DAY_TO_WORKOUT
                            EventType.RACE_EVENT -> eventType.toUpdateActionType()
                            else -> CONVERT_WORKOUT_TO_REST_DAY
                        }
                    eventType != EventType.WORKOUT -> eventType.toUpdateActionType()
                    else -> UPDATE_WORKOUT
                }

            logWorkoutDetailsUpdate(
                userActionLogger = userActionLogger,
                actionType = actionType,
                entityType = entityType,
                workoutId = workoutId,
                metadataInput =
                    WorkoutUpdateMetadataInput(
                        weekStartDate = state.value.weekStartDate.toString(),
                        original = original,
                        type = type,
                        description = description,
                        isRestDay = eventType == EventType.REST,
                        oldCategoryId = oldCategoryId,
                        newCategoryId = normalizedCategoryId,
                        oldCategoryName = oldCategoryName,
                        newCategoryName = newCategoryName,
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
            val original = state.value.workouts.firstOrNull { it.id == workoutId }
            val normalizedCategoryId =
                resolveCategoryId(
                    eventType = EventType.RACE_EVENT,
                    categoryId = categoryId,
                    categories = state.value.categories,
                )
            val (oldCategoryName, newCategoryName) =
                resolveCategoryNames(
                    eventType = EventType.RACE_EVENT,
                    normalizedCategoryId = normalizedCategoryId,
                    categories = state.value.categories,
                    original = original,
                )
            val storageWeekStart = canonicalStorageWeekStart(eventDate)
            val dayOfWeek = eventDate.dayOfWeek
            val nextOrder =
                repository.getWorkoutsForWeek(storageWeekStart)
                    .count { workout ->
                        workout.id != workoutId &&
                            workout.dayOfWeek == dayOfWeek &&
                            workout.timeSlot == null &&
                            workout.eventType == EventType.RACE_EVENT
                    }

            repository.updateWorkoutSchedule(
                workoutId = workoutId,
                weekStartDate = storageWeekStart,
                dayOfWeek = dayOfWeek,
                timeSlot = null,
                order = nextOrder,
            )
            repository.updateWorkoutDetails(
                workoutId = workoutId,
                type = type,
                description = description,
                eventType = EventType.RACE_EVENT,
                categoryId = normalizedCategoryId,
            )

            val dateChanged =
                original?.weekStartDate != storageWeekStart || original?.dayOfWeek != dayOfWeek
            val actionType =
                if (dateChanged) {
                    EventType.RACE_EVENT.toMoveActionType()
                } else {
                    EventType.RACE_EVENT.toUpdateActionType()
                }

            userActionLogger.log(
                actionType = actionType,
                entityType = EventType.RACE_EVENT.toUserActionEntityType(),
                entityId = workoutId,
                metadata =
                    mutableMapOf(
                        WEEK_START_DATE to storageWeekStart.toString(),
                        OLD_WEEK_START_DATE to (original?.weekStartDate?.toString() ?: storageWeekStart.toString()),
                        NEW_WEEK_START_DATE to storageWeekStart.toString(),
                        OLD_DAY_OF_WEEK to (original?.dayOfWeek?.value?.toString() ?: dayOfWeek.value.toString()),
                        NEW_DAY_OF_WEEK to dayOfWeek.value.toString(),
                        OLD_ORDER to (original?.order?.toString() ?: nextOrder.toString()),
                        NEW_ORDER to nextOrder.toString(),
                        OLD_TYPE to (original?.type ?: EMPTY),
                        NEW_TYPE to type,
                        OLD_DESCRIPTION to (original?.description ?: EMPTY),
                        NEW_DESCRIPTION to description,
                    ).apply {
                        putWorkoutCategoryMetadata(
                            categoryId = normalizedCategoryId,
                            categoryName = newCategoryName,
                            oldCategoryId = original?.categoryId,
                            newCategoryId = normalizedCategoryId,
                            oldCategoryName = oldCategoryName,
                            newCategoryName = newCategoryName,
                        )
                    },
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

                logWorkoutDeletion(
                    userActionLogger = userActionLogger,
                    workoutId = workoutId,
                    weekStartDate = state.value.weekStartDate,
                    original = original,
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
                        completionUpdateMutex.withLock {
                            pendingCompletionById[action.workout.id] = action.previousCompleted
                            undoCompletion(
                                action = action,
                                repository = repository,
                                userActionLogger = userActionLogger,
                            )
                        }
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

        companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
            private const val UNDO_TIMEOUT_MS = 4_000L
        }
    }

private fun copyWorkoutToNextWeek(workout: Workout): Workout {
    val nextWeekStart =
        if (workout.dayOfWeek == null) {
            workout.weekStartDate.plusWeeks(1)
        } else {
            val workoutDate = workout.weekStartDate.plusDays((workout.dayOfWeek.value - 1).toLong())
            canonicalStorageWeekStart(workoutDate.plusWeeks(1))
        }

    return workout.copy(
        id = 0L,
        weekStartDate = nextWeekStart,
        isCompleted = false,
    )
}

private fun mapWorkoutsToUi(
    workouts: List<Workout>,
    categories: List<CategoryUi>,
): List<WorkoutUi> {
    val categoriesById = categories.associateBy { it.id }
    val fallbackCategory = categoriesById[UNCATEGORIZED_ID]

    return workouts.map { workout ->
        val category =
            if (workout.eventType != EventType.WORKOUT && workout.eventType != EventType.RACE_EVENT) {
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
            mutableMapOf(
                WEEK_START_DATE to action.weekStartDate.toString(),
                WAS_COMPLETED to action.newCompleted.toString(),
                IS_COMPLETED to action.previousCompleted.toString(),
                NEW_TYPE to action.workout.type,
                NEW_DESCRIPTION to action.workout.description,
            ).apply {
                putWorkoutCategoryMetadata(
                    categoryId = action.workout.categoryId,
                    categoryName = action.workout.categoryName,
                    newCategoryName = action.workout.categoryName,
                )
            },
    )
}

private fun normalizeCategoryId(
    eventType: EventType,
    categoryId: Long?,
): Long? {
    return if (eventType != EventType.WORKOUT && eventType != EventType.RACE_EVENT) {
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
    val oldCategoryName =
        original?.takeIf {
            it.eventType == EventType.WORKOUT || it.eventType == EventType.RACE_EVENT
        }?.categoryName
    val newCategoryName =
        if ((eventType != EventType.WORKOUT && eventType != EventType.RACE_EVENT) || normalizedCategoryId == null) {
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
            input.oldCategoryId?.let { put(OLD_CATEGORY_ID, it.toString()) }
            input.newCategoryId?.let {
                put(NEW_CATEGORY_ID, it.toString())
                put(CATEGORY_ID, it.toString())
            }

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

private suspend fun logWorkoutDetailsUpdate(
    userActionLogger: UserActionLogger,
    actionType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionType,
    entityType: com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType,
    workoutId: Long,
    metadataInput: WorkoutUpdateMetadataInput,
) {
    userActionLogger.log(
        actionType = actionType,
        entityType = entityType,
        entityId = workoutId,
        metadata = buildWorkoutUpdateMetadata(metadataInput),
    )
}

private fun buildDeleteBucketPositions(
    workoutId: Long,
    original: WorkoutUi?,
    currentWorkouts: List<WorkoutUi>,
): List<WorkoutPosition> {
    return original?.let { workout ->
        currentWorkouts
            .asSequence()
            .filter { it.dayOfWeek == workout.dayOfWeek }
            .filter { it.timeSlot == workout.timeSlot }
            .sortedBy { it.order }
            .filter { it.id != workoutId }
            .map {
                WorkoutPosition(
                    id = it.id,
                    weekStartDate = it.weekStartDate,
                    dayOfWeek = it.dayOfWeek,
                    timeSlot = it.timeSlot,
                    order = it.order,
                )
            }.toList()
    }.orEmpty()
}

private suspend fun logWorkoutDeletion(
    userActionLogger: UserActionLogger,
    workoutId: Long,
    weekStartDate: LocalDate,
    original: WorkoutUi?,
) {
    val entityType = original?.eventType?.toUserActionEntityType() ?: WORKOUT
    val actionType = original?.eventType?.toDeleteActionType() ?: DELETE_WORKOUT

    userActionLogger.log(
        actionType = actionType,
        entityType = entityType,
        entityId = workoutId,
        metadata =
            mutableMapOf(
                WEEK_START_DATE to weekStartDate.toString(),
                OLD_TYPE to (original?.type ?: EMPTY),
                OLD_DESCRIPTION to (original?.description ?: EMPTY),
            ).apply {
                putWorkoutCategoryMetadata(
                    categoryId = original?.categoryId,
                    categoryName = original?.categoryName,
                    oldCategoryId = original?.categoryId,
                    oldCategoryName = original?.categoryName,
                )
            },
    )
}

private fun nextUnplannedOrder(state: WeeklyTrainingState): Int {
    return state.workouts.count { it.dayOfWeek == null }
}
