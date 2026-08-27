@file:Suppress("ReturnCount")

package com.rafaelfelipeac.hermes.features.challenges.domain

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeDateBounds
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ChallengeCalculator {
    fun calculate(
        challenge: Challenge,
        progressEntries: List<ChallengeProgressEntry>,
        today: LocalDate,
    ): ChallengeCalculationResult {
        val bounds = ChallengeDateBounds(challenge.startDate, challenge.endDate)
        val orderedEntries =
            progressEntries
                .sortedWith(compareBy<ChallengeProgressEntry>({ it.entryDate }, { it.occurredAt }, { it.id }))

        val completedTotal =
            orderedEntries.fold(0L) { accumulator, entry ->
                ChallengeQuantity.add(accumulator, entry.quantity)
            }

        val expectedTotal = expectedTotal(challenge.targetQuantity, bounds, today)
        val remainingTotal =
            if (completedTotal >= challenge.targetQuantity) {
                0L
            } else {
                ChallengeQuantity.subtract(challenge.targetQuantity, completedTotal)
            }
        val carriedDebt =
            if (completedTotal >= expectedTotal) {
                0L
            } else {
                ChallengeQuantity.subtract(expectedTotal, completedTotal)
            }
        val todayProgress = orderedEntries.filter { it.entryDate == today }.sumOf { it.quantity }
        val requiredPace = requiredPace(challenge.targetQuantity, completedTotal, bounds, today)
        val firstCompletionAt = firstCompletionAt(challenge.targetQuantity, orderedEntries)
        val recoveredCompletionAt =
            if (firstCompletionAt != null && isRecoveredCompletion(bounds, orderedEntries, challenge.targetQuantity)) {
                firstCompletionAt
            } else {
                null
            }

        return ChallengeCalculationResult(
            status = statusFor(challenge, today, completedTotal, expectedTotal),
            expectedTotal = expectedTotal,
            completedTotal = completedTotal,
            remainingTotal = remainingTotal,
            carriedDebt = carriedDebt,
            todayProgress = todayProgress,
            requiredPace = requiredPace,
            visualProgress = ChallengeQuantity.cappedProgress(completedTotal, challenge.targetQuantity),
            firstCompletionAt = firstCompletionAt,
            recoveredCompletionAt = recoveredCompletionAt,
        )
    }

    private fun expectedTotal(
        targetQuantity: Long,
        bounds: ChallengeDateBounds,
        today: LocalDate,
    ): Long {
        val totalDays = bounds.inclusiveDays.coerceAtLeast(1L)
        val elapsedDays =
            when {
                today.isBefore(bounds.startDate) -> 0L
                today.isAfter(bounds.endDate) -> totalDays
                else -> ChronoUnit.DAYS.between(bounds.startDate, today) + 1
            }

        return ((targetQuantity * elapsedDays) / totalDays)
    }

    private fun requiredPace(
        targetQuantity: Long,
        completedTotal: Long,
        bounds: ChallengeDateBounds,
        today: LocalDate,
    ): Long {
        if (today.isAfter(bounds.endDate)) return 0L

        val remaining = (targetQuantity - completedTotal).coerceAtLeast(0L)
        val effectiveToday =
            when {
                today.isBefore(bounds.startDate) -> bounds.startDate
                today.isAfter(bounds.endDate) -> bounds.endDate
                else -> today
            }
        val remainingDays = ChronoUnit.DAYS.between(effectiveToday, bounds.endDate) + 1
        return if (remainingDays <= 0) {
            remaining
        } else {
            ChallengeQuantity.ceilDiv(remaining, remainingDays)
        }
    }

    private fun statusFor(
        challenge: Challenge,
        today: LocalDate,
        completedTotal: Long,
        expectedTotal: Long,
    ): ChallengeStatus {
        if (completedTotal <= 0L) return ChallengeStatus.NOT_STARTED
        if (completedTotal > challenge.targetQuantity) return ChallengeStatus.EXCEEDED
        if (completedTotal == challenge.targetQuantity) return ChallengeStatus.COMPLETED
        if (today.isAfter(challenge.endDate)) return ChallengeStatus.EXPIRED_INCOMPLETE
        return when {
            completedTotal > expectedTotal -> ChallengeStatus.AHEAD
            completedTotal == expectedTotal -> ChallengeStatus.ON_TRACK
            else -> ChallengeStatus.BEHIND
        }
    }

    private fun firstCompletionAt(
        targetQuantity: Long,
        entries: List<ChallengeProgressEntry>,
    ): Instant? {
        var total = 0L
        for (entry in entries) {
            total = ChallengeQuantity.add(total, entry.quantity)
            if (total >= targetQuantity) {
                return entry.occurredAt
            }
        }
        return null
    }

    private fun isRecoveredCompletion(
        bounds: ChallengeDateBounds,
        entries: List<ChallengeProgressEntry>,
        targetQuantity: Long,
    ): Boolean {
        var total = 0L
        var firstCompletionDate: LocalDate? = null
        val progressByDate = entries.groupBy { it.entryDate }
        val orderedDates = progressByDate.keys.sorted()

        orderedDates.forEach { date ->
            total = ChallengeQuantity.add(total, progressByDate.getValue(date).sumOf { it.quantity })
            if (total >= targetQuantity && firstCompletionDate == null) {
                firstCompletionDate = date
            }
        }

        val completionDate = firstCompletionDate ?: return false
        var runningTotal = 0L
        orderedDates.forEach { date ->
            if (date.isAfter(completionDate)) return@forEach
            runningTotal = ChallengeQuantity.add(runningTotal, progressByDate.getValue(date).sumOf { it.quantity })
            val scheduled = expectedForDate(bounds, targetQuantity, date)
            if (date.isBefore(completionDate) && runningTotal < scheduled) {
                return true
            }
        }

        return false
    }

    private fun expectedForDate(
        bounds: ChallengeDateBounds,
        targetQuantity: Long,
        date: LocalDate,
    ): Long {
        val totalDays = bounds.inclusiveDays.coerceAtLeast(1L)
        val elapsedDays =
            when {
                date.isBefore(bounds.startDate) -> 0L
                date.isAfter(bounds.endDate) -> totalDays
                else -> ChronoUnit.DAYS.between(bounds.startDate, date) + 1
            }
        return (targetQuantity * elapsedDays) / totalDays
    }
}

fun ChallengeCalculator.calculate(
    challenge: Challenge,
    progressEntries: List<ChallengeProgressEntry>,
    clock: Clock,
): ChallengeCalculationResult {
    return calculate(
        challenge = challenge,
        progressEntries = progressEntries,
        today = LocalDate.now(clock),
    )
}
