package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WorkoutUi
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens
import com.rafaelfelipeac.hermes.core.ui.theme.FabContainerDark
import com.rafaelfelipeac.hermes.core.ui.theme.FabContainerLight
import com.rafaelfelipeac.hermes.core.ui.theme.FabContentDark
import com.rafaelfelipeac.hermes.core.ui.theme.FabContentLight

private val AddMenuScrimAlpha = 0.30f
private const val ADD_FAB_TEST_TAG = "add-fab"

@Composable
fun TrainingWeekScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingWeekViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isAddMenuVisible by rememberSaveable { mutableStateOf(false) }
    var editingWorkout by rememberSaveable { mutableStateOf<WorkoutUi?>(null) }
    var deletingWorkout by rememberSaveable { mutableStateOf<WorkoutUi?>(null) }
    val isDarkTheme = isSystemInDarkTheme()
    val fabContainerColor = if (isDarkTheme) FabContainerDark else FabContainerLight
    val fabContentColor = if (isDarkTheme) FabContentDark else FabContentLight

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(Dimens.SpacingXl),
        ) {
            Text(
                text = stringResource(R.string.nav_training_week),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingLg))
            WeeklyCalendarHeader(
                selectedDate = state.selectedDate,
                weekStartDate = state.weekStartDate,
                dayIndicators = state.dayIndicators,
                onDateSelected = viewModel::onDateSelected,
                onWeekChanged = viewModel::onWeekChanged,
            )

            WeeklyTrainingContent(
                selectedDate = state.selectedDate,
                workouts = state.workouts,
                onWorkoutMoved = viewModel::moveWorkout,
                onWorkoutCompletionChanged = viewModel::updateWorkoutCompletion,
                onWorkoutEdit = { workout -> editingWorkout = workout },
                onWorkoutDelete = { workout -> deletingWorkout = workout },
                onWeekChanged = viewModel::onWeekChanged,
            )
        }

        if (isAddMenuVisible) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.scrim.copy(
                                alpha = AddMenuScrimAlpha,
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
                        .padding(Dimens.SpacingXl),
        ) {
            FloatingActionButton(
                onClick = { isAddMenuVisible = !isAddMenuVisible },
                containerColor = fabContainerColor,
                contentColor = fabContentColor,
                modifier = Modifier.testTag(ADD_FAB_TEST_TAG),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_item),
                )
            }
        }

        if (isAddMenuVisible) {
            Column(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = Dimens.SpacingXl, bottom = Dimens.AddMenuBottomPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
                horizontalAlignment = Alignment.End,
            ) {
                AddActionPill(
                    label = stringResource(R.string.add_workout),
                    onClick = {
                        isAddMenuVisible = false
                        isAddDialogVisible = true
                    },
                )
                AddActionPill(
                    label = stringResource(R.string.add_rest_day),
                    onClick = {
                        isAddMenuVisible = false
                        viewModel.addRestDay()
                    },
                )
            }
        }
    }

    if (isAddDialogVisible) {
        AddWorkoutDialog(
            onDismiss = { isAddDialogVisible = false },
            onSave = { type, description ->
                viewModel.addWorkout(type, description)

                isAddDialogVisible = false
            },
            isEdit = false,
        )
    }

    if (editingWorkout != null) {
        val workout = editingWorkout!!
        AddWorkoutDialog(
            onDismiss = { editingWorkout = null },
            onSave = { type, description ->
                viewModel.updateWorkoutDetails(
                    workoutId = workout.id,
                    type = type,
                    description = description,
                    isRestDay = workout.isRestDay,
                )
                editingWorkout = null
            },
            isEdit = true,
            initialType = workout.type,
            initialDescription = workout.description,
        )
    }

    if (deletingWorkout != null) {
        val workout = deletingWorkout!!
        val title =
            if (workout.isRestDay) {
                stringResource(R.string.delete_rest_day_title)
            } else {
                stringResource(R.string.delete_workout_title)
            }
        val message =
            if (workout.isRestDay) {
                stringResource(R.string.delete_rest_day_message)
            } else {
                stringResource(R.string.delete_workout_message)
            }
        val confirmLabel =
            if (workout.isRestDay) {
                stringResource(R.string.delete_rest_day)
            } else {
                stringResource(R.string.delete_workout)
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
}

@Composable
private fun AddActionPill(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge,
        tonalElevation = Dimens.ElevationMd,
        shadowElevation = Dimens.ElevationMd,
        modifier = Modifier.defaultMinSize(minWidth = Dimens.AddActionPillMinWidth),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = Dimens.AddActionPillHorizontalPadding,
                    vertical = Dimens.SpacingLg,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = androidx.compose.material3.MaterialTheme.typography.titleSmall)
        }
    }
}
