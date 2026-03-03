package com.rafaelfelipeac.hermes.features.weeklytraining.domain

import java.time.DayOfWeek
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val DAYS_IN_WEEK = 7L
private const val DAYS_IN_WEEK_INT = 7

fun weekStart(
    date: LocalDate,
    startDay: DayOfWeek,
): LocalDate {
    return date.with(TemporalAdjusters.previousOrSame(startDay))
}

fun weekDates(start: LocalDate): List<LocalDate> {
    return (0L until DAYS_IN_WEEK).map { offset -> start.plusDays(offset) }
}

fun orderedDays(startDay: DayOfWeek): List<DayOfWeek> {
    return (0L until DAYS_IN_WEEK).map { offset -> startDay.plus(offset) }
}

fun canonicalStorageWeekStart(date: LocalDate): LocalDate {
    return date.with(TemporalAdjusters.previousOrSame(MONDAY))
}

fun storageWeekStartsForDisplayWeek(displayWeekStart: LocalDate): List<LocalDate> {
    return weekDates(displayWeekStart)
        .map(::canonicalStorageWeekStart)
        .distinct()
}

fun displayDateForDay(
    displayWeekStart: LocalDate,
    displayStartDay: DayOfWeek,
    dayOfWeek: DayOfWeek,
): LocalDate {
    val offset = (dayOfWeek.value - displayStartDay.value + DAYS_IN_WEEK_INT) % DAYS_IN_WEEK_INT

    return displayWeekStart.plusDays(offset.toLong())
}
