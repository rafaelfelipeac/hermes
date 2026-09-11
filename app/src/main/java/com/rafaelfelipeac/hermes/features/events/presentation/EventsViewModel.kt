package com.rafaelfelipeac.hermes.features.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.flow.stateInWhileSubscribed
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategorySeeder
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.CreateWeeklyItemCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.UndoDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WeeklyTrainingCommandResult
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutCompletionCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDeleteCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.command.WorkoutDetailsCommand
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.UndoMessage
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi as toWorkoutUi

@HiltViewModel
class EventsViewModel
    @Inject
    constructor(
        private val repository: WeeklyTrainingRepository,
        private val categoryRepository: CategoryRepository,
        private val categorySeeder: CategorySeeder,
        private val weeklyTrainingCommandRepository: WeeklyTrainingCommandRepository,
    ) : ViewModel() {
        private val messageEvents = MutableSharedFlow<EventsMessage>(extraBufferCapacity = 1)
        private val undoState = MutableStateFlow<EventUndoState?>(null)
        private var undoTimeoutJob: Job? = null
        private var undoCounter = 0L

        val state =
            combine(
                repository.observeWorkoutsByEventType(RACE_EVENT),
                categoryRepository.observeCategories(),
            ) { workouts, categories ->
                val categoriesById = categories.associateBy { it.id }
                EventsUiState(
                    events =
                        workouts.asSequence()
                            .map { workout ->
                                val category = workout.categoryId?.let(categoriesById::get)
                                workout.toWorkoutUi(category?.toUi())
                            }
                            .toList(),
                    categories = categories.map { it.toUi() },
                )
            }.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = EventsUiState(),
            )

        val messages: SharedFlow<EventsMessage> = messageEvents.asSharedFlow()
        val undoUiState: StateFlow<EventUndoState?> =
            undoState.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = null,
            )

        init {
            viewModelScope.launch {
                categorySeeder.ensureSeeded()
            }
        }

        fun addRaceEvent(
            title: String,
            description: String,
            categoryId: Long?,
            eventDate: LocalDate,
        ) {
            if (eventDate.isBefore(LocalDate.now())) return

            val currentCategories = state.value.categories
            val normalizedCategoryId =
                if (currentCategories.any { it.id == categoryId }) {
                    categoryId
                } else {
                    UNCATEGORIZED_ID
                }
            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(eventDate)
                val dayOfWeek = eventDate.dayOfWeek
                val result =
                    weeklyTrainingCommandRepository.createItem(
                        CreateWeeklyItemCommand(
                            eventType = RACE_EVENT,
                            storageWeekStart = storageWeekStart,
                            displayWeekStart = storageWeekStart,
                            dayOfWeek = dayOfWeek,
                            timeSlot = null,
                            type = title,
                            description = description,
                            categoryId = normalizedCategoryId,
                        ),
                    )

                if (result is WeeklyTrainingCommandResult.ItemCreated) {
                    messageEvents.emit(EventsMessage.Created(title))
                }
            }
        }

        fun updateRaceEvent(
            eventId: Long,
            title: String,
            description: String,
            categoryId: Long?,
            eventDate: LocalDate,
        ) {
            val original = state.value.events.firstOrNull { it.id == eventId } ?: return
            val currentCategories = state.value.categories
            val normalizedCategoryId =
                if (currentCategories.any { it.id == categoryId }) {
                    categoryId
                } else {
                    UNCATEGORIZED_ID
                }
            val storageWeekStart = canonicalStorageWeekStart(eventDate)
            val dayOfWeek = eventDate.dayOfWeek
            val dateChanged =
                original.weekStartDate != storageWeekStart || original.dayOfWeek != dayOfWeek

            if (dateChanged && eventDate.isBefore(LocalDate.now())) return

            viewModelScope.launch {
                val result =
                    weeklyTrainingCommandRepository.updateDetails(
                        WorkoutDetailsCommand(
                            workoutId = eventId,
                            type = title,
                            description = description,
                            eventType = RACE_EVENT,
                            categoryId = normalizedCategoryId,
                            displayWeekStart = storageWeekStart,
                            targetDate = eventDate,
                        ),
                    )

                if (result is WeeklyTrainingCommandResult.DetailsChanged) {
                    messageEvents.emit(EventsMessage.Updated(title))
                }
            }
        }

        fun updateRaceEventCompletion(
            eventId: Long,
            isCompleted: Boolean,
        ) {
            viewModelScope.launch {
                val original = state.value.events.firstOrNull { it.id == eventId } ?: return@launch
                val result =
                    weeklyTrainingCommandRepository.updateCompletion(
                        WorkoutCompletionCommand(
                            workoutId = eventId,
                            isCompleted = isCompleted,
                            displayWeekStart = original.weekStartDate,
                        ),
                    )

                if (result !is WeeklyTrainingCommandResult.CompletionChanged) {
                    return@launch
                }

                setUndoAction(
                    action =
                        PendingEventUndoAction.Completion(
                            event = original.copy(isCompleted = result.previousCompleted),
                            previousCompleted = result.previousCompleted,
                            newCompleted = isCompleted,
                        ),
                    message =
                        if (isCompleted) {
                            UndoMessage.Completed
                        } else {
                            UndoMessage.MarkedIncomplete
                        },
                )
            }
        }

        fun deleteRaceEvent(eventId: Long) {
            viewModelScope.launch {
                val original = state.value.events.firstOrNull { it.id == eventId } ?: return@launch
                val result =
                    weeklyTrainingCommandRepository.deleteWorkout(
                        WorkoutDeleteCommand(
                            workoutId = eventId,
                            displayWeekStart = original.weekStartDate,
                        ),
                    )

                if (result is WeeklyTrainingCommandResult.WorkoutDeleted) {
                    setUndoAction(
                        action = PendingEventUndoAction.Delete(original),
                        message = UndoMessage.Deleted,
                    )
                }
            }
        }

        fun undoLastAction() {
            val currentUndo = undoState.value ?: return
            clearUndoTimeout()

            viewModelScope.launch {
                when (val action = currentUndo.action) {
                    is PendingEventUndoAction.Delete -> undoDelete(action)
                    is PendingEventUndoAction.Completion -> undoCompletion(action)
                }

                undoState.value = null
            }
        }

        fun clearUndo() {
            clearUndoTimeout()
            undoState.value = null
        }

        private suspend fun undoDelete(action: PendingEventUndoAction.Delete) {
            val event = action.event
            val restoredOrder = nextRaceEventOrder(event.weekStartDate, event.dayOfWeek)
            weeklyTrainingCommandRepository.undoDelete(
                UndoDeleteCommand(
                    workout = event.toDomain().copy(order = restoredOrder),
                    displayWeekStart = event.weekStartDate,
                    previousPositions = emptyList(),
                ),
            )
        }

        private suspend fun nextRaceEventOrder(
            weekStartDate: LocalDate,
            dayOfWeek: DayOfWeek?,
            excludeWorkoutId: Long? = null,
        ): Int {
            return repository.getWorkoutsForWeek(weekStartDate)
                .asSequence()
                .filter { workout ->
                    workout.id != excludeWorkoutId &&
                        workout.dayOfWeek == dayOfWeek &&
                        workout.timeSlot == null
                }
                .maxOfOrNull { it.order }
                ?.plus(1)
                ?: 0
        }

        private suspend fun undoCompletion(action: PendingEventUndoAction.Completion) {
            weeklyTrainingCommandRepository.undoCompletion(
                UndoCompletionCommand(
                    workoutId = action.event.id,
                    previousCompleted = action.previousCompleted,
                    newCompleted = action.newCompleted,
                    displayWeekStart = action.event.weekStartDate,
                ),
            )
        }

        private fun setUndoAction(
            action: PendingEventUndoAction,
            message: UndoMessage,
        ) {
            val newId = ++undoCounter
            undoState.value = EventUndoState(id = newId, message = message, action = action)
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

        private companion object {
            const val UNDO_TIMEOUT_MS = 4_000L
        }
    }

private fun WorkoutUi.toDomain(): Workout {
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
