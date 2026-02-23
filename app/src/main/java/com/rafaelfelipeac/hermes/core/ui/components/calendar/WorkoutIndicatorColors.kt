package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_DARK
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_LIGHT
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.WORKOUT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

fun workoutIndicatorColor(
    workout: WorkoutUi,
    isDarkTheme: Boolean,
    surface: Color,
): Color {
    if (workout.eventType != WORKOUT) {
        return if (isDarkTheme) RestDaySurfaceDark else RestDaySurfaceLight
    }

    val categoryAccent =
        workout.categoryColorId?.let { accent ->
            baseCategoryColor(accent = categoryAccentColor(accent))
        }

    return when {
        workout.isCompleted && categoryAccent == null -> TodoBlue
        workout.isCompleted && categoryAccent != null ->
            completedCategoryColor(
                accent = categoryAccent,
                isDarkTheme = isDarkTheme,
                surface = surface,
            )
        categoryAccent != null -> categoryAccent
        else -> CompletedBlue
    }
}

fun completedCategoryColor(
    accent: Color,
    isDarkTheme: Boolean,
    surface: Color,
): Color {
    val base = baseCategoryColor(accent = accent)
    val extraBlend = if (isDarkTheme) INDICATOR_EXTRA_BLEND_DARK else INDICATOR_EXTRA_BLEND_LIGHT
    return lerp(base, surface, extraBlend)
}

fun baseCategoryColor(accent: Color): Color {
    return accent
}
