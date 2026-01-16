package com.rafaelfelipeac.hermes.features.trainingweek.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WorkoutUi

@Composable
fun TrainingWeekScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingWeekViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var editingWorkout by rememberSaveable { mutableStateOf<WorkoutUi?>(null) }
    var deletingWorkout by rememberSaveable { mutableStateOf<WorkoutUi?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        WeeklyCalendarHeader(
            selectedDate = state.selectedDate,
            weekStartDate = state.weekStartDate,
            dayIndicators = state.dayIndicators,
            onDateSelected = viewModel::onDateSelected,
            onWeekChanged = viewModel::onWeekChanged
        )

        WeeklyTrainingContent(
            selectedWeekStartDate = state.weekStartDate,
            workouts = state.workouts,
            onAddWorkout = { isAddDialogVisible = true },
            onAddRestDay = viewModel::addRestDay,
            onWorkoutMoved = viewModel::moveWorkout,
            onWorkoutCompletionChanged = viewModel::updateWorkoutCompletion,
            onWorkoutEdit = { workout -> editingWorkout = workout },
            onWorkoutDelete = { workout -> deletingWorkout = workout }
        )
    }

    if (isAddDialogVisible) {
        AddWorkoutDialog(
            onDismiss = { isAddDialogVisible = false },
            onSave = { type, description ->
                viewModel.addWorkout(type, description)

                isAddDialogVisible = false
            },
            isEdit = false
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
                    isRestDay = workout.isRestDay
                )
                editingWorkout = null
            },
            isEdit = true,
            initialType = workout.type,
            initialDescription = workout.description
        )
    }

    if (deletingWorkout != null) {
        val workout = deletingWorkout!!
        val title = if (workout.isRestDay) {
            stringResource(R.string.delete_rest_day_title)
        } else {
            stringResource(R.string.delete_workout_title)
        }
        val message = if (workout.isRestDay) {
            stringResource(R.string.delete_rest_day_message)
        } else {
            stringResource(R.string.delete_workout_message)
        }
        val confirmLabel = if (workout.isRestDay) {
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
                    }
                ) {
                    Text(text = confirmLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingWorkout = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            }
        )
    }
}
