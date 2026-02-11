package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
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
    targetSectionOverride: SectionKey? = null,
) {
    val workout = context.workouts.firstOrNull { it.id == draggedWorkoutId } ?: return
    val fallbackSection = workout.dayOfWeek.toSectionKey()
    val targetSection =
        targetSectionOverride ?: findTargetSection(dragPosition, context.sectionBounds, fallbackSection)
    val targetItems = context.workoutsBySection[targetSection].orEmpty()
    val newOrder =
        computeOrderForDrop(dragPosition, targetItems, workout.id, context.itemBounds)
    val newDay = targetSection.dayOfWeekOrNull()

    if (newDay != workout.dayOfWeek || newOrder != workout.order) {
        context.onWorkoutMoved(workout.id, newDay, newOrder)
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
