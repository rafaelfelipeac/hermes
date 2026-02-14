package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.calendar.baseCategoryColor
import com.rafaelfelipeac.hermes.core.ui.components.calendar.completedCategoryColor
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CheckboxBoxSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CheckboxSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CheckboxYOffset
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CloseIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ContentPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SmallIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

private const val WORKOUT_ROW_DRAGGING_ALPHA = 0f
private const val WORKOUT_ROW_CONTENT_ALPHA = 1f
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
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val categoryAccent =
        workout.categoryColorId?.let { accent ->
            baseCategoryColor(
                accent = categoryAccentColor(accent),
                isDarkTheme = isDarkTheme,
                surface = colorScheme.surface,
            )
        }
    val categoryChipBase =
        categoryAccent?.let { accent ->
            if (workout.isCompleted) {
                completedCategoryColor(
                    accent = accent,
                    isDarkTheme = isDarkTheme,
                    surface = colorScheme.surface,
                )
            } else {
                accent
            }
        }
    val categoryChipBackground =
        categoryChipBase?.let { base ->
            lighterTone(base, isDarkTheme = isDarkTheme)
        }
    val categoryChipContent = Color.White
    val rowModifier =
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                coordinates = it

                if (!isDragging) {
                    onItemPositioned(it.boundsInRoot())
                }
            }
            .clip(shapes.medium)
            .background(if (isDragging) Color.Transparent else colors.background)
            .then(Modifier)
            .then(
                if (isDragging) {
                    Modifier
                        .height(Zero)
                        .padding(Zero)
                        .alpha(WORKOUT_ROW_DRAGGING_ALPHA)
                } else {
                    Modifier
                        .padding(ContentPadding)
                        .alpha(WORKOUT_ROW_CONTENT_ALPHA)
                },
            )

    Box(modifier = rowModifier) {
        if (!workout.isRestDay && categoryAccent != null) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .width(SpacingXs)
                        .background(categoryAccent),
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = SpacingXl)
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
                            .size(CheckboxBoxSize + SpacingSm)
                            .offset(y = CheckboxYOffset),
                    contentAlignment = Alignment.Center,
                ) {
                    if (workout.isCompleted) {
                        val completedButtonColor =
                            categoryChipBackground ?: colors.content.copy(alpha = TYPE_CHIP_ALPHA)
                        val completedButtonContent = Color.White
                        val completedIconSize = CheckboxSize - SpacingXs

                        Box(
                            modifier =
                                Modifier
                                    .size(CheckboxSize + SpacingSm)
                                    .clip(CircleShape)
                                    .background(completedButtonColor)
                                    .clickable { onToggleCompleted(false) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.workout_completed),
                                tint = completedButtonContent,
                                modifier = Modifier.size(completedIconSize),
                            )
                        }
                    } else {
                        Checkbox(
                            checked = false,
                            onCheckedChange = onToggleCompleted,
                            modifier = Modifier.size(CheckboxSize + SpacingSm),
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
                Spacer(modifier = Modifier.width(SpacingLg))
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
                                modifier = Modifier.size(SmallIconSize),
                            )

                            Spacer(modifier = Modifier.width(SpacingSm))

                            Text(
                                text = stringResource(R.string.rest_day_label),
                                style = typography.titleSmall,
                                color = colors.content,
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                            workout.categoryName?.let { categoryName ->
                                TitleChip(
                                    label = categoryName,
                                    containerColor =
                                        categoryChipBackground ?: colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                                    contentColor = categoryChipContent,
                                )
                            }

                            Column(modifier = Modifier.padding(start = SpacingXs)) {
                                Text(
                                    text = workout.type,
                                    style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.content,
                                )
                            }
                        }
                    }

                    if (hasDescription) {
                        Spacer(modifier = Modifier.height(SpacingXs))

                        Text(
                            text = workout.description,
                            style = typography.bodySmall,
                            color = colors.content,
                            modifier = Modifier.padding(start = SpacingXs),
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
                    .offset(x = SpacingXs, y = Zero)
                    .size(CloseIconSize)
                    .clickable { onDelete() },
        )
    }
}

