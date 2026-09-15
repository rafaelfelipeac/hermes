package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.HermesSnackbar
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.LocalDate

private const val ADD_FAB_TEST_TAG = "add-fab"

@Composable
fun WeeklyTrainingScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (WorkoutDialogDraft) -> Unit = {},
    pendingWorkoutDraft: WorkoutDialogDraft? = null,
    onWorkoutDraftConsumed: () -> Unit = {},
    requestedWorkoutId: Long? = null,
    requestedWorkoutDate: LocalDate? = null,
    requestedWorkoutRequestKey: Long = 0L,
    onRequestedWorkoutDateConsumed: () -> Unit = {},
    onRequestedWorkoutConsumed: () -> Unit = {},
    viewModel: WeeklyTrainingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val undoState by viewModel.undoUiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isAddMenuVisible by rememberSaveable { mutableStateOf(false) }
    var editingWorkout by remember { mutableStateOf<WorkoutUi?>(null) }
    var deletingWorkout by remember { mutableStateOf<WorkoutUi?>(null) }
    var isCopyReplaceDialogVisible by rememberSaveable { mutableStateOf(false) }
    var draftType by rememberSaveable { mutableStateOf(EMPTY) }
    var draftDescription by rememberSaveable { mutableStateOf(EMPTY) }
    var draftCategoryId by rememberSaveable { mutableStateOf<Long?>(UNCATEGORIZED_ID) }
    var draftEventDate by remember { mutableStateOf<LocalDate?>(state.selectedDate) }
    var requestedWorkoutRequestToken by remember { mutableStateOf<Long?>(null) }
    var requestedWorkoutIdToOpen by remember { mutableStateOf<Long?>(null) }
    var requestedWorkoutDateToOpen by remember { mutableStateOf<LocalDate?>(null) }
    var isRaceEventDialogVisible by rememberSaveable { mutableStateOf(false) }
    var focusedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftConsumedLocally by remember { mutableStateOf(false) }
    val fabContainerColor = colorScheme.primaryContainer
    val fabContentColor = colorScheme.onPrimaryContainer
    val undoLabel = stringResource(R.string.weekly_training_undo_action)
    val emptyCopyMessage = stringResource(R.string.weekly_training_copy_last_week_empty)
    val mockWorkoutType = stringResource(R.string.mock_workout_type)
    val mockWorkoutDescription = stringResource(R.string.mock_workout_description)
    val pickerCategories = state.categories.filter { !it.isHidden || it.id == UNCATEGORIZED_ID }
    val plannerFocusCategories =
        pickerCategories.sortedWith(
            compareBy<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi> {
                it.id == UNCATEGORIZED_ID
            }
                .thenBy { it.sortOrder },
        )
    val undoMessage =
        undoState?.let { currentUndo ->
            val eventType =
                when (val action = currentUndo.action) {
                    is PendingUndoAction.Delete -> action.workout.eventType
                    is PendingUndoAction.Completion -> action.workout.eventType
                    is PendingUndoAction.MoveOrReorder -> action.movedEventType
                    is PendingUndoAction.ReplaceWeek -> WORKOUT
                }

            undoSnackbarMessage(
                message = currentUndo.message,
                eventType = eventType,
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

    LaunchedEffect(requestedWorkoutRequestKey) {
        requestedWorkoutRequestToken = requestedWorkoutRequestKey
        requestedWorkoutIdToOpen = requestedWorkoutId
        requestedWorkoutDateToOpen = requestedWorkoutDate
        if (requestedWorkoutDate != null) {
            viewModel.onDateSelected(requestedWorkoutDate)
            onRequestedWorkoutDateConsumed()
        }
    }

    LaunchedEffect(
        requestedWorkoutRequestToken,
        requestedWorkoutIdToOpen,
        requestedWorkoutDateToOpen,
        state.selectedDate,
        state.workouts,
    ) {
        val targetWorkoutId = requestedWorkoutIdToOpen ?: return@LaunchedEffect

        if (requestedWorkoutDateToOpen != null && state.selectedDate != requestedWorkoutDateToOpen) {
            return@LaunchedEffect
        }

        val targetWorkout = state.workouts.firstOrNull { it.id == targetWorkoutId } ?: return@LaunchedEffect

        if (editingWorkout?.id != targetWorkout.id) {
            editingWorkout = targetWorkout
        }
    }

    LaunchedEffect(editingWorkout?.id, requestedWorkoutRequestToken) {
        val openedWorkoutId = editingWorkout?.id
        if (openedWorkoutId != null && openedWorkoutId == requestedWorkoutIdToOpen) {
            requestedWorkoutRequestToken = null
            requestedWorkoutIdToOpen = null
            requestedWorkoutDateToOpen = null
            onRequestedWorkoutConsumed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            when (message) {
                WeeklyTrainingMessage.NothingToCopyFromLastWeek ->
                    snackbarHostState.showSnackbar(
                        message = emptyCopyMessage,
                        duration = SnackbarDuration.Short,
                    )
            }
        }
    }

    LaunchedEffect(pendingWorkoutDraft, state.categories, state.workouts) {
        if (pendingWorkoutDraft == null) {
            draftConsumedLocally = false
            return@LaunchedEffect
        }

        if (draftConsumedLocally) return@LaunchedEffect

        if (pendingWorkoutDraft.workoutId == null) {
            draftType = pendingWorkoutDraft.type
            draftDescription = pendingWorkoutDraft.description
            draftCategoryId = pendingWorkoutDraft.categoryId ?: UNCATEGORIZED_ID
            draftEventDate = pendingWorkoutDraft.eventDate
            if (pendingWorkoutDraft.isRaceEvent) {
                draftEventDate = pendingWorkoutDraft.eventDate
                isRaceEventDialogVisible = true
            } else {
                isAddDialogVisible = true
            }
            draftConsumedLocally = true
            onWorkoutDraftConsumed()
        } else {
            val workout = state.workouts.firstOrNull { it.id == pendingWorkoutDraft.workoutId }
            val category = state.categories.firstOrNull { it.id == pendingWorkoutDraft.categoryId }

            if (workout != null) {
                editingWorkout =
                    workout.copy(
                        type = pendingWorkoutDraft.type,
                        description = pendingWorkoutDraft.description,
                        categoryId = pendingWorkoutDraft.categoryId,
                        categoryName = category?.name,
                        categoryColorId = category?.colorId,
                    )
                draftEventDate = pendingWorkoutDraft.eventDate
                draftConsumedLocally = true
                onWorkoutDraftConsumed()
            }
        }
    }

    LaunchedEffect(focusedCategoryId, plannerFocusCategories) {
        if (focusedCategoryId != null && plannerFocusCategories.none { it.id == focusedCategoryId }) {
            focusedCategoryId = null
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                HermesSnackbar(snackbarData = data)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddMenuVisible = !isAddMenuVisible },
                containerColor = fabContainerColor,
                contentColor = fabContentColor,
                modifier = Modifier.testTag(ADD_FAB_TEST_TAG),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.weekly_training_add_item),
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
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = SpacingXl,
                            top = SpacingXl,
                            end = SpacingXl,
                        ),
            ) {
                Text(
                    text = stringResource(R.string.weekly_training_nav_label),
                    style = typography.titleLarge,
                )

                Spacer(modifier = Modifier.height(SpacingLg))

                WeeklyCalendarHeader(
                    selectedDate = state.selectedDate,
                    weekStartDate = state.weekStartDate,
                    dayIndicators = state.dayIndicators,
                    onDateSelected = viewModel::onDateSelected,
                    onWeekChanged = viewModel::onWeekChanged,
                )

                if (plannerFocusCategories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(SpacingLg))

                    WeeklyPlannerCategoryFilters(
                        categories = plannerFocusCategories,
                        focusedCategoryId = focusedCategoryId,
                        onCategorySelected = { categoryId -> focusedCategoryId = categoryId },
                        onClearFilters = { focusedCategoryId = null },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (state.isWeekLoaded) {
                    state.weeklyHeaderSummary?.let { summary ->
                        Spacer(modifier = Modifier.height(SpacingLg))

                        WeeklyHeaderSummary(
                            summary = summary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(modifier = Modifier.height(SpacingLg))

                    WeeklyTrainingContent(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        selectedDate = state.selectedDate,
                        workouts = state.workouts,
                        focusedCategoryId = focusedCategoryId,
                        requestedWorkoutId = requestedWorkoutIdToOpen,
                        dayOrder = state.dayOrder,
                        slotModePolicy = state.slotModePolicy,
                        onWorkoutMoved = viewModel::moveWorkout,
                        onWorkoutCompletionChanged = viewModel::updateWorkoutCompletion,
                        onWorkoutEdit = { workout ->
                            draftEventDate = null
                            editingWorkout = workout
                        },
                        onWorkoutDelete = { workout -> deletingWorkout = workout },
                        onWeekChanged = viewModel::onWeekChanged,
                    )
                } else {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            WeeklyTrainingAddMenu(
                isVisible = isAddMenuVisible,
                onDismiss = { isAddMenuVisible = false },
                onAddWorkout = {
                    isAddMenuVisible = false
                    draftType = EMPTY
                    draftDescription = EMPTY
                    draftCategoryId = UNCATEGORIZED_ID
                    draftEventDate = null
                    isAddDialogVisible = true
                },
                onAddRaceEvent = {
                    isAddMenuVisible = false
                    draftType = EMPTY
                    draftDescription = EMPTY
                    draftCategoryId = UNCATEGORIZED_ID
                    draftEventDate = state.selectedDate
                    isRaceEventDialogVisible = true
                },
                onAddRest = {
                    isAddMenuVisible = false
                    viewModel.addRest()
                },
                onAddBusy = {
                    isAddMenuVisible = false
                    viewModel.addBusy()
                },
                onAddSick = {
                    isAddMenuVisible = false
                    viewModel.addSick()
                },
                onCopyLastWeek = {
                    isAddMenuVisible = false
                    if (state.isWeekLoaded && state.workouts.isEmpty()) {
                        viewModel.copyLastWeek()
                    } else {
                        isCopyReplaceDialogVisible = true
                    }
                },
                onAddMockWorkout = {
                    isAddMenuVisible = false
                    viewModel.addWorkout(
                        type = mockWorkoutType,
                        description = mockWorkoutDescription,
                        categoryId = UNCATEGORIZED_ID,
                    )
                },
            )
        }
    }

    WeeklyTrainingWorkoutDialog(
        visible = isAddDialogVisible,
        isEdit = false,
        selectedCategoryId = draftCategoryId,
        categories = pickerCategories,
        weekStartDay = state.weekStartDay,
        selectedDate = draftEventDate,
        initialType = draftType,
        initialDescription = draftDescription,
        onDismiss = {
            isAddDialogVisible = false
            draftType = EMPTY
            draftDescription = EMPTY
            draftCategoryId = UNCATEGORIZED_ID
            draftEventDate = null
        },
        onSave = { type, description, categoryId, workoutDate ->
            viewModel.addWorkout(type, description, categoryId, workoutDate)
            isAddDialogVisible = false
            draftType = EMPTY
            draftDescription = EMPTY
            draftCategoryId = UNCATEGORIZED_ID
            draftEventDate = null
        },
        onManageCategories = { draft ->
            isAddDialogVisible = false
            draftType = draft.type
            draftDescription = draft.description
            draftCategoryId = draft.categoryId
            draftEventDate = draft.eventDate
            onManageCategories(draft)
        },
    )

    WeeklyTrainingWorkoutDialog(
        visible = isRaceEventDialogVisible,
        isEdit = false,
        isRaceEvent = true,
        selectedCategoryId = draftCategoryId,
        categories = pickerCategories,
        weekStartDay = state.weekStartDay,
        selectedDate = draftEventDate,
        initialType = draftType,
        initialDescription = draftDescription,
        onDismiss = {
            isRaceEventDialogVisible = false
            draftType = EMPTY
            draftDescription = EMPTY
            draftCategoryId = UNCATEGORIZED_ID
            draftEventDate = null
        },
        onSave = { type, description, categoryId, eventDate ->
            val targetDate = eventDate ?: state.selectedDate
            viewModel.addRaceEvent(type, description, categoryId, targetDate)
            isRaceEventDialogVisible = false
            draftType = EMPTY
            draftDescription = EMPTY
            draftCategoryId = UNCATEGORIZED_ID
            draftEventDate = null
        },
        onManageCategories = { draft ->
            isRaceEventDialogVisible = false
            draftType = draft.type
            draftDescription = draft.description
            draftCategoryId = draft.categoryId
            draftEventDate = draft.eventDate
            onManageCategories(draft)
        },
    )

    editingWorkout?.let { workout ->
        WeeklyTrainingWorkoutDialog(
            visible = true,
            isEdit = true,
            workout = workout,
            categories = state.categories,
            weekStartDay = state.weekStartDay,
            selectedDate = draftEventDate ?: workout.workoutDateOrNull(),
            onDismiss = {
                editingWorkout = null
                draftEventDate = null
            },
            onSave = { type, description, categoryId, workoutDate ->
                if (workout.eventType == RACE_EVENT) {
                    val targetDate = workoutDate ?: draftEventDate ?: state.selectedDate
                    viewModel.updateRaceEvent(
                        workoutId = workout.id,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                        eventDate = targetDate,
                    )
                } else {
                    viewModel.updateWorkoutDetails(
                        workoutId = workout.id,
                        type = type,
                        description = description,
                        eventType = workout.eventType,
                        categoryId = categoryId,
                        workoutDate = workoutDate,
                    )
                }
                editingWorkout = null
                draftEventDate = null
            },
            onManageCategories = { draft ->
                editingWorkout = null
                draftEventDate = draft.eventDate
                onManageCategories(
                    draft.copy(
                        workoutId = workout.id,
                        isRaceEvent = workout.eventType == RACE_EVENT,
                    ),
                )
            },
        )
    }

    deletingWorkout?.let { workout ->
        WeeklyTrainingDeleteDialog(
            workout = workout,
            onDismiss = { deletingWorkout = null },
            onConfirm = {
                viewModel.deleteWorkout(workout.id)
                deletingWorkout = null
            },
        )
    }

    if (isCopyReplaceDialogVisible) {
        WeeklyTrainingCopyReplaceDialog(
            onDismiss = { isCopyReplaceDialogVisible = false },
            onConfirm = {
                viewModel.copyLastWeek()
                isCopyReplaceDialogVisible = false
            },
        )
    }
}
