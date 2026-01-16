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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog

@Composable
fun TrainingWeekScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingWeekViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }

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
            onWorkoutCompletionChanged = viewModel::updateWorkoutCompletion
        )
    }

    if (isAddDialogVisible) {
        AddWorkoutDialog(
            onDismiss = { isAddDialogVisible = false },
            onSave = { type, description ->
                viewModel.addWorkout(type, description)

                isAddDialogVisible = false
            }
        )
    }
}
