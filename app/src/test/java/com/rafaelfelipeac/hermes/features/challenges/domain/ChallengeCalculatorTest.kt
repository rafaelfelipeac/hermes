package com.rafaelfelipeac.hermes.features.challenges.domain

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuickAddValue
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ChallengeCalculatorTest {
    @Test
    fun integerParserAndFormatter_useWholeNumbersOnly() {
        val parsed = ChallengeQuantity.parseLocalized("1,250", java.util.Locale.US)

        assertEquals(1_250L, parsed)
        assertNull(ChallengeQuantity.parseLocalized("1.250", java.util.Locale.US))
        assertEquals("1250", ChallengeQuantity.format(1_250L, java.util.Locale.US))
    }

    @Test
    fun quickAddValues_roundHalfUpAndKeepHighestDuplicatePercentage() {
        val quickAdds = ChallengeQuantity.quickAddValues(2L)

        assertEquals(
            listOf(
                ChallengeQuickAddValue(percentage = 50, quantity = 1L),
                ChallengeQuickAddValue(percentage = 100, quantity = 2L),
            ),
            quickAdds,
        )
    }

    @Test
    fun calculator_handlesDailyGoalsWithDebtAndTodayTargets() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 3),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 8L, LocalDate.of(2026, 8, 1), "2026-08-01T08:00:00Z"),
                progressEntry(2L, 1L, 2L, LocalDate.of(2026, 8, 2), "2026-08-02T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(30L, result.plannedTotal)
        assertEquals(30L, result.expectedTotal)
        assertEquals(10L, result.completedTotal)
        assertEquals(10L, result.carriedDebt)
        assertEquals(10L, result.todayTarget)
        assertEquals(0L, result.todayProgress)
        assertEquals(10L, result.todayRemaining)
        assertEquals(20L, result.requiredPace)
        assertNull(result.firstCompletionAt)
        assertNull(result.recoveredCompletionAt)
    }

    @Test
    fun calculator_distributesTotalGoalsLinearly() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 100L,
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 4),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 20L, LocalDate.of(2026, 8, 1), "2026-08-01T08:00:00Z"),
                progressEntry(2L, 1L, 20L, LocalDate.of(2026, 8, 2), "2026-08-02T08:00:00Z"),
                progressEntry(3L, 1L, 20L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(100L, result.plannedTotal)
        assertEquals(75L, result.expectedTotal)
        assertEquals(60L, result.completedTotal)
        assertEquals(10L, result.carriedDebt)
        assertNull(result.todayTarget)
        assertEquals(20L, result.todayProgress)
    }

    private fun challenge(
        targetType: ChallengeTargetType,
        targetQuantity: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Challenge {
        return Challenge(
            id = 1L,
            title = "Build",
            description = null,
            targetType = targetType,
            targetQuantity = targetQuantity,
            startDate = startDate,
            endDate = endDate,
            lifecycle = ChallengeLifecycle.ACTIVE,
            archivedAt = null,
            createdAt = Instant.parse("2026-08-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T00:00:00Z"),
        )
    }

    private fun progressEntry(
        id: Long,
        challengeId: Long,
        quantity: Long,
        entryDate: LocalDate,
        occurredAt: String,
    ): ChallengeProgressEntry {
        val instant = Instant.parse(occurredAt)
        return ChallengeProgressEntry(
            id = id,
            challengeId = challengeId,
            quantity = quantity,
            entryDate = entryDate,
            occurredAt = instant,
            createdAt = instant,
            updatedAt = instant,
        )
    }
}
