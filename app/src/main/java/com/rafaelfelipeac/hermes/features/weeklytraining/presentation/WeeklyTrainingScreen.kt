package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.rafaelfelipeac.hermes.core.ui.components.AddRaceEventDialog
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddMenuBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.LocalDate

private const val ADD_MENU_SCRIM_ALPHA = 0.30f
private const val ADD_FAB_TEST_TAG = "add-fab"
private const val WEEKLY_FILTER_ALL_CHIP_KEY = "weekly-filter-all-chip"

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
    var draftEventDate by remember { mutableStateOf<LocalDate?>(state.selectedDate) }
    var isRaceEventDialogVisible by rememberSaveable { mutableStateOf(false) }
    var focusedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var draftConsumedLocally by remember { mutableStateOf(false) }
    val fabContainerColor = colorScheme.primaryContainer
    val fabContentColor = colorScheme.onPrimaryContainer
    val undoLabel = stringResource(R.string.weekly_training_undo_action)
    val emptyCopyMessage = stringResource(R.string.weekly_training_copy_last_week_empty)
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
                Snackbar(
                    snackbarData = data,
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    actionColor = colorScheme.primary,
                )
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
                        icon = Icons.Outlined.Flag,
                        label = stringResource(R.string.weekly_training_add_race_event),
                        onClick = {
                            isAddMenuVisible = false
                            draftType = EMPTY
                            draftDescription = EMPTY
                            draftCategoryId = UNCATEGORIZED_ID
                            draftEventDate = state.selectedDate
                            isRaceEventDialogVisible = true
                        },
                    )

                    AddActionPill(
                        icon = Icons.Outlined.Bedtime,
                        label = stringResource(R.string.weekly_training_add_rest_day),
                        onClick = {
                            isAddMenuVisible = false
                            viewModel.addRest()
                        },
                    )

                    AddActionPill(
                        icon = Icons.Outlined.EventBusy,
                        label = stringResource(R.string.weekly_training_add_busy),
                        onClick = {
                            isAddMenuVisible = false
                            viewModel.addBusy()
                        },
                    )

                    AddActionPill(
                        icon = Icons.Outlined.MedicalServices,
                        label = stringResource(R.string.weekly_training_add_sick),
                        onClick = {
                            isAddMenuVisible = false
                            viewModel.addSick()
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

    if (isRaceEventDialogVisible) {
        AddRaceEventDialog(
            onDismiss = {
                isRaceEventDialogVisible = false
                draftType = EMPTY
                draftDescription = EMPTY
                draftCategoryId = UNCATEGORIZED_ID
                draftEventDate = state.selectedDate
            },
            onSave = { type, description, categoryId, eventDate ->
                viewModel.addRaceEvent(type, description, categoryId, eventDate)
                isRaceEventDialogVisible = false
                draftType = EMPTY
                draftDescription = EMPTY
                draftCategoryId = UNCATEGORIZED_ID
                draftEventDate = state.selectedDate
            },
            onManageCategories = { type, description, categoryId, eventDate ->
                isRaceEventDialogVisible = false
                draftType = type
                draftDescription = description
                draftCategoryId = categoryId
                draftEventDate = eventDate
                onManageCategories(
                    WorkoutDialogDraft(
                        workoutId = null,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                        isRaceEvent = true,
                    ),
                )
            },
            isEdit = false,
            categories = pickerCategories,
            selectedCategoryId = draftCategoryId,
            selectedDate = draftEventDate,
            initialTitle = draftType,
            initialDescription = draftDescription,
        )
    }

    editingWorkout?.let { workout ->
        val editCategories =
            state.categories
                .filter { !it.isHidden || it.id == UNCATEGORIZED_ID || it.id == workout.categoryId }
                .sortedBy { it.sortOrder }

        if (workout.eventType == RACE_EVENT) {
            AddRaceEventDialog(
                onDismiss = {
                    editingWorkout = null
                    draftEventDate = null
                },
                onSave = { type, description, categoryId, eventDate ->
                    viewModel.updateRaceEvent(
                        workoutId = workout.id,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                    )
                    editingWorkout = null
                    draftEventDate = null
                },
                onManageCategories = { type, description, categoryId, eventDate ->
                    editingWorkout = null
                    onManageCategories(
                        WorkoutDialogDraft(
                            workoutId = workout.id,
                            type = type,
                            description = description,
                            categoryId = categoryId,
                            eventDate = eventDate,
                            isRaceEvent = true,
                        ),
                    )
                },
                isEdit = true,
                categories = editCategories,
                selectedCategoryId = workout.categoryId,
                selectedDate =
                    draftEventDate
                        ?: workout.weekStartDate.plusDays((workout.dayOfWeek?.value?.minus(1) ?: 0).toLong()),
                initialTitle = workout.type,
                initialDescription = workout.description,
            )
        } else {
            AddWorkoutDialog(
                onDismiss = { editingWorkout = null },
                onSave = { type, description, categoryId ->
                    viewModel.updateWorkoutDetails(
                        workoutId = workout.id,
                        type = type,
                        description = description,
                        eventType = workout.eventType,
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
    }

    deletingWorkout?.let { workout ->
        val title =
            when (workout.eventType) {
                WORKOUT -> stringResource(R.string.weekly_training_delete_workout_title)
                REST -> stringResource(R.string.weekly_training_delete_rest_day_title)
                BUSY -> stringResource(R.string.weekly_training_delete_busy_title)
                SICK -> stringResource(R.string.weekly_training_delete_sick_title)
                RACE_EVENT -> stringResource(R.string.weekly_training_delete_race_event_title)
            }
        val message =
            when (workout.eventType) {
                WORKOUT -> stringResource(R.string.weekly_training_delete_workout_message)
                REST -> stringResource(R.string.weekly_training_delete_rest_day_message)
                BUSY -> stringResource(R.string.weekly_training_delete_busy_message)
                SICK -> stringResource(R.string.weekly_training_delete_sick_message)
                RACE_EVENT -> stringResource(R.string.weekly_training_delete_race_event_message)
            }
        val confirmLabel =
            when (workout.eventType) {
                WORKOUT -> stringResource(R.string.weekly_training_delete_workout)
                REST -> stringResource(R.string.weekly_training_delete_rest_day)
                BUSY -> stringResource(R.string.weekly_training_delete_busy)
                SICK -> stringResource(R.string.weekly_training_delete_sick)
                RACE_EVENT -> stringResource(R.string.weekly_training_delete_race_event)
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
private fun WeeklyPlannerCategoryFilters(
    categories: List<com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi>,
    focusedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val clearFiltersLabel = stringResource(R.string.filters_clear)
    val selectedIndex =
        if (focusedCategoryId == null) {
            0
        } else {
            categories.indexOfFirst { it.id == focusedCategoryId } + 1
        }

    LaunchedEffect(selectedIndex, categories, focusedCategoryId) {
        centerWeeklySelectedChip(listState, selectedIndex)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SpacingSm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item(key = WEEKLY_FILTER_ALL_CHIP_KEY) {
                FilterChip(
                    selected = focusedCategoryId == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text(text = stringResource(R.string.activity_filter_all)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.primaryContainer,
                            selectedLabelColor = colorScheme.onPrimaryContainer,
                        ),
                )
            }

            itemsIndexed(categories, key = { _, item -> item.id }) { _, category ->
                FilterChip(
                    selected = focusedCategoryId == category.id,
                    onClick = { onCategorySelected(category.id) },
                    label = { Text(text = category.name) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = colorScheme.secondaryContainer,
                            selectedLabelColor = colorScheme.onSecondaryContainer,
                        ),
                )
            }
        }

        if (focusedCategoryId != null) {
            FilterChip(
                selected = false,
                onClick = onClearFilters,
                label = {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = clearFiltersLabel,
                    )
                },
            )
        }
    }
}

private suspend fun centerWeeklySelectedChip(
    listState: LazyListState,
    selectedIndex: Int,
) {
    if (selectedIndex < 0) return

    var itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex }

    if (itemInfo == null) {
        listState.animateScrollToItem(selectedIndex)
        itemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == selectedIndex } ?: return
    }

    val viewportCenter =
        (listState.layoutInfo.viewportStartOffset + listState.layoutInfo.viewportEndOffset) / 2
    val itemCenter = itemInfo.offset + itemInfo.size / 2
    val delta = (itemCenter - viewportCenter).toFloat()

    if (delta != 0f) {
        listState.animateScrollBy(delta)
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
