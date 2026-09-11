package com.rafaelfelipeac.hermes.features.challenges.presentation

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry

internal data class ChallengeCalculationState(
    val activeChallenges: List<Challenge>,
    val archivedChallenges: List<Challenge>,
    val allChallenges: List<Challenge>,
    val allProgressEntries: List<ChallengeProgressEntry>,
    val calculations: Map<Long, ChallengeCalculationResult>,
)
