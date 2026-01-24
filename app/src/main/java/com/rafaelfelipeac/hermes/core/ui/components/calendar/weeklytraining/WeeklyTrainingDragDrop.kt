package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.model.WorkoutUi
import java.time.DayOfWeek

private const val FIRST_LIST_INDEX = 0
private const val NO_INDEX = -1

internal data class DropContext(
    val workouts: List<WorkoutUi>,
    val workoutsBySection: Map<SectionKey, List<WorkoutUi>>,
    val sectionBounds: Map<SectionKey, Rect>,
    val itemBounds: Map<WorkoutId, Rect>,
    val onWorkoutMoved: (WorkoutId, DayOfWeek?, Int) -> Unit,
)

internal fun handleDrop(
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

    if (candidates.isEmpty()) return FIRST_LIST_INDEX

    val sorted = candidates.sortedBy { it.order }
    val dropIndex =
        sorted.indexOfFirst { workout ->
            val bounds = itemBounds[workout.id] ?: return@indexOfFirst false
            dropPosition.y < bounds.center.y
        }

    return if (dropIndex == NO_INDEX) sorted.size else dropIndex
}

private fun DayOfWeek?.toSectionKey(): SectionKey {
    return if (this == null) SectionKey.ToBeDefined else SectionKey.Day(this)
}
