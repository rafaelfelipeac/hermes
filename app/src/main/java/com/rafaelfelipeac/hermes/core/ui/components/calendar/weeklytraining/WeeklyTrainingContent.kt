package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Rect.Companion.Zero
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SectionKey.Day
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SectionKey.ToBeDefined
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SwipeThreshold
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyCalendarBottomPadding
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.DayOfWeek.FRIDAY
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.SATURDAY
import java.time.DayOfWeek.SUNDAY
import java.time.DayOfWeek.THURSDAY
import java.time.DayOfWeek.TUESDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate

private const val NO_INDEX = -1
private const val FIRST_LIST_INDEX = 0
private const val SECTION_LIST_ITEM_SPAN = 2
private const val WEEK_CHANGE_STEP = 1L
private const val AUTO_SCROLL_EDGE = 96f
private const val AUTO_SCROLL_MAX_SPEED = 18f
private const val AUTO_SCROLL_SAFE_PADDING = 16f
private const val AUTO_SCROLL_FRAME_DELAY_MS = 16L
private const val WEEKLY_TRAINING_CONTENT_TAG = "weekly-training-content"
private const val SECTION_ITEM_KEY_PREFIX = "section-"
private const val DIVIDER_ITEM_KEY_PREFIX = "divider-"
private const val SECTION_HEADER_TAG_PREFIX = "section-header-"

