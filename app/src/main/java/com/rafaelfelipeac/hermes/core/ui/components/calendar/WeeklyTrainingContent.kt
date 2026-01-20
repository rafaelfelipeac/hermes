package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens
import com.rafaelfelipeac.hermes.core.ui.theme.TextSizes
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyTrainingContentPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlueContent
import com.rafaelfelipeac.hermes.core.ui.theme.RestDayContentDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDayContentLight
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlueContent
import kotlinx.coroutines.delay
import java.time.DayOfWeek
import java.time.LocalDate

private const val WORKOUT_ROW_DRAGGING_ALPHA = 0f
private const val WORKOUT_ROW_CONTENT_ALPHA = 1f
private const val WORKOUT_BORDER_ALPHA = 0.6f
private const val GHOST_ROW_ALPHA = 0.45f
private const val TYPE_CHIP_ALPHA = 0.18f
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
private const val SECTION_KEY_TBD = "tbd"
typealias WorkoutId = Long

data class WorkoutUi(
    val id: WorkoutId,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val isRestDay: Boolean,
    val order: Int,
)

@Composable
fun WeeklyTrainingContent(
    selectedDate: LocalDate,
    workouts: List<WorkoutUi>,
    onWorkoutMoved: (WorkoutId, DayOfWeek?, Int) -> Unit,
    onWorkoutCompletionChanged: (WorkoutId, Boolean) -> Unit,
    onWorkoutEdit: (WorkoutUi) -> Unit,
    onWorkoutDelete: (WorkoutUi) -> Unit,
    onWeekChanged: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sections =
        remember(workouts) {
            buildList {
                if (workouts.any { it.dayOfWeek == null }) {
                    add(SectionKey.ToBeDefined)
                }
                add(SectionKey.Day(DayOfWeek.MONDAY))
                add(SectionKey.Day(DayOfWeek.TUESDAY))
                add(SectionKey.Day(DayOfWeek.WEDNESDAY))
                add(SectionKey.Day(DayOfWeek.THURSDAY))
                add(SectionKey.Day(DayOfWeek.FRIDAY))
                add(SectionKey.Day(DayOfWeek.SATURDAY))
                add(SectionKey.Day(DayOfWeek.SUNDAY))
            }
        }

    val sectionBounds = remember { mutableStateMapOf<SectionKey, Rect>() }
    val itemBounds = remember { mutableStateMapOf<WorkoutId, Rect>() }
    var draggedWorkoutId by remember { mutableStateOf<WorkoutId?>(null) }
    var dragPosition by remember { mutableStateOf<Offset?>(null) }
    var draggedItemHeight by remember { mutableStateOf(0f) }
    var containerBounds by remember { mutableStateOf(Rect.Zero) }
    val listState = rememberLazyListState()
    val swipeThreshold = with(LocalDensity.current) { Dimens.SwipeThreshold.toPx() }
    var dragAmount by remember { mutableStateOf(0f) }
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

    LaunchedEffect(selectedDate, sections) {
        if (draggedWorkoutId == null) {
            val targetSection = SectionKey.Day(selectedDate.dayOfWeek)
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
        if (hasNewUnscheduled && sections.firstOrNull() == SectionKey.ToBeDefined) {
            listState.animateScrollToItem(FIRST_LIST_INDEX)
        }
    }

    LaunchedEffect(draggedWorkoutId) {
        while (draggedWorkoutId != null) {
            val position = dragPosition
            if (position != null && containerBounds != Rect.Zero) {
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
                            onHorizontalDrag = { _, amount ->
                                dragAmount += amount
                            },
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
                            if (change == null || activeId == null || containerBounds == Rect.Zero) {
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
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingLg),
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
                        )

                        val items = workoutsBySection[section].orEmpty()

                        if (items.isEmpty()) {
                            EmptySectionRow()
                        } else {
                            items.forEachIndexed { index, workout ->
                                if (index > FIRST_LIST_INDEX) {
                                    Spacer(modifier = Modifier.height(Dimens.SpacingMd))
                                }

                                WorkoutRow(
                                    workout = workout,
                                    isDragging = draggedWorkoutId == workout.id,
                                    onToggleCompleted = { checked ->
                                        onWorkoutCompletionChanged(workout.id, checked)
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
                        modifier = Modifier.padding(top = Dimens.SpacingMd),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }

        if (draggedWorkout != null && dragPosition != null) {
            val ghostHeight =
                if (draggedItemHeight > 0f) {
                    draggedItemHeight
                } else {
                    itemBounds[draggedWorkout.id]?.height ?: 0f
                }
            val ghostYOffset = dragPosition!!.y - containerBounds.top - ghostHeight / 2f
            GhostWorkoutRow(
                workout = draggedWorkout,
                modifier =
                    Modifier.graphicsLayer {
                        translationY = ghostYOffset
                    },
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    tag: String,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier =
            Modifier
                .padding(
                    horizontal = Dimens.SpacingXs,
                    vertical = Dimens.SpacingMd,
                )
                .testTag(tag),
    )
}

@Composable
private fun EmptySectionRow() {
    Text(
        text = stringResource(R.string.no_workouts),
        style = MaterialTheme.typography.bodySmall,
        modifier =
            Modifier.padding(
                horizontal = Dimens.SpacingLg,
                vertical = Dimens.SpacingMd,
            ),
    )
}

@Composable
private fun WorkoutRow(
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
            modifier =
                Modifier
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
                            checked = workout.isCompleted,
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
private fun GhostWorkoutRow(
    workout: WorkoutUi,
    modifier: Modifier = Modifier,
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

private fun findTargetSection(
    dropPosition: Offset,
    sectionBounds: Map<SectionKey, Rect>,
    fallback: SectionKey,
): SectionKey {
    return sectionBounds.entries.firstOrNull { it.value.contains(dropPosition) }?.key
        ?: fallback
}

private fun computeOrderForDrop(
    dropPosition: Offset,
    items: List<WorkoutUi>,
    draggedId: WorkoutId,
    itemBounds: Map<WorkoutId, Rect>,
): Int {
    val candidates = items.filterNot { it.id == draggedId }

    if (candidates.isEmpty()) return FIRST_LIST_INDEX

    val sorted = candidates.sortedBy { it.order }
    val dropIndex =
        sorted.indexOfFirst { workout ->
            val bounds = itemBounds[workout.id] ?: return@indexOfFirst false
            dropPosition.y < bounds.center.y
        }

    return if (dropIndex == NO_INDEX) sorted.size else dropIndex
}

private fun itemBoundsHeight(coordinates: LayoutCoordinates?): Float {
    return coordinates?.boundsInRoot()?.height ?: 0f
}

private data class DropContext(
    val workouts: List<WorkoutUi>,
    val workoutsBySection: Map<SectionKey, List<WorkoutUi>>,
    val sectionBounds: Map<SectionKey, Rect>,
    val itemBounds: Map<WorkoutId, Rect>,
    val onWorkoutMoved: (WorkoutId, DayOfWeek?, Int) -> Unit,
)

private fun handleDrop(
    draggedWorkoutId: WorkoutId,
    dragPosition: Offset,
    context: DropContext,
) {
    val workout = context.workouts.firstOrNull { it.id == draggedWorkoutId } ?: return
    val fallbackSection = workout.dayOfWeek.toSectionKey()
    val targetSection = findTargetSection(dragPosition, context.sectionBounds, fallbackSection)
    val targetItems = context.workoutsBySection[targetSection].orEmpty()
    val newOrder =
        computeOrderForDrop(dragPosition, targetItems, workout.id, context.itemBounds)
    val newDay = targetSection.dayOfWeekOrNull()

    if (newDay != workout.dayOfWeek || newOrder != workout.order) {
        context.onWorkoutMoved(workout.id, newDay, newOrder)
    }
}

private sealed class SectionKey(val key: String) {
    object ToBeDefined : SectionKey(SECTION_KEY_TBD)

    data class Day(val dayOfWeek: DayOfWeek) : SectionKey(dayOfWeek.name)
}

private fun DayOfWeek?.toSectionKey(): SectionKey {
    return if (this == null) SectionKey.ToBeDefined else SectionKey.Day(this)
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

    val background =
        when {
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

@Composable
private fun SectionKey.title(): String {
    return when (this) {
        SectionKey.ToBeDefined -> stringResource(R.string.section_to_be_defined)
        is SectionKey.Day -> stringResource(dayOfWeek.labelRes())
    }
}

private fun DayOfWeek.labelRes(): Int {
    return when (this) {
        DayOfWeek.MONDAY -> R.string.day_monday
        DayOfWeek.TUESDAY -> R.string.day_tuesday
        DayOfWeek.WEDNESDAY -> R.string.day_wednesday
        DayOfWeek.THURSDAY -> R.string.day_thursday
        DayOfWeek.FRIDAY -> R.string.day_friday
        DayOfWeek.SATURDAY -> R.string.day_saturday
        DayOfWeek.SUNDAY -> R.string.day_sunday
    }
}

private fun SectionKey.dayOfWeekOrNull(): DayOfWeek? {
    return when (this) {
        SectionKey.ToBeDefined -> null
        is SectionKey.Day -> dayOfWeek
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
