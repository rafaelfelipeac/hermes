package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.time.Instant
import java.time.LocalDate

data class ChallengeProgressEntry(
    val id: Long,
    val challengeId: Long,
    val quantity: Long,
    val entryDate: LocalDate,
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
)
