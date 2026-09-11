package com.rafaelfelipeac.hermes.features.events.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.AddRaceEventDialog
import com.rafaelfelipeac.hermes.core.ui.components.HermesSnackbar
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.events.presentation.model.EventDialogDraft
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.undoSnackbarMessage
import java.time.LocalDate

private const val SNACKBAR_EVENT_TITLE_PLACEHOLDER = "__EVENT_TITLE__"

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (EventDialogDraft) -> Unit = {},
    pendingEventDraft: EventDialogDraft? = null,
    onEventDraftConsumed: () -> Unit = {},
    weekStartDay: WeekStartDay,
    requestedEventId: Long? = null,
    onRequestedEventConsumed: () -> Unit = {},
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val undoState by viewModel.undoUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var isDialogVisible by rememberSaveable { mutableStateOf(false) }
    var editingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftTitle by rememberSaveable { mutableStateOf(EMPTY) }
    var draftDescription by rememberSaveable { mutableStateOf(EMPTY) }
    var draftCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftDate by rememberSaveable { mutableStateOf<LocalDate?>(null) }
    var deletingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftConsumedLocally by remember { mutableStateOf(false) }
    val undoLabel = stringResource(R.string.weekly_training_undo_action)
    val createdMessage =
        stringResource(
            R.string.activity_action_create_race_event,
            SNACKBAR_EVENT_TITLE_PLACEHOLDER,
        )
    val updatedMessage =
        stringResource(
            R.string.activity_action_update_race_event,
            SNACKBAR_EVENT_TITLE_PLACEHOLDER,
        )

    val editingEvent = editingEventId?.let { id -> state.events.firstOrNull { it.id == id } }
    val deletingEvent = deletingEventId?.let { id -> state.events.firstOrNull { it.id == id } }
    val categoriesForPicker =
        if (editingEvent == null) {
            state.categories.filterNot { it.isHidden }
        } else {
            val selectedCategoryId = draftCategoryId ?: editingEvent.categoryId
            val hiddenSelectedCategory =
                selectedCategoryId?.let { categoryId ->
                    state.categories.firstOrNull { it.id == categoryId && it.isHidden }
                }

            state.categories.filterNot { it.isHidden } + listOfNotNull(hiddenSelectedCategory)
        }
    val undoMessage =
        undoState?.let { currentUndo ->
            undoSnackbarMessage(
                message = currentUndo.message,
                eventType = RACE_EVENT,
            )
        }

    LaunchedEffect(undoState?.id) {
        if (undoMessage != null) {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result =
                snackbarHostState.showSnackbar(
                    message = undoMessage,
                    actionLabel = undoLabel,
                    duration = SnackbarDuration.Indefinite,
                )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoLastAction()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    LaunchedEffect(undoState) {
        if (undoState == null) {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    LaunchedEffect(editingEventId, requestedEventId) {
        if (editingEventId != null && editingEventId == requestedEventId) {
            onRequestedEventConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message =
                    when (message) {
                        is EventsMessage.Created ->
                            createdMessage.replace(
                                SNACKBAR_EVENT_TITLE_PLACEHOLDER,
                                message.title,
                            )
                        is EventsMessage.Updated ->
                            updatedMessage.replace(
                                SNACKBAR_EVENT_TITLE_PLACEHOLDER,
                                message.title,
                            )
                    },
                duration = SnackbarDuration.Short,
            )
        }
    }

    LaunchedEffect(pendingEventDraft, state.events, state.categories) {
        if (pendingEventDraft == null) {
            draftConsumedLocally = false
            return@LaunchedEffect
        }

        if (draftConsumedLocally) return@LaunchedEffect

        if (pendingEventDraft.eventId == null) {
            draftTitle = pendingEventDraft.title
            draftDescription = pendingEventDraft.description
            draftCategoryId = pendingEventDraft.categoryId
            draftDate = pendingEventDraft.eventDate
            editingEventId = null
            isDialogVisible = true
            draftConsumedLocally = true
            onEventDraftConsumed()
        } else {
            val event = state.events.firstOrNull { it.id == pendingEventDraft.eventId }

            if (event != null) {
                editingEventId = event.id
                draftTitle = pendingEventDraft.title
                draftDescription = pendingEventDraft.description
                draftCategoryId = pendingEventDraft.categoryId
                draftDate = pendingEventDraft.eventDate
                isDialogVisible = true
                draftConsumedLocally = true
                onEventDraftConsumed()
            }
        }
    }

    fun openEditDialog(event: WorkoutUi) {
        editingEventId = event.id
        draftTitle = event.type
        draftDescription = event.description
        draftCategoryId = event.categoryId
        draftDate = event.eventDate()
        isDialogVisible = true
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                HermesSnackbar(snackbarData = data)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEventId = null
                    draftTitle = EMPTY
                    draftDescription = EMPTY
                    draftCategoryId = null
                    draftDate = null
                    isDialogVisible = true
                },
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = stringResource(R.string.race_events_add),
                )
            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding =
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = Zero,
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding(),
            )

        EventsContent(
            state = state,
            modifier = modifier,
            contentPadding = contentPadding,
            requestedEventId = requestedEventId,
            onEditEvent = ::openEditDialog,
            onToggleCompleted = { eventId, checked ->
                viewModel.updateRaceEventCompletion(
                    eventId = eventId,
                    isCompleted = checked,
                )
            },
            onDeleteEvent = { eventId -> deletingEventId = eventId },
        )
    }

    if (isDialogVisible) {
        AddRaceEventDialog(
            onDismiss = {
                isDialogVisible = false
                editingEventId = null
            },
            onSave = { title, description, categoryId, eventDate ->
                val currentEditingEventId = editingEventId

                if (currentEditingEventId == null) {
                    viewModel.addRaceEvent(
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    )
                } else {
                    viewModel.updateRaceEvent(
                        eventId = currentEditingEventId,
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    )
                }

                isDialogVisible = false
                editingEventId = null
            },
            onManageCategories = { title, description, categoryId, eventDate ->
                isDialogVisible = false
                onManageCategories(
                    EventDialogDraft(
                        eventId = editingEventId,
                        title = title,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    ),
                )
            },
            isEdit = editingEvent != null,
            categories = categoriesForPicker,
            selectedCategoryId = draftCategoryId,
            weekStartDay = weekStartDay,
            selectedDate = draftDate,
            initialTitle = draftTitle,
            initialDescription = draftDescription,
        )
    }

    deletingEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { deletingEventId = null },
            title = { Text(text = stringResource(R.string.weekly_training_delete_race_event_title)) },
            text = { Text(text = stringResource(R.string.weekly_training_delete_race_event_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRaceEvent(event.id)
                        deletingEventId = null
                    },
                ) {
                    Text(text = stringResource(R.string.weekly_training_delete_race_event))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEventId = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }
}
