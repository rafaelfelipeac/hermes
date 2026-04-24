package com.rafaelfelipeac.hermes.features.events.presentation.model

import java.time.LocalDate

data class EventDialogDraft(
    val eventId: Long?,
    val title: String,
    val description: String,
    val categoryId: Long?,
    val eventDate: LocalDate?,
)
