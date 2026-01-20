package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.material.icons.outlined.DragIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelfelipeac.hermes.R
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
    val swipeThreshold = with(LocalDensity.current) { 72.dp.toPx() }
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
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
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
            listState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(draggedWorkoutId) {
        while (draggedWorkoutId != null) {
            val position = dragPosition
            if (position != null && containerBounds != Rect.Zero) {
                val edge = 96f
                val maxSpeed = 18f
                val safeTop = containerBounds.top + 16f
                val safeBottom = containerBounds.bottom - 16f
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
            delay(16)
        }
    }

    Box(
        modifier =
            modifier
                .testTag("weekly-training-content")
                .pointerInput(selectedDate, draggedWorkoutId) {
                    if (draggedWorkoutId == null) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, amount ->
                                dragAmount += amount
                            },
                            onDragEnd = {
                                when {
                                    dragAmount <= -swipeThreshold -> onWeekChanged(selectedDate.plusWeeks(1))
                                    dragAmount >= swipeThreshold -> onWeekChanged(selectedDate.minusWeeks(1))
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            sections.forEach { section ->
                item(key = "section-${section.key}") {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { sectionBounds[section] = it.boundsInRoot() },
                    ) {
                        SectionHeader(
                            title = section.title(),
                            tag = "section-header-${section.key}",
                        )

                        val items = workoutsBySection[section].orEmpty()

                        if (items.isEmpty()) {
                            EmptySectionRow()
                        } else {
                            items.forEachIndexed { index, workout ->
                                if (index > 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
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
                item(key = "divider-${section.key}") {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
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
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .testTag(tag),
    )
}

@Composable
private fun EmptySectionRow() {
    Text(
        text = stringResource(R.string.no_workouts),
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                        width = 2.dp,
                        color = colors.background.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.medium,
                    )
                } else {
                    Modifier
                },
            )
            .then(
                if (isDragging) {
                    Modifier
                        .height(0.dp)
                        .padding(0.dp)
                        .alpha(0f)
                } else {
                    Modifier
                        .padding(14.dp)
                        .alpha(1f)
                },
            )

    Box(modifier = rowModifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Row(
                verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(enabled = !workout.isRestDay) { onEdit() },
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = stringResource(R.string.drag_label),
                    tint = colors.content,
                    modifier =
                        Modifier
                            .padding(end = 12.dp)
                            .size(24.dp)
                            .clickable(enabled = false) {}
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    coordinates?.localToRoot(down.position)?.let {
                                        onDragStarted(it, itemBoundsHeight(coordinates))
                                    }
                                }
                            },
                )
                Column {
                    if (workout.isRestDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = colors.content,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.rest_day_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.content,
                            )
                        }
                    } else {
                        TypeChip(
                            label = workout.type,
                            containerColor = colors.content.copy(alpha = 0.18f),
                            contentColor = colors.content,
                        )
                    }

                    if (hasDescription) {
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = workout.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.content,
                        )
                    }
                }
            }

            if (!workout.isRestDay) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .offset(x = (-24).dp, y = (-2).dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (workout.isCompleted) {
                        Text(
                            text = "👍",
                            modifier =
                                Modifier
                                    .clickable { onToggleCompleted(false) }
                                    .size(26.dp),
                            fontSize = 22.sp,
                        )
                    } else {
                        Checkbox(
                            checked = workout.isCompleted,
                            onCheckedChange = onToggleCompleted,
                            modifier = Modifier.size(26.dp),
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
        }

        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.delete_workout),
            tint = colors.content,
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(14.dp)
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            width = 2.dp,
                            color = colors.background.copy(alpha = 0.6f),
                            shape = MaterialTheme.shapes.medium,
                        )
                    } else {
                        Modifier
                    },
                )
                .alpha(0.45f),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = if (hasDescription) Alignment.Top else Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = null,
                    tint = colors.content,
                    modifier =
                        Modifier
                            .padding(end = 12.dp)
                            .size(24.dp),
                )
                Column {
                    if (workout.isRestDay) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Bedtime,
                                contentDescription = null,
                                tint = colors.content,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.rest_day_label),
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.content,
                            )
                        }
                    } else {
                        TypeChip(
                            label = workout.type,
                            containerColor = colors.content.copy(alpha = 0.18f),
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
    return sectionBounds.entries.firstOrNull { it.value.contains(dropPosition) }?.key ?: fallback
}

private fun computeOrderForDrop(
    dropPosition: Offset,
    items: List<WorkoutUi>,
    draggedId: WorkoutId,
    itemBounds: Map<WorkoutId, Rect>,
): Int {
    val candidates = items.filterNot { it.id == draggedId }

    if (candidates.isEmpty()) return 0

    val sorted = candidates.sortedBy { it.order }
    val dropIndex =
        sorted.indexOfFirst { workout ->
            val bounds = itemBounds[workout.id] ?: return@indexOfFirst false
            dropPosition.y < bounds.center.y
        }

    return if (dropIndex == -1) sorted.size else dropIndex
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
    val newOrder = computeOrderForDrop(dragPosition, targetItems, workout.id, context.itemBounds)
    val newDay = targetSection.dayOfWeekOrNull()

    if (newDay != workout.dayOfWeek || newOrder != workout.order) {
        context.onWorkoutMoved(workout.id, newDay, newOrder)
    }
}

private sealed class SectionKey(val key: String) {
    object ToBeDefined : SectionKey("tbd")

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
private fun WeeklyTrainingContentPreview() {
    WeeklyTrainingContent(
        selectedDate = LocalDate.of(2026, 1, 15),
        workouts =
            listOf(
                WorkoutUi(
                    id = 1L,
                    dayOfWeek = null,
                    type = "Run",
                    description = "Easy 5k",
                    isCompleted = false,
                    isRestDay = false,
                    order = 0,
                ),
                WorkoutUi(
                    id = 2L,
                    dayOfWeek = DayOfWeek.MONDAY,
                    type = "Swim",
                    description = "Intervals 10x100",
                    isCompleted = false,
                    isRestDay = false,
                    order = 0,
                ),
                WorkoutUi(
                    id = 3L,
                    dayOfWeek = DayOfWeek.WEDNESDAY,
                    type = "Bike",
                    description = "Tempo 45 min",
                    isCompleted = true,
                    isRestDay = false,
                    order = 0,
                ),
            ),
        onWorkoutMoved = { _, _, _ -> },
        onWorkoutCompletionChanged = { _, _ -> },
        onWorkoutEdit = {},
        onWorkoutDelete = {},
    )
}
