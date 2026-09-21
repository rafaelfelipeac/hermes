package com.rafaelfelipeac.hermes.features.categories.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryDragRulesTest {
    @Test
    fun previewMovedCategory_movesItemToTargetIndex() {
        val categories = categoryList()

        val preview = categories.previewMovedCategory(categoryId = 1L, targetIndex = 2)

        assertEquals(listOf(2L, 3L, 1L), preview.map { it.id })
    }

    @Test
    fun previewMovedCategory_keepsListForInvalidMove() {
        val categories = categoryList()

        val preview = categories.previewMovedCategory(categoryId = 1L, targetIndex = 0)

        assertEquals(categories, preview)
    }

    @Test
    fun findCategoryDragTargetIndex_usesMeasuredRowCenters() {
        val rowBounds =
            listOf(
                CategoryDragRowBounds(1L, 0, Rect(0f, 20f, 100f, 80f)),
                CategoryDragRowBounds(2L, 1, Rect(0f, 100f, 100f, 180f)),
                CategoryDragRowBounds(3L, 2, Rect(0f, 220f, 100f, 300f)),
            )

        val targetIndex =
            findCategoryDragTargetIndex(
                dragPosition = Offset(50f, 190f),
                rowBounds = rowBounds,
                fallbackIndex = 0,
            )

        assertEquals(2, targetIndex)
    }

    @Test
    fun computeCategoryAutoScrollStep_scrollsNearBottom() {
        val step =
            computeCategoryAutoScrollStep(
                position = Offset(50f, 380f),
                context =
                    CategoryAutoScrollContext(
                        containerBounds = Rect(0f, 0f, 100f, 400f),
                        edge = 100f,
                        safePadding = 16f,
                        canScrollBackward = true,
                        canScrollForward = true,
                    ),
            )

        assertEquals(14.4f, step.scrollDelta, FLOAT_DELTA)
    }

    private fun categoryList(): List<CategoryUi> {
        return listOf(
            CategoryUi(id = 1L, name = "Run", colorId = "run", sortOrder = 0, isHidden = false, isSystem = true),
            CategoryUi(id = 2L, name = "Bike", colorId = "bike", sortOrder = 1, isHidden = false, isSystem = true),
            CategoryUi(id = 3L, name = "Swim", colorId = "swim", sortOrder = 2, isHidden = false, isSystem = true),
        )
    }

    private companion object {
        const val FLOAT_DELTA = 0.001f
    }
}
