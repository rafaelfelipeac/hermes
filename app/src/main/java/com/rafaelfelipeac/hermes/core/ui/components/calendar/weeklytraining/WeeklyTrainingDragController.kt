package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Rect.Companion.Zero
import androidx.compose.ui.input.pointer.PointerId
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutId

private const val AUTO_SCROLL_MAX_SPEED = 18f

@Stable
internal class WeeklyTrainingDragController {
    var draggedWorkoutId by mutableStateOf<WorkoutId?>(null)
        private set
    var dragPosition by mutableStateOf<Offset?>(null)
        private set
    var draggedItemHeight by mutableFloatStateOf(0f)
        private set
    var dragPointerId by mutableStateOf<PointerId?>(null)
        private set
    var liveDropPreview by mutableStateOf<DropPreview?>(null)
    var containerBounds by mutableStateOf(Zero)
        private set
    var hoveredSection by mutableStateOf<SectionKey?>(null)
    var dragAmount by mutableFloatStateOf(0f)

    fun startDrag(
        workoutId: WorkoutId,
        position: Offset,
        itemHeight: Float,
    ) {
        if (draggedWorkoutId != null) return

        draggedWorkoutId = workoutId
        dragPosition = position
        draggedItemHeight = itemHeight
        dragPointerId = null
    }

    fun updateContainerBounds(bounds: Rect) {
        containerBounds = bounds
    }

    fun updateDragPosition(position: Offset?) {
        dragPosition = position
    }

    fun updateDragPointer(pointerId: PointerId?) {
        dragPointerId = pointerId
    }

    fun clearPointerTracking() {
        dragPointerId = null
        liveDropPreview = null
    }

    fun resetSwipeDrag() {
        dragAmount = 0f
    }

    fun clearDrag() {
        draggedWorkoutId = null
        dragPosition = null
        draggedItemHeight = 0f
        hoveredSection = null
        dragPointerId = null
        liveDropPreview = null
    }
}

internal data class AutoScrollStep(
    val clampedPosition: Offset,
    val scrollDelta: Float,
)

@Composable
internal fun rememberWeeklyTrainingDragController(): WeeklyTrainingDragController {
    return remember { WeeklyTrainingDragController() }
}

internal fun computeAutoScrollStep(
    position: Offset,
    containerBounds: Rect,
    edge: Float,
    safePadding: Float,
    canScrollBackward: Boolean,
    canScrollForward: Boolean,
): AutoScrollStep {
    val safeTop = containerBounds.top + safePadding
    val safeBottom = containerBounds.bottom - safePadding
    val clampedPosition =
        Offset(
            position.x,
            position.y.coerceIn(safeTop, safeBottom),
        )

    val distanceToTop = clampedPosition.y - containerBounds.top
    val distanceToBottom = containerBounds.bottom - clampedPosition.y
    val scrollDelta =
        when {
            distanceToTop < edge && canScrollBackward -> {
                -AUTO_SCROLL_MAX_SPEED * (1f - (distanceToTop / edge))
            }

            distanceToBottom < edge && canScrollForward -> {
                AUTO_SCROLL_MAX_SPEED * (1f - (distanceToBottom / edge))
            }

            else -> 0f
        }

    return AutoScrollStep(
        clampedPosition = clampedPosition,
        scrollDelta = scrollDelta,
    )
}
