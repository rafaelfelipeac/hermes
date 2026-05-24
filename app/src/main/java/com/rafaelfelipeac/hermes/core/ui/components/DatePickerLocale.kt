package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.material3.DatePickerState
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import java.util.Locale

internal fun Locale.withWeekStartDay(weekStartDay: WeekStartDay): Locale {
    val weekStartKeyword =
        when (weekStartDay) {
            WeekStartDay.MONDAY -> "mon"
            WeekStartDay.TUESDAY -> "tue"
            WeekStartDay.WEDNESDAY -> "wed"
            WeekStartDay.THURSDAY -> "thu"
            WeekStartDay.FRIDAY -> "fri"
            WeekStartDay.SATURDAY -> "sat"
            WeekStartDay.SUNDAY -> "sun"
        }

    return runCatching {
        Locale.Builder()
            .setLocale(this)
            .setUnicodeLocaleKeyword("fw", weekStartKeyword)
            .build()
    }.getOrElse { this }
}

internal fun DatePickerState.applyWeekStartDayOverride(weekStartDay: WeekStartDay) {
    runCatching {
        val stateClass = javaClass
        val calendarModelField =
            stateClass.findDeclaredField("calendarModel") ?: return@runCatching
        calendarModelField.isAccessible = true
        val calendarModel = calendarModelField.get(this) ?: return@runCatching
        val firstDayField =
            calendarModel.javaClass.findDeclaredField("firstDayOfWeek") ?: return@runCatching
        firstDayField.isAccessible = true
        firstDayField.setInt(calendarModel, weekStartDay.dayOfWeek.value)
        // Force the cached month to recalculate against the updated week start.
        displayedMonthMillis = displayedMonthMillis
    }
}

private fun Class<*>.findDeclaredField(name: String) =
    generateSequence(this) { it.superclass }
        .mapNotNull { clazz -> runCatching { clazz.getDeclaredField(name) }.getOrNull() }
        .firstOrNull()
