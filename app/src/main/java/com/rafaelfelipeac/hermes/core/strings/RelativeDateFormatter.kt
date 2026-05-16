package com.rafaelfelipeac.hermes.core.strings

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Suppress("LongParameterList")
fun relativeDateText(
    date: LocalDate,
    today: LocalDate,
    todayLabel: String,
    tomorrowLabel: String,
    yesterdayLabel: String,
    formatter: DateTimeFormatter,
): String {
    return when (date) {
        today -> todayLabel
        today.plusDays(1) -> tomorrowLabel
        today.minusDays(1) -> yesterdayLabel
        else -> date.format(formatter)
    }
}

fun relativeDaysUntilText(
    daysUntil: Int,
    todayLabel: String,
    tomorrowLabel: String,
    yesterdayLabel: String,
    fallbackText: String,
): String {
    return when (daysUntil) {
        0 -> todayLabel
        1 -> tomorrowLabel
        -1 -> yesterdayLabel
        else -> fallbackText
    }
}