@Composable
private fun TitleChip(
    label: String,
    containerColor: Color,
    contentColor: Color,
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = shapes.small,
    ) {
        Text(
            text = label,
            style = typography.labelSmall,
            modifier =
                Modifier.padding(
                    horizontal = SpacingMd,
                    vertical = SpacingXs,
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
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val categoryAccent =
        workout.categoryColorId?.let { accent ->
            baseCategoryColor(
                accent = categoryAccentColor(accent),
                isDarkTheme = isDarkTheme,
                surface = colorScheme.surface,
            )
        }
    val categoryChipBase =
        categoryAccent?.let { accent ->
            if (workout.isCompleted) {
                completedCategoryColor(
                    accent = accent,
                    isDarkTheme = isDarkTheme,
                    surface = colorScheme.surface,
                )
            } else {
                accent
            }
        }
    val categoryChipBackground =
        categoryChipBase?.let { base ->
            lighterTone(base, isDarkTheme = isDarkTheme)
        }
    val categoryChipContent = Color.White

    Surface(
        color = colors.background,
        contentColor = colors.content,
        shape = shapes.medium,
        modifier =
            modifier
                .fillMaxWidth()
                .then(Modifier)
                .alpha(GHOST_ROW_ALPHA),
    ) {
        Box {
            if (!workout.isRestDay && categoryAccent != null) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(SpacingXs)
                            .background(categoryAccent),
                )
            }

            Row(
                modifier = Modifier.padding(ContentPadding),
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
                                    modifier = Modifier.size(SmallIconSize),
                                )

                                Spacer(modifier = Modifier.width(SpacingSm))

                                Text(
                                    text = stringResource(R.string.rest_day_label),
                                    style = typography.titleSmall,
                                    color = colors.content,
                                )
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
                                workout.categoryName?.let { categoryName ->
                                    TitleChip(
                                        label = categoryName,
                                        containerColor =
                                            categoryChipBackground ?: colors.content.copy(alpha = TYPE_CHIP_ALPHA),
                                        contentColor = categoryChipContent,
                                    )
                                }

                                Column(modifier = Modifier.padding(start = SpacingXs)) {
                                    Text(
                                        text = workout.type,
                                        style = typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.content,
                                    )
                                }
                            }
                        }

                        if (hasDescription) {
                            Spacer(modifier = Modifier.height(SpacingXs))
                            Text(
                                text = workout.description,
                                style = typography.bodySmall,
                                color = colors.content,
                                modifier = Modifier.padding(start = SpacingXs),
                            )
                        }
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
    val themeColorScheme = colorScheme
    val todoColor = CompletedBlue
    val todoContent = CompletedBlueContent
    val completedColor = TodoBlue
    val completedContent = TodoBlueContent
    val isUnscheduled = workout.dayOfWeek == null
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val restDayBackground = themeColorScheme.surfaceColorAtElevation(ElevationSm)
    val restDayContent = themeColorScheme.onSurfaceVariant
    val categoryAccent =
        workout.categoryColorId?.let { accent ->
            baseCategoryColor(
                accent = categoryAccentColor(accent),
                isDarkTheme = isDarkTheme,
                surface = themeColorScheme.surface,
            )
        }
    val categoryCompletedBackground =
        categoryAccent?.let { accent ->
            completedCategoryColor(
                accent = accent,
                isDarkTheme = isDarkTheme,
                surface = themeColorScheme.surface,
            )
        }
    val categoryContent =
        categoryAccent?.let { background ->
            readableContentOn(background)
        }
    val categoryCompletedContent =
        categoryCompletedBackground?.let { background ->
            readableContentOn(background)
        }

    val background =
        when {
            isDragging -> themeColorScheme.surfaceVariant
            workout.isCompleted && categoryAccent == null -> completedColor
            workout.isRestDay -> restDayBackground
            workout.isCompleted && categoryCompletedBackground != null -> categoryCompletedBackground
            categoryAccent != null -> categoryAccent
            isUnscheduled -> todoColor
            else -> todoColor
        }
    val content =
        when {
            workout.isRestDay -> restDayContent
            workout.isCompleted && categoryContent == null -> completedContent
            workout.isCompleted && categoryCompletedContent != null -> categoryCompletedContent
            categoryContent != null -> categoryContent
            isUnscheduled -> todoContent
            else -> todoContent
        }

    return RowColors(background, content)
}

private fun itemBoundsHeight(coordinates: LayoutCoordinates?): Float {
    return coordinates?.boundsInRoot()?.height ?: 0f
}

private fun readableContentOn(background: Color): Color {
    return if (background.luminance() > 0.5f) Color.Black else Color.White
}

private fun lighterTone(
    color: Color,
    isDarkTheme: Boolean,
): Color {
    val blend = if (isDarkTheme) 0.16f else 0.1f
    return lerp(color, Color.White, blend)
}
