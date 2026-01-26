package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens
import com.rafaelfelipeac.hermes.core.ui.theme.RestDayContentDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDayContentLight
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TextSizes
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlueContent
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

private const val WORKOUT_ROW_DRAGGING_ALPHA = 0f
private const val WORKOUT_ROW_CONTENT_ALPHA = 1f
private const val WORKOUT_BORDER_ALPHA = 0.6f
private const val GHOST_ROW_ALPHA = 0.45f
private const val TYPE_CHIP_ALPHA = 0.18f

@Composable
internal fun WorkoutRow(
    workout: WorkoutUi,
    isDragging: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    onDragStarted: (Offset, Float) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onItemPositioned: (Rect) -> Unit,
) {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val colors = workoutRowColors(workout, isDragging = isDragging)
    val hasDescription = workout.description.isNotBlank()
    val rowModifier =
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                coordinates = it

                if (!isDragging) {
                    onItemPositioned(it.boundsInRoot())
                }
            }
            .clip(MaterialTheme.shapes.medium)
            .background(if (isDragging) Color.Transparent else colors.background)
            .then(
                if (!isDragging && !workout.isRestDay) {
                    Modifier.border(
                        width = Dimens.BorderThin,
                        color = colors.background.copy(alpha = WORKOUT_BORDER_ALPHA),
                        shape = MaterialTheme.shapes.medium,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (isDragging) {
                    Modifier
                        .height(Dimens.Zero)
                        .padding(Dimens.Zero)
                        .alpha(WORKOUT_ROW_DRAGGING_ALPHA)
                } else {
                    Modifier
                        .padding(Dimens.ContentPadding)
                        .alpha(WORKOUT_ROW_CONTENT_ALPHA)
                },
            )

    Box(modifier = rowModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = Dimens.SpacingXl)
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            coordinates?.localToRoot(offset)?.let {
                                onDragStarted(it, itemBoundsHeight(coordinates))
                            }
                        },
                        onDrag = { _, _ -> },
                    )
                },
            verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            if (!workout.isRestDay) {
                Box(
                    modifier =
                        Modifier
                            .size(Dimens.CheckboxBoxSize)
                            .offset(y = Dimens.CheckboxYOffset),
                    contentAlignment = Alignment.Center,
                ) {
                    if (workout.isCompleted) {
                        Text(
                            text = stringResource(R.string.workout_completed_emoji),
                            modifier =
                                Modifier
                                    .clickable { onToggleCompleted(false) }
                                    .size(Dimens.CheckboxSize),
                            fontSize = TextSizes.CompletedEmojiFontSize,
                        )
                    } else {
                        Checkbox(
                            checked = false,
                            onCheckedChange = onToggleCompleted,
                            modifier = Modifier.size(Dimens.CheckboxSize),
                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor = colors.content,
                                    uncheckedColor = colors.content,
                                    checkmarkColor = colors.background,
                                ),
                        )
                    }
                }
            }

            if (!workout.isRestDay) {
                Spacer(modifier = Modifier.width(Dimens.SpacingLg))
            }

            Row(
                verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(enabled = !workout.isRestDay) { onEdit() },
            ) {
                Column {
                    if (workout.isRestDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = colors.content,
                                modifier = Modifier.size(Dimens.SmallIconSize),
                            )

                            Spacer(modifier = Modifier.width(Dimens.SpacingSm))

                            Text(
                                text = stringResource(R.string.rest_day_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.content,
                            )
                        }
                    } else {
                        TypeChip(
                            label = workout.type,
                            containerColor = colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                            contentColor = colors.content,
                        )
                    }

                    if (hasDescription) {
                        Spacer(modifier = Modifier.height(Dimens.SpacingXs))

                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.content,
                        )
                    }
                }
            }
        }

        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.delete_workout),
            tint = colors.content,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = Dimens.SpacingXs, y = Dimens.Zero)
                    .size(Dimens.CloseIconSize)
                    .clickable { onDelete() },
        )
    }
}

@Composable
private fun TypeChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier =
                Modifier.padding(
                    horizontal = Dimens.SpacingMd,
                    vertical = Dimens.SpacingXs,
                ),
        )
    }
}

@Composable
internal fun GhostWorkoutRow(
    modifier: Modifier = Modifier,
    workout: WorkoutUi,
) {
    val colors = workoutRowColors(workout, isDragging = false)
    val hasDescription = workout.description.isNotBlank()

    Surface(
        color = colors.background,
        contentColor = colors.content,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (!workout.isRestDay) {
                        Modifier.border(
                            width = Dimens.BorderThin,
                            color = colors.background.copy(alpha = WORKOUT_BORDER_ALPHA),
                            shape = MaterialTheme.shapes.medium,
                        )
                    } else {
                        Modifier
                    },
                )
                .alpha(GHOST_ROW_ALPHA),
    ) {
        Row(
            modifier = Modifier.padding(Dimens.ContentPadding),
            verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Column {
                    if (workout.isRestDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = colors.content,
                                modifier = Modifier.size(Dimens.SmallIconSize),
                            )

                            Spacer(modifier = Modifier.width(Dimens.SpacingSm))

                            Text(
                                text = stringResource(R.string.rest_day_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.content,
                            )
                        }
                    } else {
                        TypeChip(
                            label = workout.type,
                            containerColor = colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                            contentColor = colors.content,
                        )
                    }

                    if (hasDescription) {
                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.content,
                        )
                    }
                }
            }
        }
    }
}

private data class RowColors(
    val background: Color,
    val content: Color,
)

@Composable
private fun workoutRowColors(
    workout: WorkoutUi,
    isDragging: Boolean,
): RowColors {
    val colorScheme = MaterialTheme.colorScheme
    val todoColor = CompletedBlue
    val todoContent = CompletedBlueContent
    val completedColor = TodoBlue
    val completedContent = TodoBlueContent
    val isUnscheduled = workout.dayOfWeek == null
    val isDarkTheme = isSystemInDarkTheme()
    val restDayBackground = if (isDarkTheme) RestDaySurfaceDark else RestDaySurfaceLight
    val restDayContent = if (isDarkTheme) RestDayContentDark else RestDayContentLight

    val background = when {
        isDragging -> colorScheme.surfaceVariant
        workout.isCompleted -> completedColor
        workout.isRestDay -> restDayBackground
        isUnscheduled -> todoColor
        else -> todoColor
    }
    val content =
        when {
            workout.isRestDay -> restDayContent
            workout.isCompleted -> completedContent
            isUnscheduled -> todoContent
            else -> todoContent
        }

    return RowColors(background, content)
}

private fun itemBoundsHeight(coordinates: LayoutCoordinates?): Float {
    return coordinates?.boundsInRoot()?.height ?: 0f
}
