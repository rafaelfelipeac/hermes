package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.time.Instant

data class ChallengeCalculationResult(
    val status: ChallengeStatus,
    val plannedTotal: Long,
    val expectedTotal: Long,
    val completedTotal: Long,
    val remainingTotal: Long,
    val carriedDebt: Long,
    val todayProgress: Long,
    val todayTarget: Long? = null,
    val todayRemaining: Long? = null,
    val requiredPace: Long,
    val visualProgress: Double,
    val firstCompletionAt: Instant? = null,
    val recoveredCompletionAt: Instant? = null,
)
