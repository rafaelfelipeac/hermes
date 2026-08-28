package com.rafaelfelipeac.hermes.features.challenges.domain.model

import java.time.LocalDate

data class ChallengeEditorState(
    val challengeId: Long? = null,
    val title: String = "",
    val description: String = "",
    val targetType: ChallengeTargetType = ChallengeTargetType.DAILY,
    val targetQuantityText: String = "",
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val lifecycle: ChallengeLifecycle = ChallengeLifecycle.ACTIVE,
    val isDirty: Boolean = false,
    val validationMessage: String? = null,
)
