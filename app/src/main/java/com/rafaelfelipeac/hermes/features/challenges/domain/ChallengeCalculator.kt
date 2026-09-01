@file:Suppress("ReturnCount")

package com.rafaelfelipeac.hermes.features.challenges.domain

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeCalculationResult
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeDateBounds
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ChallengeCalculator {
    @Suppress("LongMethod")
    fun calculate(
        challenge: Challenge,
        progressEntries: List<ChallengeProgressEntry>,
        today: LocalDate,
    ): ChallengeCalculationResult {
        val bounds = ChallengeDateBounds(challenge.startDate, challenge.endDate)
        val totalDays = bounds.inclusiveDays.coerceAtLeast(1L)
        val orderedEntries =
            progressEntries
                .sortedWith(compareBy<ChallengeProgressEntry>({ it.entryDate }, { it.occurredAt }, { it.id }))

        val completedTotal =
            orderedEntries.fold(0L) { accumulator, entry ->
                ChallengeQuantity.add(accumulator, entry.quantity)
            }
        val plannedTotal = plannedTotal(challenge, totalDays)
        val elapsedDays = elapsedDays(bounds, today)
        val priorElapsedDays = elapsedDays(bounds, today, includeToday = false)
        val expectedTotal = expectedTotal(challenge, totalDays, elapsedDays)
        val expectedBeforeToday = expectedTotal(challenge, totalDays, priorElapsedDays)

        val remainingTotal =
            if (completedTotal >= plannedTotal) {
                0L
            } else {
                ChallengeQuantity.subtract(plannedTotal, completedTotal)
            }
        val carriedDebt =
            if (completedTotal >= expectedBeforeToday) {
                0L
            } else {
                ChallengeQuantity.subtract(expectedBeforeToday, completedTotal)
            }
        val todayProgress = orderedEntries.filter { it.entryDate == today }.sumOf { it.quantity }
        val todayTarget =
            when (challenge.targetType) {
                ChallengeTargetType.DAILY -> challenge.targetQuantity
                ChallengeTargetType.TOTAL -> null
            }
        val requiredPace = requiredPace(plannedTotal, completedTotal, bounds, today)
        val todayRemaining =
            if (today.isBefore(bounds.startDate) || today.isAfter(bounds.endDate)) {
                todayTarget
            } else {
                val effectiveTodayTarget =
                    when (challenge.targetType) {
                        ChallengeTargetType.DAILY -> maxOf(challenge.targetQuantity, requiredPace)
                        ChallengeTargetType.TOTAL -> requiredPace
                    }
                (effectiveTodayTarget - todayProgress).coerceAtLeast(0L)
            }
        val firstCompletionAt = firstCompletionAt(plannedTotal, orderedEntries)
        val recoveredCompletionAt =
            if (
                firstCompletionAt != null &&
                isRecoveredCompletion(bounds, orderedEntries, plannedTotal, challenge.targetType)
            ) {
                firstCompletionAt
            } else {
                null
            }

        return ChallengeCalculationResult(
            status = statusFor(challenge, today, completedTotal, plannedTotal, expectedTotal),
            plannedTotal = plannedTotal,
            expectedTotal = expectedTotal,
            completedTotal = completedTotal,
            remainingTotal = remainingTotal,
            carriedDebt = carriedDebt,
            todayProgress = todayProgress,
            todayTarget = todayTarget,
            todayRemaining = todayRemaining,
            requiredPace = requiredPace,
            visualProgress = ChallengeQuantity.cappedProgress(completedTotal, plannedTotal),
            firstCompletionAt = firstCompletionAt,
            recoveredCompletionAt = recoveredCompletionAt,
        )
    }

    private fun plannedTotal(
        challenge: Challenge,
        totalDays: Long,
    ): Long {
        return when (challenge.targetType) {
            ChallengeTargetType.DAILY -> ChallengeQuantity.multiply(challenge.targetQuantity, totalDays)
            ChallengeTargetType.TOTAL -> challenge.targetQuantity
        }
    }

    private fun expectedTotal(
        challenge: Challenge,
        totalDays: Long,
        elapsedDays: Long,
    ): Long {
        return when (challenge.targetType) {
            ChallengeTargetType.DAILY -> ChallengeQuantity.multiply(challenge.targetQuantity, elapsedDays)
            ChallengeTargetType.TOTAL ->
                ChallengeQuantity.multiplyAndDivideFloor(
                    challenge.targetQuantity,
                    elapsedDays,
                    totalDays,
                )
        }
    }

    private fun requiredPace(
        plannedTotal: Long,
        completedTotal: Long,
        bounds: ChallengeDateBounds,
        today: LocalDate,
    ): Long {
        if (today.isAfter(bounds.endDate)) return 0L

        val remaining = (plannedTotal - completedTotal).coerceAtLeast(0L)
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
        plannedTotal: Long,
        expectedTotal: Long,
    ): ChallengeStatus {
        if (today.isBefore(challenge.startDate)) return ChallengeStatus.NOT_STARTED
        if (completedTotal > plannedTotal) return ChallengeStatus.EXCEEDED
        if (completedTotal == plannedTotal) return ChallengeStatus.COMPLETED
        if (today.isAfter(challenge.endDate)) return ChallengeStatus.EXPIRED_INCOMPLETE
        return when {
            completedTotal > expectedTotal -> ChallengeStatus.AHEAD
            completedTotal == expectedTotal -> ChallengeStatus.ON_TRACK
            else -> ChallengeStatus.BEHIND
        }
    }

    private fun firstCompletionAt(
        plannedTotal: Long,
        entries: List<ChallengeProgressEntry>,
    ): Instant? {
        var total = 0L
        for (entry in entries) {
            total = ChallengeQuantity.add(total, entry.quantity)
            if (total >= plannedTotal) {
                return entry.occurredAt
            }
        }
        return null
    }

    private fun isRecoveredCompletion(
        bounds: ChallengeDateBounds,
        entries: List<ChallengeProgressEntry>,
        plannedTotal: Long,
        targetType: com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType,
    ): Boolean {
        val progressByDate = entries.groupBy { it.entryDate }
        val orderedDates = progressByDate.keys.sorted()
        var total = 0L
        var firstCompletionDate: LocalDate? = null

        orderedDates.forEach { date ->
            total = ChallengeQuantity.add(total, progressByDate.getValue(date).sumOf { it.quantity })
            if (total >= plannedTotal && firstCompletionDate == null) {
                firstCompletionDate = date
            }
        }

        val completionDate = firstCompletionDate ?: return false
        var runningTotal = 0L
        orderedDates.forEach { date ->
            if (date.isAfter(completionDate)) return@forEach
            runningTotal = ChallengeQuantity.add(runningTotal, progressByDate.getValue(date).sumOf { it.quantity })
            val scheduled = expectedForDate(bounds, plannedTotal, date, targetType)
            if (date.isBefore(completionDate) && runningTotal < scheduled) {
                return true
            }
        }

        return false
    }

    private fun expectedForDate(
        bounds: ChallengeDateBounds,
        plannedTotal: Long,
        date: LocalDate,
        targetType: com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType,
    ): Long {
        val totalDays = bounds.inclusiveDays.coerceAtLeast(1L)
        val elapsedDays = elapsedDays(bounds, date)
        return when (targetType) {
            ChallengeTargetType.DAILY -> ChallengeQuantity.multiply(plannedTotal / totalDays, elapsedDays)
            ChallengeTargetType.TOTAL ->
                ChallengeQuantity.multiplyAndDivideFloor(
                    plannedTotal,
                    elapsedDays,
                    totalDays,
                )
        }
    }

    private fun elapsedDays(
        bounds: ChallengeDateBounds,
        date: LocalDate,
        includeToday: Boolean = true,
    ): Long {
        return when {
            date.isBefore(bounds.startDate) -> 0L
            date.isAfter(bounds.endDate) -> bounds.inclusiveDays.coerceAtLeast(1L)
            includeToday -> ChronoUnit.DAYS.between(bounds.startDate, date) + 1
            else -> ChronoUnit.DAYS.between(bounds.startDate, date)
        }.coerceIn(0L, bounds.inclusiveDays.coerceAtLeast(1L))
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
