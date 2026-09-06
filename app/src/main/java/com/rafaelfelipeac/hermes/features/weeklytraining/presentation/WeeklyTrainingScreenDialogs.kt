package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.AddRaceEventDialog
import com.rafaelfelipeac.hermes.core.ui.components.AddWorkoutDialog
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.BUSY
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.REST
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.SICK
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.LocalDate

@Composable
internal fun WeeklyTrainingWorkoutDialog(
    visible: Boolean,
    isEdit: Boolean,
    workout: WorkoutUi? = null,
    isRaceEvent: Boolean = false,
    initialType: String = EMPTY,
    initialDescription: String = EMPTY,
    selectedCategoryId: Long? = UNCATEGORIZED_ID,
    categories: List<CategoryUi>,
    weekStartDay: WeekStartDay,
    selectedDate: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?, LocalDate?) -> Unit,
    onManageCategories: (WorkoutDialogDraft) -> Unit,
) {
    if (!visible) return

    val actualWorkout = workout
    val renderRaceEvent = actualWorkout?.eventType == RACE_EVENT || isRaceEvent
    val dialogCategories =
        categories
            .filter {
                !it.isHidden || it.id == UNCATEGORIZED_ID || it.id == actualWorkout?.categoryId
            }
            .sortedBy { it.sortOrder }

    if (renderRaceEvent) {
        AddRaceEventDialog(
            onDismiss = onDismiss,
            onSave = onSave,
            onManageCategories = { type, description, categoryId, eventDate ->
                onManageCategories(
                    WorkoutDialogDraft(
                        workoutId = actualWorkout?.id,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                        eventDate = eventDate,
                        isRaceEvent = true,
                    ),
                )
            },
            isEdit = isEdit,
            categories = dialogCategories,
            selectedCategoryId = actualWorkout?.categoryId ?: selectedCategoryId,
            weekStartDay = weekStartDay,
            selectedDate = selectedDate,
            initialTitle = actualWorkout?.type ?: initialType,
            initialDescription = actualWorkout?.description ?: initialDescription,
        )
    } else {
        AddWorkoutDialog(
            onDismiss = onDismiss,
            onSave = onSave,
            onManageCategories = { type, description, categoryId, workoutDate ->
                onManageCategories(
                    WorkoutDialogDraft(
                        workoutId = actualWorkout?.id,
                        type = type,
                        description = description,
                        categoryId = categoryId,
                        eventDate = workoutDate,
                    ),
                )
            },
            isEdit = isEdit,
            categories = dialogCategories,
            selectedCategoryId = actualWorkout?.categoryId ?: selectedCategoryId,
            weekStartDay = weekStartDay,
            selectedDate = selectedDate,
            initialType = actualWorkout?.type ?: initialType,
            initialDescription = actualWorkout?.description ?: initialDescription,
        )
    }
}

@Composable
internal fun WeeklyTrainingDeleteDialog(
    workout: WorkoutUi,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
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
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )
}

@Composable
internal fun WeeklyTrainingCopyReplaceDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_title))
        },
        text = {
            Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.weekly_training_copy_last_week_replace_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )
}
