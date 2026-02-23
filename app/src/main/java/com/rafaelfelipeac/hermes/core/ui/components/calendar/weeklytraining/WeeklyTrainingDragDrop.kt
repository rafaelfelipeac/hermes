package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.AFTERNOON
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.MORNING
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot.NIGHT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek

private const val FIRST_LIST_INDEX = 0
private const val NO_INDEX = -1

internal data class DropContext(
    val workouts: List<WorkoutUi>,
    val workoutsBySection: Map<SectionKey, List<WorkoutUi>>,
    val sectionBounds: Map<SectionKey, Rect>,
    val dayUsesSlots: Map<DayOfWeek, Boolean>,
    val itemBounds: Map<WorkoutId, Rect>,
    val onWorkoutMoved: (WorkoutId, DayOfWeek?, TimeSlot?, Int) -> Unit,
)

internal fun handleDrop(
    draggedWorkoutId: WorkoutId,
    dragPosition: Offset,
    context: DropContext,
    targetSectionOverride: SectionKey? = null,
) {
    val workout = context.workouts.firstOrNull { it.id == draggedWorkoutId } ?: return
    val fallbackSection = workout.dayOfWeek.toSectionKey()
    val targetSection =
        targetSectionOverride ?: findTargetSection(dragPosition, context.sectionBounds, fallbackSection)
    val newDay = targetSection.dayOfWeekOrNull()
    val usesSlots = newDay?.let { day -> context.dayUsesSlots[day] == true } == true
    val newTimeSlot =
        when {
            newDay == null -> null
            !usesSlots -> null
            else -> slotFromDropPosition(dragPosition, context.sectionBounds[targetSection])
        }
    val targetItems =
        context.workoutsBySection[targetSection]
            .orEmpty()
            .filter { item ->
                if (!usesSlots) {
                    true
                } else {
                    (item.timeSlot ?: MORNING) == newTimeSlot
                }
            }
    val newOrder = computeOrderForDrop(dragPosition, targetItems, workout.id, context.itemBounds)

    if (newDay != workout.dayOfWeek || newTimeSlot != workout.timeSlot || newOrder != workout.order) {
        context.onWorkoutMoved(workout.id, newDay, newTimeSlot, newOrder)
    }
}

private fun slotFromDropPosition(
    dropPosition: Offset,
    sectionRect: Rect?,
): TimeSlot {
    if (sectionRect == null || sectionRect.height <= 0f) {
        return MORNING
    }
    val relativeY = (dropPosition.y - sectionRect.top).coerceIn(0f, sectionRect.height)
    val third = sectionRect.height / 3f

    return when {
        relativeY < third -> MORNING
        relativeY < third * 2f -> AFTERNOON
        else -> NIGHT
    }
}

internal fun findTargetSection(
    dropPosition: Offset,
    sectionBounds: Map<SectionKey, Rect>,
    fallback: SectionKey,
): SectionKey {
    val directMatch = sectionBounds.entries.firstOrNull { it.value.contains(dropPosition) }?.key

    val target =
        directMatch
            ?: if (sectionBounds.isEmpty()) {
                fallback
            } else {
                val ordered =
                    sectionBounds.entries
                        .sortedBy { it.value.top }
                        .map { it.key to it.value }
                val first = ordered.first()
                val last = ordered.last()
                val boundaryTarget =
                    ordered
                        .windowed(2)
                        .firstOrNull { (current, next) ->
                            val boundary = (current.second.bottom + next.second.top) / 2f
                            dropPosition.y <= boundary
                        }
                        ?.first()
                        ?.first

                when {
                    dropPosition.y <= first.second.top -> first.first
                    boundaryTarget != null -> boundaryTarget
                    else -> last.first
                }
            }

    return target
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

fun DayOfWeek?.toSectionKey(): SectionKey {
    return if (this == null) SectionKey.ToBeDefined else SectionKey.Day(this)
}
