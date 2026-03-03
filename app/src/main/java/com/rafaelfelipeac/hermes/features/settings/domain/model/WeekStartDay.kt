package com.rafaelfelipeac.hermes.features.settings.domain.model

import java.time.DayOfWeek

enum class WeekStartDay(
    val dayOfWeek: DayOfWeek,
) {
    MONDAY(DayOfWeek.MONDAY),
    TUESDAY(DayOfWeek.TUESDAY),
    WEDNESDAY(DayOfWeek.WEDNESDAY),
    THURSDAY(DayOfWeek.THURSDAY),
    FRIDAY(DayOfWeek.FRIDAY),
    SATURDAY(DayOfWeek.SATURDAY),
    SUNDAY(DayOfWeek.SUNDAY),
    ;

    companion object {
        fun fromStoredValue(raw: String?): WeekStartDay {
            return raw
                ?.let { value -> runCatching { valueOf(value) }.getOrNull() }
                ?: MONDAY
        }
    }
}
