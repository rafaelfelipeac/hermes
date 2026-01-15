package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate

typealias WorkoutId = Long

data class WorkoutUi(
    val id: WorkoutId,
    val dayOfWeek: DayOfWeek?,
    val type: String,
    val description: String,
    val isCompleted: Boolean,
    val order: Int
)

@Composable
fun WeeklyTrainingContent(
    selectedWeekStartDate: LocalDate,
    workouts: List<WorkoutUi>,
    onAddWorkout: () -> Unit,
    onWorkoutMoved: (WorkoutId, DayOfWeek?, Int) -> Unit,
    onWorkoutCompletionChanged: (WorkoutId, Boolean) -> Unit,
    onDominantDayChanged: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val sections = remember {
        listOf(
            SectionKey.ToBeDefined,
            SectionKey.Day(DayOfWeek.MONDAY),
            SectionKey.Day(DayOfWeek.TUESDAY),
            SectionKey.Day(DayOfWeek.WEDNESDAY),
            SectionKey.Day(DayOfWeek.THURSDAY),
            SectionKey.Day(DayOfWeek.FRIDAY),
            SectionKey.Day(DayOfWeek.SATURDAY),
            SectionKey.Day(DayOfWeek.SUNDAY)
        )
    }

    val sectionBounds = remember { mutableStateMapOf<SectionKey, Rect>() }
    val itemBounds = remember { mutableStateMapOf<WorkoutId, Rect>() }
    var draggedWorkoutId by remember { mutableStateOf<WorkoutId?>(null) }

    val workoutsBySection = remember(workouts) {
        sections.associateWith { section ->
            workouts
                .filter { it.dayOfWeek == section.dayOfWeekOrNull() }
                .sortedBy { it.order }
        }
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "add-workout") {
            Button(onClick = onAddWorkout) {
                Text(text = "Add workout")
            }
        }

        sections.forEach { section ->
            item(key = "section-${section.key}") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { sectionBounds[section] = it.boundsInRoot() }
                ) {
                    SectionHeader(title = section.title())

                    val items = workoutsBySection[section].orEmpty()
                    if (items.isEmpty()) {
                        EmptySectionRow()
                    } else {
                        items.forEach { workout ->
                            WorkoutRow(
                                workout = workout,
                                isDragging = draggedWorkoutId == workout.id,
                                onToggleCompleted = { checked ->
                                    onWorkoutCompletionChanged(workout.id, checked)
                                },
                                onDrop = { dropPosition ->
                                    val targetSection = findTargetSection(
                                        dropPosition,
                                        sectionBounds,
                                        section
                                    )
                                    val targetItems = workoutsBySection[targetSection].orEmpty()
                                    val newOrder = computeOrderForDrop(
                                        dropPosition,
                                        targetItems,
                                        workout.id,
                                        itemBounds
                                    )
                                    val newDay = targetSection.dayOfWeekOrNull()
                                    if (newDay != workout.dayOfWeek || newOrder != workout.order) {
                                        onWorkoutMoved(workout.id, newDay, newOrder)
                                    }
                                },
                                onDragStarted = { draggedWorkoutId = workout.id },
                                onDragEnded = {
                                    draggedWorkoutId = null
                                },
                                onItemPositioned = { itemBounds[workout.id] = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
    )
}

@Composable
private fun EmptySectionRow() {
    Text(
        text = "No workouts",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun WorkoutRow(
    workout: WorkoutUi,
    isDragging: Boolean,
    onToggleCompleted: (Boolean) -> Unit,
    onDrop: (Offset) -> Unit,
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    onItemPositioned: (Rect) -> Unit
) {
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var lastDragPosition by remember { mutableStateOf<Offset?>(null) }
    val rowModifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned {
            coordinates = it
            onItemPositioned(it.boundsInRoot())
        }
        .background(
            if (isDragging) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        )
        .padding(12.dp)

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Drag",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(32.dp)
                    .clickable(enabled = false) {}
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                onDragStarted()
                                coordinates?.localToRoot(offset)?.let { lastDragPosition = it }
                            },
                            onDragEnd = {
                                val dropPosition = lastDragPosition
                                    ?: dragPositionFromCoordinates(coordinates)
                                if (dropPosition != null) {
                                    onDrop(dropPosition)
                                }
                                onDragEnded()
                            },
                            onDragCancel = onDragEnded,
                            onDrag = { change, _ ->
                                coordinates?.localToRoot(change.position)?.let {
                                    lastDragPosition = it
                                }
                            }
                        )
                    }
            )
            Column {
                Text(
                    text = workout.type,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Checkbox(
            checked = workout.isCompleted,
            onCheckedChange = onToggleCompleted
        )
    }
}

private fun findTargetSection(
    dropPosition: Offset,
    sectionBounds: Map<SectionKey, Rect>,
    fallback: SectionKey
): SectionKey {
    return sectionBounds.entries.firstOrNull { it.value.contains(dropPosition) }?.key ?: fallback
}

private fun computeOrderForDrop(
    dropPosition: Offset,
    items: List<WorkoutUi>,
    draggedId: WorkoutId,
    itemBounds: Map<WorkoutId, Rect>
): Int {
    val candidates = items.filterNot { it.id == draggedId }
    if (candidates.isEmpty()) return 0

    val sorted = candidates.sortedBy { it.order }
    val dropIndex = sorted.indexOfFirst { workout ->
        val bounds = itemBounds[workout.id] ?: return@indexOfFirst false
        dropPosition.y < bounds.center.y
    }
    return if (dropIndex == -1) sorted.size else dropIndex
}

private fun dragPositionFromCoordinates(coordinates: LayoutCoordinates?): Offset? {
    return coordinates?.boundsInRoot()?.center
}

private sealed class SectionKey(val key: String) {
    object ToBeDefined : SectionKey("tbd")
    data class Day(val dayOfWeek: DayOfWeek) : SectionKey(dayOfWeek.name)
}

private fun SectionKey.title(): String {
    return when (this) {
        SectionKey.ToBeDefined -> "To be defined"
        is SectionKey.Day -> dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
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
        selectedWeekStartDate = LocalDate.of(2026, 1, 12),
        workouts = listOf(
            WorkoutUi(
                id = 1L,
                dayOfWeek = null,
                type = "Run",
                description = "Easy 5k",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = 2L,
                dayOfWeek = DayOfWeek.MONDAY,
                type = "Swim",
                description = "Intervals 10x100",
                isCompleted = false,
                order = 0
            ),
            WorkoutUi(
                id = 3L,
                dayOfWeek = DayOfWeek.WEDNESDAY,
                type = "Bike",
                description = "Tempo 45 min",
                isCompleted = true,
                order = 0
            )
        ),
        onAddWorkout = {},
        onWorkoutMoved = { _, _, _ -> },
        onWorkoutCompletionChanged = { _, _ -> }
    )
}
