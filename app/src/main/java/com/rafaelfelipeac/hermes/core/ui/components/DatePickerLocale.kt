package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.material3.DatePickerState
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay

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
    }
}

private fun Class<*>.findDeclaredField(name: String) =
    generateSequence(this) { it.superclass }
        .mapNotNull { clazz -> runCatching { clazz.getDeclaredField(name) }.getOrNull() }
        .firstOrNull()
