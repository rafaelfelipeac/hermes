package com.rafaelfelipeac.hermes.features.categories.presentation

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
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi

private const val NO_INDEX = -1
private const val AUTO_SCROLL_MAX_SPEED = 18f

@Stable
internal class CategoryDragController {
    var draggedCategoryId by mutableStateOf<Long?>(null)
        private set
    var dragPosition by mutableStateOf<Offset?>(null)
        private set
    var dragTouchOffset by mutableStateOf(Offset.Zero)
        private set
    var draggedItemHeight by mutableFloatStateOf(0f)
        private set
    var draggedItemWidth by mutableFloatStateOf(0f)
        private set
    var targetIndex by mutableStateOf<Int?>(null)
    var rowBoundsSnapshot by mutableStateOf<List<CategoryDragRowBounds>>(emptyList())
        private set
    var containerBounds by mutableStateOf(Zero)
        private set

    fun startDrag(
        categoryId: Long,
        position: Offset,
        touchOffset: Offset,
        itemBounds: Rect,
        initialTargetIndex: Int,
        rowBounds: List<CategoryDragRowBounds>,
    ): Boolean {
        if (draggedCategoryId != null) return false

        draggedCategoryId = categoryId
        dragPosition = position
        dragTouchOffset = touchOffset
        draggedItemHeight = itemBounds.height
        draggedItemWidth = itemBounds.width
        targetIndex = initialTargetIndex
        rowBoundsSnapshot = rowBounds

        return true
    }

    fun updateContainerBounds(bounds: Rect) {
        containerBounds = bounds
    }

    fun updateDragPosition(position: Offset?) {
        dragPosition = position
    }

    fun clearDrag() {
        draggedCategoryId = null
        dragPosition = null
        dragTouchOffset = Offset.Zero
        draggedItemHeight = 0f
        draggedItemWidth = 0f
        targetIndex = null
        rowBoundsSnapshot = emptyList()
    }
}

internal data class CategoryDragRowBounds(
    val categoryId: Long,
    val index: Int,
    val bounds: Rect,
)

internal data class CategoryAutoScrollStep(
    val clampedPosition: Offset,
    val scrollDelta: Float,
)

internal data class CategoryAutoScrollContext(
    val containerBounds: Rect,
    val edge: Float,
    val safePadding: Float,
    val canScrollBackward: Boolean,
    val canScrollForward: Boolean,
)

@Composable
internal fun rememberCategoryDragController(): CategoryDragController {
    return remember { CategoryDragController() }
}

internal fun List<CategoryUi>.previewMovedCategory(
    categoryId: Long,
    targetIndex: Int,
): List<CategoryUi> {
    val currentIndex = indexOfFirst { it.id == categoryId }
    if (currentIndex == NO_INDEX || targetIndex !in indices || currentIndex == targetIndex) return this

    return toMutableList().apply {
        val category = removeAt(currentIndex)
        add(targetIndex, category)
    }
}

internal fun findCategoryDragTargetIndex(
    dragPosition: Offset,
    rowBounds: Collection<CategoryDragRowBounds>,
    fallbackIndex: Int,
): Int {
    if (rowBounds.isEmpty()) return fallbackIndex

    val sortedBounds = rowBounds.sortedBy { it.index }
    val first = sortedBounds.first()
    val last = sortedBounds.last()
    val y = dragPosition.y

    if (y <= first.bounds.center.y) return first.index
    if (y >= last.bounds.center.y) return last.index

    return sortedBounds.firstOrNull { bounds ->
        y <= bounds.bounds.center.y
    }?.index ?: fallbackIndex
}

internal fun computeCategoryAutoScrollStep(
    position: Offset,
    context: CategoryAutoScrollContext,
): CategoryAutoScrollStep {
    val containerBounds = context.containerBounds
    val effectivePadding =
        if (containerBounds.height <= 0f) {
            0f
        } else {
            minOf(context.safePadding, containerBounds.height / 2f)
        }
    val safeTop = containerBounds.top + effectivePadding
    val safeBottom = containerBounds.bottom - effectivePadding
    val clampedPosition =
        Offset(
            x = position.x,
            y = position.y.coerceIn(safeTop, safeBottom),
        )

    val distanceToTop = clampedPosition.y - containerBounds.top
    val distanceToBottom = containerBounds.bottom - clampedPosition.y
    val scrollDelta =
        when {
            distanceToTop < context.edge && context.canScrollBackward -> {
                -AUTO_SCROLL_MAX_SPEED * (1f - (distanceToTop / context.edge))
            }

            distanceToBottom < context.edge && context.canScrollForward -> {
                AUTO_SCROLL_MAX_SPEED * (1f - (distanceToBottom / context.edge))
            }

            else -> 0f
        }

    return CategoryAutoScrollStep(
        clampedPosition = clampedPosition,
        scrollDelta = scrollDelta,
    )
}
