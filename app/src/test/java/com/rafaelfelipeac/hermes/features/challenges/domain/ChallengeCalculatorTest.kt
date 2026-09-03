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
    fun quickAddValues_returnsThreePositiveIncreasingIntegerValues() {
        assertEquals(
            listOf(
                ChallengeQuickAddValue(percentage = 25, quantity = 1L),
                ChallengeQuickAddValue(percentage = 50, quantity = 2L),
                ChallengeQuickAddValue(percentage = 100, quantity = 3L),
            ),
            ChallengeQuantity.quickAddValues(1L),
        )

        val quickAdds = ChallengeQuantity.quickAddValues(2L)

        assertEquals(
            listOf(
                ChallengeQuickAddValue(percentage = 25, quantity = 1L),
                ChallengeQuickAddValue(percentage = 50, quantity = 2L),
                ChallengeQuickAddValue(percentage = 100, quantity = 3L),
            ),
            quickAdds,
        )

        assertEquals(
            listOf(
                ChallengeQuickAddValue(percentage = 25, quantity = 3L),
                ChallengeQuickAddValue(percentage = 50, quantity = 5L),
                ChallengeQuickAddValue(percentage = 100, quantity = 10L),
            ),
            ChallengeQuantity.quickAddValues(10L),
        )
        assertEquals(3, quickAdds.map { it.quantity }.distinct().size)
    }

    @Test
    fun quantityMath_handlesLongBoundaryWithoutIntermediateOverflow() {
        val quickAdds = ChallengeQuantity.quickAddValues(Long.MAX_VALUE)

        assertEquals(Long.MAX_VALUE, quickAdds.last().quantity)
        assertEquals(
            6_148_914_691_236_517_204L,
            ChallengeQuantity.multiplyAndDivideFloor(Long.MAX_VALUE, 2L, 3L),
        )
    }

    @Test
    fun calculator_countsTodayProgressTowardDailyCarriedDebt() {
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
                progressEntry(3L, 1L, 5L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(30L, result.plannedTotal)
        assertEquals(30L, result.expectedTotal)
        assertEquals(15L, result.completedTotal)
        assertEquals(5L, result.carriedDebt)
        assertEquals(10L, result.todayTarget)
        assertEquals(5L, result.todayProgress)
        assertEquals(10L, result.todayRemaining)
        assertEquals(15L, result.requiredPace)
        assertNull(result.firstCompletionAt)
        assertNull(result.recoveredCompletionAt)
    }

    @Test
    fun calculator_clearsDailyCarriedDebtWhenTodayProgressCatchesUp() {
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
                progressEntry(3L, 1L, 15L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(30L, result.plannedTotal)
        assertEquals(30L, result.expectedTotal)
        assertEquals(25L, result.completedTotal)
        assertEquals(0L, result.carriedDebt)
        assertEquals(10L, result.todayTarget)
        assertEquals(15L, result.todayProgress)
        assertEquals(0L, result.todayRemaining)
        assertEquals(5L, result.requiredPace)
    }

    @Test
    fun calculator_countsTodayProgressTowardTotalCarriedDebt() {
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
                progressEntry(3L, 1L, 5L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(100L, result.plannedTotal)
        assertEquals(75L, result.expectedTotal)
        assertEquals(45L, result.completedTotal)
        assertEquals(5L, result.carriedDebt)
        assertNull(result.todayTarget)
        assertEquals(5L, result.todayProgress)
        assertEquals(23L, result.todayRemaining)
        assertEquals(28L, result.requiredPace)
    }

    @Test
    fun calculator_clearsTotalCarriedDebtWhenTodayProgressCatchesUp() {
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
                progressEntry(3L, 1L, 10L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.BEHIND, result.status)
        assertEquals(100L, result.plannedTotal)
        assertEquals(75L, result.expectedTotal)
        assertEquals(50L, result.completedTotal)
        assertEquals(0L, result.carriedDebt)
        assertNull(result.todayTarget)
        assertEquals(10L, result.todayProgress)
        assertEquals(15L, result.todayRemaining)
        assertEquals(25L, result.requiredPace)
    }

    @Test
    fun calculator_keepsDailyRemainingAtZeroWhenTodayProgressCoversRecoveryTarget() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 3),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 20L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(20L, result.todayProgress)
        assertEquals(10L, result.requiredPace)
        assertEquals(0L, result.todayRemaining)
    }

    @Test
    fun calculator_marksDailyCompletionAsRecoveredWhenWholeEntryDayWasMissed() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 3),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 10L, LocalDate.of(2026, 8, 1), "2026-08-01T08:00:00Z"),
                progressEntry(2L, 1L, 20L, LocalDate.of(2026, 8, 3), "2026-08-03T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 3))

        assertEquals(ChallengeStatus.COMPLETED, result.status)
        assertEquals(Instant.parse("2026-08-03T08:00:00Z"), result.firstCompletionAt)
        assertEquals(Instant.parse("2026-08-03T08:00:00Z"), result.recoveredCompletionAt)
    }

    @Test
    fun calculator_marksTotalCompletionAsRecoveredWhenWholeEntryDayWasMissed() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 100L,
                startDate = LocalDate.of(2026, 8, 1),
                endDate = LocalDate.of(2026, 8, 4),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 25L, LocalDate.of(2026, 8, 1), "2026-08-01T08:00:00Z"),
                progressEntry(2L, 1L, 75L, LocalDate.of(2026, 8, 4), "2026-08-04T08:00:00Z"),
            )

        val result = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 8, 4))

        assertEquals(ChallengeStatus.COMPLETED, result.status)
        assertEquals(Instant.parse("2026-08-04T08:00:00Z"), result.firstCompletionAt)
        assertEquals(Instant.parse("2026-08-04T08:00:00Z"), result.recoveredCompletionAt)
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
