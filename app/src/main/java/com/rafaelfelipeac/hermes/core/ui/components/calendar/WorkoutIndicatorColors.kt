package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

fun workoutIndicatorColor(
    workout: WorkoutUi,
    isDarkTheme: Boolean,
    surface: Color,
): Color {
    if (workout.isRestDay) {
        return if (isDarkTheme) RestDaySurfaceDark else RestDaySurfaceLight
    }

    val categoryAccent =
        workout.categoryColorId?.let { accent ->
            baseCategoryColor(
                accent = categoryAccentColor(accent),
                isDarkTheme = isDarkTheme,
                surface = surface,
            )
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
    val base = baseCategoryColor(accent = accent, isDarkTheme = isDarkTheme, surface = surface)
    val extraBlend = if (isDarkTheme) 0.12f else 0.16f
    return lerp(base, surface, extraBlend)
}

fun baseCategoryColor(
    accent: Color,
    isDarkTheme: Boolean,
    surface: Color,
): Color {
    return accent
}
