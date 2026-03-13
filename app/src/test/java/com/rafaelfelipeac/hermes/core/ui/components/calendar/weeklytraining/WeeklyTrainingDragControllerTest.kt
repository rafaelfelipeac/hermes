package com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyTrainingDragControllerTest {
    @Test
    fun computeAutoScrollStep_clampsDragAndScrollsNearTopEdge() {
        val result =
            computeAutoScrollStep(
                position = Offset(x = 24f, y = 2f),
                context =
                    AutoScrollContext(
                        containerBounds = Rect(left = 0f, top = 0f, right = 100f, bottom = 200f),
                        edge = 40f,
                        safePadding = 16f,
                        canScrollBackward = true,
                        canScrollForward = true,
                    ),
            )

        assertEquals(Offset(24f, 16f), result.clampedPosition)
        assertEquals(-10.8f, result.scrollDelta, 0.001f)
    }
}