@Composable
fun WeeklyTrainingContent(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    workouts: List<WorkoutUi>,
    onWorkoutMoved: (WorkoutId, DayOfWeek?, Int) -> Unit,
    onWorkoutCompletionChanged: (WorkoutUi, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onWeekChanged: (LocalDate) -> Unit = {},
) {
    val sections =
        remember(workouts) {
            buildList {
                if (workouts.any { it.dayOfWeek == null }) {
                    add(ToBeDefined)
                }

                add(Day(MONDAY))
                add(Day(TUESDAY))
                add(Day(WEDNESDAY))
                add(Day(THURSDAY))
                add(Day(FRIDAY))
                add(Day(SATURDAY))
                add(Day(SUNDAY))
            }
        }

    val sectionBounds = remember { mutableStateMapOf<SectionKey, Rect>() }
    val itemBounds = remember { mutableStateMapOf<WorkoutId, Rect>() }
    var draggedWorkoutId by remember { mutableStateOf<WorkoutId?>(null) }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    var draggedItemHeight by remember { mutableFloatStateOf(0f) }
    var containerBounds by remember { mutableStateOf(Zero) }
    val listState = rememberLazyListState()
    val swipeThreshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    var dragAmount by remember { mutableFloatStateOf(0f) }
    val workoutsBySection =
        remember(workouts) {
            sections.associateWith { section ->
                workouts
                    .filter { it.dayOfWeek == section.dayOfWeekOrNull() }
                    .sortedBy { it.order }
            }
        }
    val draggedWorkout = draggedWorkoutId?.let { id -> workouts.firstOrNull { it.id == id } }
    var previousUnscheduledIds by remember { mutableStateOf<Set<WorkoutId>>(emptySet()) }
    var isTbdHelpVisible by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDate, sections) {
        if (draggedWorkoutId == null) {
            val targetSection = Day(selectedDate.dayOfWeek)
            val targetIndex = sections.indexOf(targetSection)

            if (targetIndex != NO_INDEX) {
                val listIndex = targetIndex * SECTION_LIST_ITEM_SPAN
                listState.animateScrollToItem(listIndex)
            }
        }
    }

    LaunchedEffect(workouts) {
        val currentUnscheduledIds =
            workouts
                .filter { it.dayOfWeek == null }
                .map { it.id }
                .toSet()
        val hasNewUnscheduled = currentUnscheduledIds.any { it !in previousUnscheduledIds }

        if (hasNewUnscheduled && sections.firstOrNull() == ToBeDefined) {
            listState.animateScrollToItem(FIRST_LIST_INDEX)
        }

        previousUnscheduledIds = currentUnscheduledIds
    }

    LaunchedEffect(draggedWorkoutId) {
        while (draggedWorkoutId != null) {
            val position = dragPosition

            if (position != null && containerBounds != Zero) {
                val edge = AUTO_SCROLL_EDGE
                val maxSpeed = AUTO_SCROLL_MAX_SPEED
                val safeTop = containerBounds.top + AUTO_SCROLL_SAFE_PADDING
                val safeBottom = containerBounds.bottom - AUTO_SCROLL_SAFE_PADDING
                val clampedPosition =
                    Offset(
                        position.x,
                        position.y.coerceIn(safeTop, safeBottom),
                    )

                if (clampedPosition != position) {
                    dragPosition = clampedPosition
                }

                val distanceToTop = clampedPosition.y - containerBounds.top
                val distanceToBottom = containerBounds.bottom - clampedPosition.y
                val scrollDelta =
                    when {
                        distanceToTop < edge && listState.canScrollBackward -> {
                            -maxSpeed * (1f - (distanceToTop / edge))
                        }

                        distanceToBottom < edge && listState.canScrollForward -> {
                            maxSpeed * (1f - (distanceToBottom / edge))
                        }

                        else -> 0f
                    }

                if (scrollDelta != 0f) {
                    listState.scrollBy(scrollDelta)
                }
            }
            delay(AUTO_SCROLL_FRAME_DELAY_MS)
        }
    }

    Box(
        modifier =
            modifier
                .testTag(WEEKLY_TRAINING_CONTENT_TAG)
                .pointerInput(selectedDate, draggedWorkoutId) {
                    if (draggedWorkoutId == null) {
                        detectHorizontalDragGestures(
                            onDragStart = { dragAmount = 0f },
                            onHorizontalDrag = { _, dragDelta -> dragAmount += dragDelta },
                            onDragEnd = {
                                when {
                                    dragAmount <= -swipeThreshold ->
                                        onWeekChanged(selectedDate.plusWeeks(WEEK_CHANGE_STEP))

                                    dragAmount >= swipeThreshold ->
                                        onWeekChanged(selectedDate.minusWeeks(WEEK_CHANGE_STEP))
                                }

                                dragAmount = 0f
                            },
                            onDragCancel = { dragAmount = 0f },
                        )
                    }
                }
                .onGloballyPositioned {
                    containerBounds = it.boundsInRoot()
                }
                .pointerInput(draggedWorkoutId, containerBounds) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            val activeId = draggedWorkoutId

                            if (change == null || activeId == null || containerBounds == Zero) {
                                continue
                            }

                            val root =
                                Offset(
                                    containerBounds.left + change.position.x,
                                    containerBounds.top + change.position.y,
                                )

                            dragPosition = root

                            if (!change.pressed) {
                                handleDrop(
                                    draggedWorkoutId = activeId,
                                    dragPosition = root,
                                    context =
                                        DropContext(
                                            workouts = workouts,
                                            workoutsBySection = workoutsBySection,
                                            sectionBounds = sectionBounds,
                                            itemBounds = itemBounds,
                                            onWorkoutMoved = onWorkoutMoved,
                                        ),
                                )
                                draggedWorkoutId = null
                                dragPosition = null
                                draggedItemHeight = 0f
                            }
                        }
                    }
                },
    ) {
        LazyColumn(
            state = listState,
            userScrollEnabled = draggedWorkoutId == null,
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
            contentPadding = PaddingValues(bottom = WeeklyCalendarBottomPadding),
        ) {
            sections.forEach { section ->
                item(key = "$SECTION_ITEM_KEY_PREFIX${section.key}") {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned {
                                    sectionBounds[section] = it.boundsInRoot()
                                },
                    ) {
                        SectionHeader(
                            title = section.title(),
                            tag = "$SECTION_HEADER_TAG_PREFIX${section.key}",
                            showHelp = section == ToBeDefined,
                            onHelpClick = { isTbdHelpVisible = true },
                        )

                        val items = workoutsBySection[section].orEmpty()

                        if (items.isEmpty()) {
                            EmptySectionRow()
                        } else {
                            items.forEachIndexed { index, workout ->
                                if (index > FIRST_LIST_INDEX) {
                                    Spacer(modifier = Modifier.height(SpacingMd))
                                }

                                WorkoutRow(
                                    workout = workout,
                                    isDragging = draggedWorkoutId == workout.id,
                                    onToggleCompleted = { checked ->
                                        onWorkoutCompletionChanged(workout, checked)
                                    },
                                    onDragStarted = { position, height ->
                                        draggedWorkoutId = workout.id
                                        dragPosition = position
                                        draggedItemHeight = height
                                    },
                                    onEdit = { onWorkoutEdit(workout) },
                                    onDelete = { onWorkoutDelete(workout) },
                                    onItemPositioned = { itemBounds[workout.id] = it },
                                )
                            }
                        }
                    }
                }

                item(key = "$DIVIDER_ITEM_KEY_PREFIX${section.key}") {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = SpacingMd),
                        color = colorScheme.outlineVariant,
                    )
                }
            }
        }

        if (draggedWorkout != null && dragPosition != null) {
            val currentDragPosition = checkNotNull(dragPosition)
            val ghostHeight =
                if (draggedItemHeight > 0f) {
                    draggedItemHeight
                } else {
                    itemBounds[draggedWorkout.id]?.height ?: 0f
                }
            val ghostYOffset = currentDragPosition.y - containerBounds.top - ghostHeight / 2f

            GhostWorkoutRow(
                workout = draggedWorkout,
                modifier =
                    Modifier.graphicsLayer {
                        translationY = ghostYOffset
                    },
            )
        }
    }

    if (isTbdHelpVisible) {
        AlertDialog(
            onDismissRequest = { isTbdHelpVisible = false },
            title = { Text(text = stringResource(R.string.tbd_help_title)) },
            text = { Text(text = stringResource(R.string.tbd_help_message)) },
            confirmButton = {
                TextButton(onClick = { isTbdHelpVisible = false }) {
                    Text(text = stringResource(R.string.tbd_help_confirm))
                }
            },
        )
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
        onWorkoutMoved = { _, _, _ -> },
        onWorkoutCompletionChanged = { _, _ -> },
        onWorkoutEdit = {},
        onWorkoutDelete = {},
    )
}
