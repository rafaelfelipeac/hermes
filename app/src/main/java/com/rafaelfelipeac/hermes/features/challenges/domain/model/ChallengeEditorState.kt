package com.rafaelfelipeac.hermes.features.challenges.domain.model

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import java.time.LocalDate

data class ChallengeEditorState(
    val challengeId: Long? = null,
    val categoryId: Long? = null,
    val title: String = EMPTY,
    val description: String = EMPTY,
    val targetType: ChallengeTargetType = ChallengeTargetType.DAILY,
    val targetQuantityText: String = EMPTY,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val lifecycle: ChallengeLifecycle = ChallengeLifecycle.ACTIVE,
    val isDirty: Boolean = false,
    val validationMessage: String? = null,
)
