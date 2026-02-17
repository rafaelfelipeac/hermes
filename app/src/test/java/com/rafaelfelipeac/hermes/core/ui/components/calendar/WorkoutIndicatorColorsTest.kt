package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek.MONDAY

class WorkoutIndicatorColorsTest {
    @Test
    fun workoutIndicatorColor_usesCategoryColorForIncomplete() {
        val workout =
            WorkoutUi(
                id = 1L,
                dayOfWeek = MONDAY,
                type = "Run",
                description = "Easy",
                isCompleted = false,
                isRestDay = false,
                categoryId = 2L,
                categoryColorId = COLOR_RUN,
                categoryName = "Run",
                order = 0,
            )
        val surface = Color(0xFFF7F9FC)

        val color = workoutIndicatorColor(workout, isDarkTheme = false, surface = surface)

        assertEquals(categoryAccentColor(COLOR_RUN), color)
    }

    @Test
    fun workoutIndicatorColor_usesCompletedToneForCategory() {
        val workout =
            WorkoutUi(
                id = 2L,
                dayOfWeek = MONDAY,
                type = "Run",
                description = "Easy",
                isCompleted = true,
                isRestDay = false,
                categoryId = 2L,
                categoryColorId = COLOR_RUN,
                categoryName = "Run",
                order = 0,
            )
        val surface = Color(0xFFF7F9FC)
        val expected = lerp(categoryAccentColor(COLOR_RUN), surface, 0.16f)

        val color = workoutIndicatorColor(workout, isDarkTheme = false, surface = surface)

        assertEquals(expected, color)
    }

    @Test
    fun workoutIndicatorColor_usesRestDayColor() {
        val workout =
            WorkoutUi(
                id = 3L,
                dayOfWeek = MONDAY,
                type = "",
                description = "",
                isCompleted = false,
                isRestDay = true,
                categoryId = null,
                categoryColorId = null,
                categoryName = null,
                order = 0,
            )
        val surface = Color(0xFFF7F9FC)

        assertEquals(
            RestDaySurfaceLight,
            workoutIndicatorColor(workout, isDarkTheme = false, surface = surface),
        )
        assertEquals(
            RestDaySurfaceDark,
            workoutIndicatorColor(workout, isDarkTheme = true, surface = surface),
        )
    }
}
