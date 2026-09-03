package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.time.Instant
import java.time.LocalDate

data class Challenge(
    val id: Long,
    val categoryId: Long? = null,
    val title: String,
    val description: String? = null,
    val targetType: ChallengeTargetType,
    val targetQuantity: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val lifecycle: ChallengeLifecycle,
    val archivedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)
