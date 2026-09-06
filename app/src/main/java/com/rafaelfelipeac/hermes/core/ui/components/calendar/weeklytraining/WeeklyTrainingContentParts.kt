package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SectionKey.Day
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SectionKey.ToBeDefined
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderHairline
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyCalendarBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.Zero
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

private const val FIRST_LIST_INDEX = 0
private val WEEKLY_TRAINING_SLOTS = listOf(MORNING, AFTERNOON, NIGHT)

@Composable
internal fun WeeklyTrainingSectionList(
    listState: LazyListState,
    sections: List<SectionKey>,
    workoutsBySection: Map<SectionKey, List<WorkoutUi>>,
    sectionDates: Map<SectionKey, LocalDate>,
    dayUsesSlots: Map<DayOfWeek, Boolean>,
    focusedCategoryId: Long?,
    requestedWorkoutId: WorkoutId?,
    sectionBounds: MutableMap<SectionKey, Rect>,
    slotBounds: MutableMap<SlotSectionKey, Rect>,
    itemBounds: MutableMap<WorkoutId, Rect>,
    dragController: WeeklyTrainingDragController,
    hapticFeedback: HapticFeedback,
    onWorkoutDragStarted: () -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onTbdHelpClick: () -> Unit,
) {
    LazyColumn(
        state = listState,
        userScrollEnabled = dragController.draggedWorkoutId == null,
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
        contentPadding =
            PaddingValues(
                bottom = WeeklyCalendarBottomPadding,
            ),
    ) {
        sections.forEach { section ->
            item(key = "$SECTION_ITEM_KEY_PREFIX${section.key}") {
                WeeklyTrainingSectionContent(
                    section = section,
                    sectionDate = sectionDates[section],
                    workouts = workoutsBySection[section].orEmpty(),
                    shouldUseSlots = section is Day && dayUsesSlots[section.dayOfWeek] == true,
                    focusedCategoryId = focusedCategoryId,
                    requestedWorkoutId = requestedWorkoutId,
                    sectionBounds = sectionBounds,
                    slotBounds = slotBounds,
                    itemBounds = itemBounds,
                    dragController = dragController,
                    hapticFeedback = hapticFeedback,
                    onWorkoutDragStarted = onWorkoutDragStarted,
                    onWorkoutCompletionChanged = onWorkoutCompletionChanged,
                    onWorkoutEdit = onWorkoutEdit,
                    onWorkoutDelete = onWorkoutDelete,
                    onTbdHelpClick = onTbdHelpClick,
                )
            }

            item(key = "$DIVIDER_ITEM_KEY_PREFIX${section.key}") {
                HorizontalDivider(
                    modifier = Modifier.padding(top = SpacingMd),
                    color = colorScheme.outlineVariant,
                )
            }
        }
    }
}

@Composable
private fun WeeklyTrainingSectionContent(
    section: SectionKey,
    sectionDate: LocalDate?,
    workouts: List<WorkoutUi>,
    shouldUseSlots: Boolean,
    focusedCategoryId: Long?,
    requestedWorkoutId: WorkoutId?,
    sectionBounds: MutableMap<SectionKey, Rect>,
    slotBounds: MutableMap<SlotSectionKey, Rect>,
    itemBounds: MutableMap<WorkoutId, Rect>,
    dragController: WeeklyTrainingDragController,
    hapticFeedback: HapticFeedback,
    onWorkoutDragStarted: () -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onTbdHelpClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    sectionBounds[section] = it.boundsInRoot()
                },
    ) {
        SectionHeader(
            title = section.title(sectionDate),
            tag = "section-header-${section.key}",
            showHelp = section == ToBeDefined,
            onHelpClick = onTbdHelpClick,
        )

        if (shouldUseSlots) {
            val workoutsBySlot = buildWeeklyTrainingWorkoutsBySlot(workouts)
            WEEKLY_TRAINING_SLOTS.forEachIndexed { index, slot ->
                val slotItems = workoutsBySlot[slot].orEmpty()
                val isDropTarget =
                    dragController.draggedWorkoutId != null &&
                        dragController.liveDropPreview?.targetSection == section &&
                        dragController.liveDropPreview?.targetTimeSlot == slot

                SlotSectionCard(
                    title = stringResource(slot.labelRes()),
                    isDropTarget = isDropTarget,
                    modifier =
                        Modifier.onGloballyPositioned {
                            slotBounds[SlotSectionKey(section, slot)] = it.boundsInRoot()
                        },
                ) {
                    if (slotItems.isEmpty()) {
                        EmptySectionRow()
                    } else {
                        WeeklyTrainingWorkoutRows(
                            workouts = slotItems,
                            focusedCategoryId = focusedCategoryId,
                            requestedWorkoutId = requestedWorkoutId,
                            dragController = dragController,
                            hapticFeedback = hapticFeedback,
                            onWorkoutDragStarted = onWorkoutDragStarted,
                            onWorkoutCompletionChanged = onWorkoutCompletionChanged,
                            onWorkoutEdit = onWorkoutEdit,
                            onWorkoutDelete = onWorkoutDelete,
                            onItemPositioned = { workout, bounds ->
                                itemBounds[workout.id] = bounds
                            },
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(
                            if (index < WEEKLY_TRAINING_SLOTS.lastIndex) {
                                SpacingMd
                            } else {
                                SpacingXs
                            },
                        ),
                )
            }
        } else if (workouts.isEmpty()) {
            EmptySectionRow()
        } else {
            WeeklyTrainingWorkoutRows(
                workouts = workouts,
                focusedCategoryId = focusedCategoryId,
                requestedWorkoutId = requestedWorkoutId,
                dragController = dragController,
                hapticFeedback = hapticFeedback,
                onWorkoutDragStarted = onWorkoutDragStarted,
                onWorkoutCompletionChanged = onWorkoutCompletionChanged,
                onWorkoutEdit = onWorkoutEdit,
                onWorkoutDelete = onWorkoutDelete,
                onItemPositioned = { workout, bounds ->
                    itemBounds[workout.id] = bounds
                },
            )
        }
    }
}

private val PositiveHapticType = HapticFeedbackType.ToggleOn

private fun completionHapticType(isCompleted: Boolean): HapticFeedbackType {
    return if (isCompleted) {
        PositiveHapticType
    } else {
        HapticFeedbackType.ToggleOff
    }
}

@Composable
private fun WeeklyTrainingWorkoutRows(
    workouts: List<WorkoutUi>,
    focusedCategoryId: Long?,
    requestedWorkoutId: WorkoutId?,
    dragController: WeeklyTrainingDragController,
    hapticFeedback: HapticFeedback,
    onWorkoutDragStarted: () -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onItemPositioned: (WorkoutUi, Rect) -> Unit,
) {
    workouts.forEachIndexed { index, workout ->
        if (index > FIRST_LIST_INDEX) {
            Spacer(modifier = Modifier.height(SpacingMd))
        }

        key(workout.id) {
            WeeklyTrainingWorkoutItem(
                workout = workout,
                focusedCategoryId = focusedCategoryId,
                requestedWorkoutId = requestedWorkoutId,
                dragController = dragController,
                hapticFeedback = hapticFeedback,
                onWorkoutDragStarted = onWorkoutDragStarted,
                onWorkoutCompletionChanged = onWorkoutCompletionChanged,
                onWorkoutEdit = onWorkoutEdit,
                onWorkoutDelete = onWorkoutDelete,
                onItemPositioned = onItemPositioned,
            )
        }
    }
}

@Composable
private fun WeeklyTrainingWorkoutItem(
    workout: WorkoutUi,
    focusedCategoryId: Long?,
    requestedWorkoutId: WorkoutId?,
    dragController: WeeklyTrainingDragController,
    hapticFeedback: HapticFeedback,
    onWorkoutDragStarted: () -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onItemPositioned: (WorkoutUi, Rect) -> Unit,
) {
    WorkoutRow(
        workout = workout,
        isDragging = dragController.draggedWorkoutId == workout.id,
        isDeemphasized = workout.shouldDeemphasize(focusedCategoryId),
        focusRequested = workout.id == requestedWorkoutId,
        onToggleCompleted = { checked ->
            hapticFeedback.performHapticFeedback(completionHapticType(checked))
            onWorkoutCompletionChanged(workout, checked)
        },
        onDragStarted = { position, height ->
            if (dragController.startDrag(workout.id, position, height)) {
                onWorkoutDragStarted()
                dragController.liveDropPreview = null
            }
        },
        onEdit = { onWorkoutEdit(workout) },
        onDelete = { onWorkoutDelete(workout) },
        onItemPositioned = { bounds -> onItemPositioned(workout, bounds) },
    )
}

@Composable
internal fun WeeklyTrainingDragOverlay(
    draggedWorkout: WorkoutUi,
    dragController: WeeklyTrainingDragController,
    itemBounds: Map<WorkoutId, Rect>,
    autoScrollSafePadding: Float,
) {
    val dragPosition = dragController.dragPosition ?: return
    val density = LocalDensity.current
    val ghostHeight =
        if (dragController.draggedItemHeight > 0f) {
            dragController.draggedItemHeight
        } else {
            itemBounds[draggedWorkout.id]?.height ?: 0f
        }
    val ghostWidth = itemBounds[draggedWorkout.id]?.width ?: 0f
    val ghostYOffset = dragPosition.y - dragController.containerBounds.top - ghostHeight / 2f

    GhostWorkoutRow(
        workout = draggedWorkout,
        modifier =
            Modifier
                .graphicsLayer {
                    translationY = ghostYOffset
                }
                .then(
                    if (ghostHeight > 0f) {
                        Modifier.height(with(density) { ghostHeight.toDp() })
                    } else {
                        Modifier
                    },
                )
                .then(
                    if (ghostWidth > 0f) {
                        Modifier.width(with(density) { ghostWidth.toDp() })
                    } else {
                        Modifier
                    },
                ),
        fillMaxWidth = ghostWidth <= 0f,
    )

    dragController.liveDropPreview?.let { preview ->
        DropPreviewBadge(
            preview = preview,
            borderColor = colorScheme.outlineVariant,
            modifier =
                Modifier
                    .graphicsLayer {
                        translationY = ghostYOffset + ghostHeight + autoScrollSafePadding
                    }
                    .then(
                        if (ghostWidth > 0f) {
                            Modifier.width(with(density) { ghostWidth.toDp() })
                        } else {
                            Modifier.fillMaxWidth()
                        },
                    ),
        )
    }
}

@Composable
private fun SlotSectionCard(
    modifier: Modifier = Modifier,
    title: String,
    isDropTarget: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        tonalElevation = ElevationSm,
        shape = shapes.medium,
        border =
            if (isDropTarget) {
                BorderStroke(width = BorderHairline, color = colorScheme.outlineVariant)
            } else {
                null
            },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = Zero),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    start = SpacingLg,
                    end = SpacingLg,
                    top = SpacingMd,
                    bottom = SpacingLg,
                ),
        ) {
            Text(
                text = title,
                style = typography.labelLarge,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = SpacingSm),
            )
            content()
        }
    }
}

@Composable
private fun DropPreviewBadge(
    preview: DropPreview,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    val sectionLabel = preview.targetSection.title()
    val slotLabel =
        preview.targetTimeSlot
            ?.let { stringResource(it.labelRes()) }
            ?.takeIf { preview.targetSection is Day }
    val orderLabel = (preview.targetOrder + 1).toString()
    val label =
        if (slotLabel != null) {
            stringResource(
                R.string.weekly_training_drop_preview_with_slot,
                sectionLabel,
                slotLabel,
                orderLabel,
            )
        } else {
            stringResource(
                R.string.weekly_training_drop_preview_without_slot,
                sectionLabel,
                orderLabel,
            )
        }

    Surface(
        tonalElevation = ElevationSm,
        shape = shapes.medium,
        border = BorderStroke(width = BorderHairline, color = borderColor),
        modifier = modifier.padding(top = SpacingXs),
    ) {
        Text(
            text = label,
            style = typography.labelMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SpacingMd,
                        vertical = SpacingSm,
                    ),
        )
    }
}

@Composable
internal fun WeeklyTrainingTbdHelpDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.weekly_training_tbd_help_title)) },
        text = { Text(text = stringResource(R.string.weekly_training_tbd_help_message)) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.weekly_training_tbd_help_confirm))
            }
        },
    )
}
