package com.rafaelfelipeac.hermes.features.challenges.domain

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ChallengeCalculatorTest {
    @Test
    fun quantityParserAndFormatter_keepThreeDecimalPrecision() {
        val parsed = ChallengeQuantity.parseLocalized("1.250", java.util.Locale.US)
        assertEquals(1_250L, parsed)
        assertEquals("1.25", ChallengeQuantity.format(1_250L, java.util.Locale.US))
    }

    @Test
    fun calculator_marksCompletedAndReturnsQuickAddValues() {
        val challenge =
            Challenge(
                id = 1L,
                title = "Build",
                description = null,
                targetQuantity = 10_000L,
                unit = "km",
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 10),
                lifecycle = ChallengeLifecycle.ACTIVE,
                archivedAt = null,
                createdAt = Instant.parse("2026-08-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-08-01T00:00:00Z"),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 4_000L, LocalDate.of(2026, 8, 1), "2026-08-01T08:00:00Z"),
                progressEntry(2L, 1L, 6_000L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.COMPLETED, result.status)
        assertEquals(10_000L, result.completedTotal)
        assertEquals(0L, result.remainingTotal)
        assertEquals(0L, result.carriedDebt)
        assertEquals(Instant.parse("2026-08-03T08:00:00Z"), result.firstCompletionAt)
        assertNull(result.recoveredCompletionAt)
        assertEquals(listOf(2_500L, 5_000L, 10_000L), ChallengeQuantity.quickAddValues(10_000L))
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
