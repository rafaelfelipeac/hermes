package com.rafaelfelipeac.hermes.features.activity.presentation.model

import java.time.LocalDate

data class ActivityWeekFilterUi(
    val weekStartDate: LocalDate,
    val label: String,
    val isSelected: Boolean,
)
