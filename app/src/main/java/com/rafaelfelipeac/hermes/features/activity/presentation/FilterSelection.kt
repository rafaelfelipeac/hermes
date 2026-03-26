package com.rafaelfelipeac.hermes.features.activity.presentation

import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import java.time.LocalDate

data class FilterSelection(
    val primaryFilter: ActivityPrimaryFilter,
    val categoryId: Long?,
    val weekStartDate: LocalDate?,
)
