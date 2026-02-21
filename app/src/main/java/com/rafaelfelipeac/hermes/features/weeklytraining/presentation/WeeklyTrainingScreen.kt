package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.BuildConfig
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddMenuBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

private const val ADD_MENU_SCRIM_ALPHA = 0.30f
private const val ADD_FAB_TEST_TAG = "add-fab"

@Composable
fun WeeklyTrainingScreen(
    modifier: Modifier = Modifier,
    onManageCategories: (WorkoutDialogDraft) -> Unit = {},
    pendingWorkoutDraft: WorkoutDialogDraft? = null,
    onWorkoutDraftConsumed: () -> Unit = {},
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
    var draftConsumedLocally by remember { mutableStateOf(false) }
    val fabContainerColor = colorScheme.primaryContainer
    val fabContentColor = colorScheme.onPrimaryContainer
    val undoLabel = stringResource(R.string.weekly_training_undo_action)
    val copiedWeekMessage = stringResource(R.string.weekly_training_week_copied)
    val emptyCopyMessage = stringResource(R.string.weekly_training_copy_last_week_empty)
    val pickerCategories = state.categories.filter { !it.isHidden || it.id == UNCATEGORIZED_ID }
    val undoMessage =
        undoState?.let { currentUndo ->
            val isRestDay =
                when (val action = currentUndo.action) {
                    is PendingUndoAction.Delete -> action.workout.isRestDay
                    is PendingUndoAction.Completion -> action.workout.isRestDay
                    is PendingUndoAction.MoveOrReorder -> action.isRestDay
                    is PendingUndoAction.ReplaceWeek -> false
                }

            when (currentUndo.message) {
                UndoMessage.WeekCopied -> copiedWeekMessage
                UndoMessage.Moved ->
                    stringResource(
                        if (isRestDay) {
                            R.string.weekly_training_rest_day_moved
                        } else {
                            R.string.weekly_training_workout_moved
                        },
                    )
                UndoMessage.Deleted ->
                    stringResource(
                        if (isRestDay) {
                            R.string.weekly_training_rest_day_deleted
                        } else {
                            R.string.weekly_training_workout_deleted
                        },
                    )
                UndoMessage.Completed ->
                    stringResource(R.string.weekly_training_workout_completed)
                UndoMessage.MarkedIncomplete ->
                    stringResource(R.string.weekly_training_workout_marked_incomplete)
            }
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
            isAddDialogVisible = true
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
                draftConsumedLocally = true
                onWorkoutDraftConsumed()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    actionColor = colorScheme.primary,
                )
            }
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val contentPadding =
            PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = paddingValues.calculateTopPadding(),
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
                        .padding(SpacingXl),
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

                if (state.isWeekLoaded) {
                    WeeklyTrainingContent(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        selectedDate = state.selectedDate,
                        workouts = state.workouts,
                        onWorkoutMoved = viewModel::moveWorkout,
                        onWorkoutCompletionChanged = viewModel::updateWorkoutCompletion,
                        onWorkoutEdit = { workout -> editingWorkout = workout },
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

            if (isAddMenuVisible) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                colorScheme.scrim.copy(
                                    alpha = ADD_MENU_SCRIM_ALPHA,
                                ),
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                isAddMenuVisible = false
                            },
                )
            }

            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(SpacingXl),
            ) {
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
            }

            if (isAddMenuVisible) {
                Column(
                    modifier =
                        Modifier
                            .width(IntrinsicSize.Max)
                            .align(Alignment.BottomEnd)
                            .padding(end = SpacingXl, bottom = AddMenuBottomPadding),
                    verticalArrangement = Arrangement.spacedBy(SpacingLg),
                    horizontalAlignment = Alignment.End,
                ) {
                    AddActionPill(
                        icon = Icons.Default.Add,
                        label = stringResource(R.string.add_workout),
                        onClick = {
                            isAddMenuVisible = false
                            draftType = EMPTY
                            draftDescription = EMPTY
                            draftCategoryId = UNCATEGORIZED_ID
                            isAddDialogVisible = true
                        },
                    )

                    AddActionPill(
                        icon = Icons.Outlined.Bedtime,
                        label = stringResource(R.string.weekly_training_add_rest_day),
                        onClick = {
                            isAddMenuVisible = false
                            viewModel.addRestDay()
                        },
                    )

                    AddActionPill(
                        icon = Icons.Default.History,
                        label = stringResource(R.string.weekly_training_copy_last_week),
                        onClick = {
                            isAddMenuVisible = false

                            if (state.isWeekLoaded && state.workouts.isEmpty()) {
                                viewModel.copyLastWeek()
                            } else {
                                isCopyReplaceDialogVisible = true
                            }
                        },
                    )

                    if (BuildConfig.DEBUG) {
                        val mockType = stringResource(R.string.mock_workout_type)
                        val mockDescription = stringResource(R.string.mock_workout_description)

                        AddActionPill(
                            icon = Icons.Default.Settings,
                            label = stringResource(R.string.weekly_training_add_mock_workout),
                            onClick = {
                                isAddMenuVisible = false
                                viewModel.addWorkout(
                                    type = mockType,
                                    description = mockDescription,
                                    categoryId = UNCATEGORIZED_ID,
                                )
                            },
                        )
                    }
                }
            }
        }
    }

    if (isAddDialogVisible) {
        AddWorkoutDialog(
            onDismiss = {
                isAddDialogVisible = false
                draftType = EMPTY
                draftDescription = EMPTY
                draftCategoryId = UNCATEGORIZED_ID
            },
            onSave = { type, description, categoryId ->
                viewModel.addWorkout(type, description, categoryId)
                isAddDialogVisible = false
                draftType = EMPTY
                draftDescription = EMPTY
                draftCategoryId = UNCATEGORIZED_ID
            },
            onManageCategories = { type, description, categoryId ->
                isAddDialogVisible = false
                onManageCategories(
                    WorkoutDialogDraft(
                        workoutId = null,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                    ),
                )
            },
            isEdit = false,
            categories = pickerCategories,
            selectedCategoryId = draftCategoryId,
            initialType = draftType,
            initialDescription = draftDescription,
        )
    }

    editingWorkout?.let { workout ->
        val editCategories =
            state.categories
                .filter { !it.isHidden || it.id == UNCATEGORIZED_ID || it.id == workout.categoryId }
                .sortedBy { it.sortOrder }

        AddWorkoutDialog(
            onDismiss = { editingWorkout = null },
            onSave = { type, description, categoryId ->
                viewModel.updateWorkoutDetails(
                    workoutId = workout.id,
                    type = type,
                    description = description,
                    isRestDay = workout.isRestDay,
                    categoryId = categoryId,
                )
                editingWorkout = null
            },
            onManageCategories = { type, description, categoryId ->
                editingWorkout = null
                onManageCategories(
                    WorkoutDialogDraft(
                        workoutId = workout.id,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                    ),
                )
            },
            isEdit = true,
            categories = editCategories,
            selectedCategoryId = workout.categoryId,
            initialType = workout.type,
            initialDescription = workout.description,
        )
    }

    deletingWorkout?.let { workout ->
        val title =
            if (workout.isRestDay) {
                stringResource(R.string.weekly_training_delete_rest_day_title)
            } else {
                stringResource(R.string.weekly_training_delete_workout_title)
            }
        val message =
            if (workout.isRestDay) {
                stringResource(R.string.weekly_training_delete_rest_day_message)
            } else {
                stringResource(R.string.weekly_training_delete_workout_message)
            }
        val confirmLabel =
            if (workout.isRestDay) {
                stringResource(R.string.weekly_training_delete_rest_day)
            } else {
                stringResource(R.string.weekly_training_delete_workout)
            }

        AlertDialog(
            onDismissRequest = { deletingWorkout = null },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWorkout(workout.id)
                        deletingWorkout = null
                    },
                ) {
                    Text(text = confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingWorkout = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }

    if (isCopyReplaceDialogVisible) {
        AlertDialog(
            onDismissRequest = { isCopyReplaceDialogVisible = false },
            title = {
                Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_title))
            },
            text = {
                Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.copyLastWeek()

                        isCopyReplaceDialogVisible = false
                    },
                ) {
                    Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isCopyReplaceDialogVisible = false }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }
}

@Composable
private fun AddActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = shapes.extraLarge,
        tonalElevation = ElevationMd,
        shadowElevation = ElevationMd,
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = AddActionPillMinWidth),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AddActionPillHorizontalPadding,
                    vertical = SpacingLg,
                ),
            horizontalArrangement = Arrangement.spacedBy(SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = typography.titleSmall)
        }
    }
}
