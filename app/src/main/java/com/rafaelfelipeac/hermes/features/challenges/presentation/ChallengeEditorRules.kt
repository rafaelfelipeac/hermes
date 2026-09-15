package com.rafaelfelipeac.hermes.features.challenges.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun canEditChallengeProgress(
    challenge: Challenge,
    entryDate: LocalDate,
    today: LocalDate,
): Boolean {
    return challenge.lifecycle == ChallengeLifecycle.ACTIVE &&
        entryDate <= today &&
        !entryDate.isBefore(challenge.startDate) &&
        !entryDate.isAfter(challenge.endDate)
}

internal fun isPlannedChallengeTargetSafe(
    targetType: ChallengeTargetType,
    targetQuantity: Long,
    startDate: LocalDate,
    endDate: LocalDate,
): Boolean {
    if (targetType == ChallengeTargetType.TOTAL) return true

    val inclusiveDays = ChronoUnit.DAYS.between(startDate, endDate) + 1L
    return runCatching { ChallengeQuantity.multiply(targetQuantity, inclusiveDays) }.isSuccess
}

internal fun isChallengeProgressTotalSafe(
    entries: List<ChallengeProgressEntry>,
    challengeId: Long,
    quantity: Long,
    replacedEntryId: Long? = null,
): Boolean {
    return runCatching {
        val existingTotal =
            entries
                .asSequence()
                .filter { it.challengeId == challengeId && it.id != replacedEntryId }
                .fold(0L) { total, entry -> ChallengeQuantity.add(total, entry.quantity) }
        ChallengeQuantity.add(existingTotal, quantity)
    }.isSuccess
}

internal fun defaultChallengeEditorState(today: LocalDate): ChallengeEditorState {
    return ChallengeEditorState(
        challengeId = null,
        categoryId = null,
        title = EMPTY,
        description = EMPTY,
        targetType = ChallengeTargetType.DAILY,
        targetQuantityText = EMPTY,
        startDate = today,
        endDate = today.plusDays(29),
        lifecycle = ChallengeLifecycle.ACTIVE,
        isDirty = false,
        validationMessage = null,
    )
}
