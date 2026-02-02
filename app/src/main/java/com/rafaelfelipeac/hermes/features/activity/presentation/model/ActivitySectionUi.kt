package com.rafaelfelipeac.hermes.features.activity.presentation.model

import java.time.LocalDate

data class ActivitySectionUi(
    val date: LocalDate,
    val items: List<ActivityItemUi>,
)
