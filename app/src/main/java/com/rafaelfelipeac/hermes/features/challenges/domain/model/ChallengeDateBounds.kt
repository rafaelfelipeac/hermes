package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.time.LocalDate

data class ChallengeDateBounds(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    val inclusiveDays: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
}
