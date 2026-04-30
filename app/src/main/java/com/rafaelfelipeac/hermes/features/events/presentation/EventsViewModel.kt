package com.rafaelfelipeac.hermes.features.events.presentation

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
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DESCRIPTION
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_ORDER
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WAS_COMPLETED
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.toCreateActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.toDeleteActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.toMoveActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.toUpdateActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.toUserActionEntityType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.mapper.toUi as toWorkoutUi

@HiltViewModel
class EventsViewModel
    @Inject
    constructor(
        private val repository: WeeklyTrainingRepository,
        private val categoryRepository: CategoryRepository,
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        private val messageEvents = MutableSharedFlow<EventsMessage>(extraBufferCapacity = 1)

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
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = EventsUiState(),
            )

        val messages: SharedFlow<EventsMessage> = messageEvents.asSharedFlow()

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
            val categoryName = currentCategories.firstOrNull { it.id == normalizedCategoryId }?.name

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(eventDate)
                val dayOfWeek = eventDate.dayOfWeek
                val nextOrder =
                    repository.getWorkoutsForWeek(storageWeekStart)
                        .count { workout ->
                            workout.dayOfWeek == dayOfWeek &&
                                workout.timeSlot == null &&
                                workout.eventType == RACE_EVENT
                        }

                val workout =
                    Workout(
                        id = 0L,
                        weekStartDate = storageWeekStart,
                        dayOfWeek = dayOfWeek,
                        type = title,
                        description = description,
                        isCompleted = false,
                        isRestDay = false,
                        categoryId = normalizedCategoryId,
                        order = nextOrder,
                        eventType = RACE_EVENT,
                        timeSlot = null,
                    )

                val eventId = repository.insertWorkout(workout)

                userActionLogger.log(
                    actionType = RACE_EVENT.toCreateActionType(),
                    entityType = RACE_EVENT.toUserActionEntityType(),
                    entityId = eventId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to storageWeekStart.toString(),
                            DAY_OF_WEEK to dayOfWeek.value.toString(),
                            NEW_ORDER to nextOrder.toString(),
                            NEW_TYPE to title,
                            NEW_DESCRIPTION to description,
                        ).apply {
                            categoryId?.let { put(CATEGORY_ID, it.toString()) }
                            if (!categoryName.isNullOrBlank()) {
                                put(CATEGORY_NAME, categoryName)
                                put(NEW_CATEGORY_NAME, categoryName)
                            }
                        },
                )

                messageEvents.emit(EventsMessage.Created(title))
            }
        }

        @Suppress("CyclomaticComplexMethod", "LongMethod")
        fun updateRaceEvent(
            eventId: Long,
            title: String,
            description: String,
            categoryId: Long?,
            eventDate: LocalDate,
        ) {
            if (eventDate.isBefore(LocalDate.now())) return

            val original = state.value.events.firstOrNull { it.id == eventId }
            val currentCategories = state.value.categories
            val normalizedCategoryId =
                if (currentCategories.any { it.id == categoryId }) {
                    categoryId
                } else {
                    UNCATEGORIZED_ID
                }
            val categoryName = currentCategories.firstOrNull { it.id == normalizedCategoryId }?.name

            viewModelScope.launch {
                val storageWeekStart = canonicalStorageWeekStart(eventDate)
                val dayOfWeek = eventDate.dayOfWeek
                val nextOrder =
                    repository.getWorkoutsForWeek(storageWeekStart)
                        .count { workout ->
                            workout.id != eventId &&
                                workout.dayOfWeek == dayOfWeek &&
                                workout.timeSlot == null &&
                                workout.eventType == RACE_EVENT
                        }

                repository.updateWorkoutSchedule(
                    workoutId = eventId,
                    weekStartDate = storageWeekStart,
                    dayOfWeek = dayOfWeek,
                    timeSlot = null,
                    order = nextOrder,
                )
                repository.updateWorkoutDetails(
                    workoutId = eventId,
                    type = title,
                    description = description,
                    eventType = RACE_EVENT,
                    categoryId = normalizedCategoryId,
                )

                val dateChanged =
                    original?.weekStartDate != storageWeekStart || original?.dayOfWeek != dayOfWeek
                val actionType =
                    if (dateChanged) {
                        RACE_EVENT.toMoveActionType()
                    } else {
                        RACE_EVENT.toUpdateActionType()
                    }

                userActionLogger.log(
                    actionType = actionType,
                    entityType = RACE_EVENT.toUserActionEntityType(),
                    entityId = eventId,
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
                            NEW_TYPE to title,
                            OLD_DESCRIPTION to (original?.description ?: EMPTY),
                            NEW_DESCRIPTION to description,
                        ).apply {
                            original?.categoryId?.let { put(OLD_CATEGORY_ID, it.toString()) }
                            normalizedCategoryId?.let {
                                put(CATEGORY_ID, it.toString())
                                put(NEW_CATEGORY_ID, it.toString())
                            }
                            original?.categoryName?.takeIf { it.isNotBlank() }?.let {
                                put(OLD_CATEGORY_NAME, it)
                            }
                            categoryName?.takeIf { it.isNotBlank() }?.let {
                                put(CATEGORY_NAME, it)
                                put(NEW_CATEGORY_NAME, it)
                            }
                        },
                )

                messageEvents.emit(EventsMessage.Updated(title))
            }
        }

        fun updateRaceEventCompletion(
            eventId: Long,
            isCompleted: Boolean,
        ) {
            viewModelScope.launch {
                val original = state.value.events.firstOrNull { it.id == eventId } ?: return@launch
                if (original.isCompleted == isCompleted) return@launch

                repository.updateWorkoutCompletion(eventId, isCompleted)

                userActionLogger.log(
                    actionType =
                        if (isCompleted) {
                            com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_RACE_EVENT
                        } else {
                            com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.INCOMPLETE_RACE_EVENT
                        },
                    entityType = RACE_EVENT.toUserActionEntityType(),
                    entityId = eventId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to original.weekStartDate.toString(),
                            WAS_COMPLETED to original.isCompleted.toString(),
                            IS_COMPLETED to isCompleted.toString(),
                            NEW_TYPE to original.type,
                            NEW_DESCRIPTION to original.description,
                        ).apply {
                            original.categoryId?.let { put(CATEGORY_ID, it.toString()) }
                            original.categoryName?.takeIf { it.isNotBlank() }?.let {
                                put(CATEGORY_NAME, it)
                                put(NEW_CATEGORY_NAME, it)
                            }
                        },
                )

                messageEvents.emit(
                    if (isCompleted) {
                        EventsMessage.Completed
                    } else {
                        EventsMessage.MarkedIncomplete
                    },
                )
            }
        }

        fun deleteRaceEvent(eventId: Long) {
            viewModelScope.launch {
                val original = state.value.events.firstOrNull { it.id == eventId }
                repository.deleteWorkout(eventId)

                userActionLogger.log(
                    actionType = RACE_EVENT.toDeleteActionType(),
                    entityType = RACE_EVENT.toUserActionEntityType(),
                    entityId = eventId,
                    metadata =
                        mutableMapOf(
                            WEEK_START_DATE to (original?.weekStartDate?.toString() ?: EMPTY),
                            NEW_TYPE to (original?.type ?: EMPTY),
                            NEW_DESCRIPTION to (original?.description ?: EMPTY),
                        ).apply {
                            original?.categoryId?.let { put(CATEGORY_ID, it.toString()) }
                            original?.categoryName?.takeIf { it.isNotBlank() }?.let {
                                put(CATEGORY_NAME, it)
                                put(NEW_CATEGORY_NAME, it)
                            }
                        },
                )

                messageEvents.emit(EventsMessage.Deleted(original?.type ?: EMPTY))
            }
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }
