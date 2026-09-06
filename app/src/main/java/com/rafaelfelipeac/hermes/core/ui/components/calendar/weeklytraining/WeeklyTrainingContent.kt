package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Rect.Companion.Zero
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SwipeThreshold
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyTrainingAutoScrollEdge
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyTrainingAutoScrollSafePadding
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy.AUTO_WHEN_MULTIPLE
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

private const val NO_INDEX = -1
private const val FIRST_LIST_INDEX = 0
private const val SECTION_LIST_ITEM_SPAN = 2
private const val WEEK_CHANGE_STEP = 1L
private val AutoScrollFrameDelay = 16.milliseconds
internal const val WEEKLY_TRAINING_CONTENT_TAG = "weekly-training-content"
internal const val SECTION_ITEM_KEY_PREFIX = "section-"
internal const val DIVIDER_ITEM_KEY_PREFIX = "divider-"
internal const val SECTION_HEADER_TAG_PREFIX = "section-header-"

@Composable
fun WeeklyTrainingContent(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    workouts: List<WorkoutUi>,
    focusedCategoryId: Long? = null,
    requestedWorkoutId: WorkoutId? = null,
    dayOrder: List<DayOfWeek> = DayOfWeek.entries,
    slotModePolicy: SlotModePolicy = AUTO_WHEN_MULTIPLE,
    onWorkoutMoved: (WorkoutId, DayOfWeek?, TimeSlot?, Int) -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onWeekChanged: (LocalDate) -> Unit = {},
) {
    val sections =
        remember(workouts, dayOrder) {
            buildWeeklyTrainingSections(workouts = workouts, dayOrder = dayOrder)
        }
    val sectionBounds = remember { mutableStateMapOf<SectionKey, Rect>() }
    val slotBounds = remember { mutableStateMapOf<SlotSectionKey, Rect>() }
    val itemBounds = remember { mutableStateMapOf<WorkoutId, Rect>() }
    val dragController = rememberWeeklyTrainingDragController()
    val hapticFeedback = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val swipeThreshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    val autoScrollEdge = with(LocalDensity.current) { WeeklyTrainingAutoScrollEdge.toPx() }
    val autoScrollSafePadding = with(LocalDensity.current) { WeeklyTrainingAutoScrollSafePadding.toPx() }
    val workoutsBySection =
        remember(workouts, sections) {
            buildWeeklyTrainingWorkoutsBySection(workouts = workouts, sections = sections)
        }
    val sectionDates =
        remember(selectedDate, dayOrder) {
            buildWeeklyTrainingSectionDates(
                selectedDate = selectedDate,
                dayOrder = dayOrder,
            )
        }
    val dayUsesSlots =
        remember(workouts, sections, slotModePolicy) {
            buildWeeklyTrainingDayUsesSlots(
                workouts = workouts,
                sections = sections,
                slotModePolicy = slotModePolicy,
            )
        }
    val draggedWorkout = dragController.draggedWorkoutId?.let { id -> workouts.firstOrNull { it.id == id } }
    var previousUnscheduledIds by remember { mutableStateOf<Set<WorkoutId>>(emptySet()) }
    var isTbdHelpVisible by remember { mutableStateOf(false) }
    var lastHapticTarget by remember { mutableStateOf<DropTargetHapticKey?>(null) }
    var hasObservedHapticTarget by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate, sections) {
        if (dragController.draggedWorkoutId == null) {
            val targetSection = SectionKey.Day(selectedDate.dayOfWeek)
            val targetIndex = sections.indexOf(targetSection)

            if (targetIndex != NO_INDEX) {
                listState.animateScrollToItem(targetIndex * SECTION_LIST_ITEM_SPAN)
            }
        }
    }

    LaunchedEffect(workouts, sections) {
        val currentUnscheduledIds =
            workouts
                .filter { it.dayOfWeek == null }
                .map { it.id }
                .toSet()
        val hasNewUnscheduled = currentUnscheduledIds.any { it !in previousUnscheduledIds }

        if (hasNewUnscheduled && sections.firstOrNull() == SectionKey.ToBeDefined) {
            listState.animateScrollToItem(FIRST_LIST_INDEX)
        }

        previousUnscheduledIds = currentUnscheduledIds
    }

    LaunchedEffect(dragController.draggedWorkoutId) {
        if (dragController.draggedWorkoutId == null) {
            lastHapticTarget = null
            hasObservedHapticTarget = false
        }

        while (dragController.draggedWorkoutId != null) {
            val position = dragController.dragPosition

            if (position != null && dragController.containerBounds != Zero) {
                val autoScrollStep =
                    computeAutoScrollStep(
                        position = position,
                        context =
                            AutoScrollContext(
                                containerBounds = dragController.containerBounds,
                                edge = autoScrollEdge,
                                safePadding = autoScrollSafePadding,
                                canScrollBackward = listState.canScrollBackward,
                                canScrollForward = listState.canScrollForward,
                            ),
                    )

                if (autoScrollStep.clampedPosition != position) {
                    dragController.updateDragPosition(autoScrollStep.clampedPosition)
                }

                if (autoScrollStep.scrollDelta != 0f) {
                    listState.scrollBy(autoScrollStep.scrollDelta)
                }
            }

            delay(AutoScrollFrameDelay)
        }
    }

    LaunchedEffect(
        dragController.draggedWorkoutId,
        dragController.liveDropPreview?.targetSection,
        dragController.liveDropPreview?.targetTimeSlot,
    ) {
        if (dragController.draggedWorkoutId == null) {
            lastHapticTarget = null
            hasObservedHapticTarget = false

            return@LaunchedEffect
        }

        val target =
            dragController.liveDropPreview?.let { preview ->
                DropTargetHapticKey(
                    section = preview.targetSection,
                    timeSlot = preview.targetTimeSlot,
                )
            }

        if (target == null) {
            lastHapticTarget = null

            return@LaunchedEffect
        }

        if (!hasObservedHapticTarget) {
            lastHapticTarget = target
            hasObservedHapticTarget = true

            return@LaunchedEffect
        }

        if (target != lastHapticTarget) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
            lastHapticTarget = target
        }
    }

    Box(
        modifier =
            modifier
                .testTag(WEEKLY_TRAINING_CONTENT_TAG)
                .pointerInput(selectedDate, dragController.draggedWorkoutId) {
                    if (dragController.draggedWorkoutId == null) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragController.resetSwipeDrag() },
                            onHorizontalDrag = { _, dragDelta -> dragController.dragAmount += dragDelta },
                            onDragEnd = {
                                when {
                                    dragController.dragAmount <= -swipeThreshold ->
                                        onWeekChanged(selectedDate.plusWeeks(WEEK_CHANGE_STEP))

                                    dragController.dragAmount >= swipeThreshold ->
                                        onWeekChanged(selectedDate.minusWeeks(WEEK_CHANGE_STEP))
                                }

                                dragController.resetSwipeDrag()
                            },
                            onDragCancel = { dragController.resetSwipeDrag() },
                        )
                    }
                }
                .onGloballyPositioned {
                    dragController.updateContainerBounds(it.boundsInRoot())
                }
                .pointerInput(dragController.draggedWorkoutId, dragController.containerBounds) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val activeId = dragController.draggedWorkoutId

                            if (activeId == null || dragController.containerBounds == Zero) {
                                dragController.clearPointerTracking()
                            } else {
                                val trackedChange =
                                    dragController.dragPointerId?.let { pointerId ->
                                        event.changes.firstOrNull { it.id == pointerId }
                                    }
                                val change =
                                    trackedChange
                                        ?: run {
                                            val pressedChanges = event.changes.filter { it.pressed }
                                            if (pressedChanges.isEmpty()) {
                                                null
                                            } else {
                                                val referencePosition =
                                                    dragController.dragPosition?.let { currentRoot ->
                                                        Offset(
                                                            x = currentRoot.x - dragController.containerBounds.left,
                                                            y = currentRoot.y - dragController.containerBounds.top,
                                                        )
                                                    }
                                                val nearestPressed =
                                                    if (referencePosition == null) {
                                                        pressedChanges.first()
                                                    } else {
                                                        pressedChanges.minByOrNull { pointerChange ->
                                                            val dx = pointerChange.position.x - referencePosition.x
                                                            val dy = pointerChange.position.y - referencePosition.y
                                                            dx * dx + dy * dy
                                                        } ?: pressedChanges.first()
                                                    }
                                                dragController.updateDragPointer(nearestPressed.id)
                                                nearestPressed
                                            }
                                        }

                                if (change != null) {
                                    val root =
                                        Offset(
                                            dragController.containerBounds.left + change.position.x,
                                            dragController.containerBounds.top + change.position.y,
                                        )

                                    dragController.updateDragPosition(root)

                                    val activeWorkout =
                                        activeId.let { id -> workouts.firstOrNull { it.id == id } }
                                    if (activeWorkout != null) {
                                        val fallbackSection = activeWorkout.dayOfWeek.toSectionKey()
                                        dragController.hoveredSection =
                                            findTargetSection(root, sectionBounds, fallbackSection)
                                        dragController.liveDropPreview =
                                            computeDropPreview(
                                                draggedWorkoutId = activeId,
                                                dragPosition = root,
                                                context =
                                                    DropContext(
                                                        workouts = workouts,
                                                        workoutsBySection = workoutsBySection,
                                                        sectionBounds = sectionBounds,
                                                        slotBounds = slotBounds,
                                                        dayUsesSlots = dayUsesSlots,
                                                        itemBounds = itemBounds,
                                                        onWorkoutMoved = onWorkoutMoved,
                                                    ),
                                                targetSectionOverride = dragController.hoveredSection,
                                            )
                                    }

                                    if (!change.pressed) {
                                        val preview = dragController.liveDropPreview
                                        if (preview != null) {
                                            applyDropPreview(
                                                draggedWorkoutId = activeId,
                                                workouts = workouts,
                                                preview = preview,
                                                onWorkoutMoved = onWorkoutMoved,
                                            )
                                        } else {
                                            handleDrop(
                                                draggedWorkoutId = activeId,
                                                dragPosition = root,
                                                context =
                                                    DropContext(
                                                        workouts = workouts,
                                                        workoutsBySection = workoutsBySection,
                                                        sectionBounds = sectionBounds,
                                                        slotBounds = slotBounds,
                                                        dayUsesSlots = dayUsesSlots,
                                                        itemBounds = itemBounds,
                                                        onWorkoutMoved = onWorkoutMoved,
                                                    ),
                                                targetSectionOverride = dragController.hoveredSection,
                                            )
                                        }
                                        dragController.clearDrag()
                                    }
                                }
                            }
                        }
                    }
                },
    ) {
        WeeklyTrainingSectionList(
            listState = listState,
            sections = sections,
            workoutsBySection = workoutsBySection,
            sectionDates = sectionDates,
            dayUsesSlots = dayUsesSlots,
            focusedCategoryId = focusedCategoryId,
            requestedWorkoutId = requestedWorkoutId,
            sectionBounds = sectionBounds,
            slotBounds = slotBounds,
            itemBounds = itemBounds,
            dragController = dragController,
            hapticFeedback = hapticFeedback,
            onWorkoutDragStarted = {
                lastHapticTarget = null
                hasObservedHapticTarget = false
                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
            },
            onWorkoutCompletionChanged = onWorkoutCompletionChanged,
            onWorkoutEdit = onWorkoutEdit,
            onWorkoutDelete = onWorkoutDelete,
            onTbdHelpClick = { isTbdHelpVisible = true },
        )

        if (draggedWorkout != null) {
            WeeklyTrainingDragOverlay(
                draggedWorkout = draggedWorkout,
                dragController = dragController,
                itemBounds = itemBounds,
                autoScrollSafePadding = autoScrollSafePadding,
            )
        }
    }

    WeeklyTrainingTbdHelpDialog(
        visible = isTbdHelpVisible,
        onDismiss = { isTbdHelpVisible = false },
    )
}

private data class DropTargetHapticKey(
    val section: SectionKey,
    val timeSlot: TimeSlot?,
)

private fun applyDropPreview(
    draggedWorkoutId: WorkoutId,
    workouts: List<WorkoutUi>,
    preview: DropPreview,
    onWorkoutMoved: (WorkoutId, DayOfWeek?, TimeSlot?, Int) -> Unit,
) {
    val workout = workouts.firstOrNull { it.id == draggedWorkoutId } ?: return
    val newDay = preview.targetSection.dayOfWeekOrNull()
    val newTimeSlot = preview.targetTimeSlot
    val newOrder = preview.targetOrder

    if (newDay != workout.dayOfWeek || newTimeSlot != workout.timeSlot || newOrder != workout.order) {
        onWorkoutMoved(workout.id, newDay, newTimeSlot, newOrder)
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyTrainingContentPreview(
    @PreviewParameter(WeeklyTrainingContentPreviewProvider::class)
    preview: WeeklyTrainingContentPreviewData,
) {
    WeeklyTrainingContent(
        selectedDate = preview.selectedDate,
        workouts = preview.workouts,
        onWorkoutMoved = { _, _, _, _ -> },
        onWorkoutCompletionChanged = { _, _ -> },
        onWorkoutEdit = {},
        onWorkoutDelete = {},
    )
}
