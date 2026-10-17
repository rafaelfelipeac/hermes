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
    fun calculator_reportsDailyDelayThroughTodayAndUsesStartOfDayRecoveryTarget() {
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
        assertEquals(15L, result.carriedDebt)
        assertEquals(10L, result.todayTarget)
        assertEquals(5L, result.todayProgress)
        assertEquals(15L, result.todayRemaining)
        assertEquals(20L, result.requiredPace)
        assertNull(result.firstCompletionAt)
        assertNull(result.recoveredCompletionAt)
    }

    @Test
    fun calculator_keepsDailyDelayForTheCurrentDayUntilExpectedProgressIsReached() {
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
        assertEquals(5L, result.carriedDebt)
        assertEquals(10L, result.todayTarget)
        assertEquals(15L, result.todayProgress)
        assertEquals(5L, result.todayRemaining)
        assertEquals(20L, result.requiredPace)
    }

    @Test
    fun calculator_reportsTotalDelayThroughTodayAndUsesStartOfDayRecoveryTarget() {
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
        assertEquals(30L, result.carriedDebt)
        assertNull(result.todayTarget)
        assertEquals(5L, result.todayProgress)
        assertEquals(25L, result.todayRemaining)
        assertEquals(30L, result.requiredPace)
    }

    @Test
    fun calculator_keepsTotalDelayForTheCurrentDayUntilExpectedProgressIsReached() {
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
        assertEquals(25L, result.carriedDebt)
        assertNull(result.todayTarget)
        assertEquals(10L, result.todayProgress)
        assertEquals(20L, result.todayRemaining)
        assertEquals(30L, result.requiredPace)
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
        assertEquals(30L, result.requiredPace)
        assertEquals(10L, result.todayRemaining)
    }

    @Test
    fun calculator_matchesAccumulatedDelayExampleAndRecalculatesOnTheNextDay() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 1, 1),
                endDate = LocalDate.of(2026, 12, 31),
            )
        val entries =
            listOf(
                progressEntry(1L, 1L, 889L, LocalDate.of(2026, 10, 15), "2026-10-15T08:00:00Z"),
                progressEntry(2L, 1L, 30L, LocalDate.of(2026, 10, 16), "2026-10-16T08:00:00Z"),
            )

        val today = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 10, 16))
        val nextDay = ChallengeCalculator().calculate(challenge, entries, LocalDate.of(2026, 10, 17))

        assertEquals(2_890L, today.expectedTotal)
        assertEquals(919L, today.completedTotal)
        assertEquals(1_971L, today.carriedDebt)
        assertEquals(36L, today.requiredPace)
        assertEquals(30L, today.todayProgress)
        assertEquals(6L, today.todayRemaining)

        assertEquals(2_900L, nextDay.expectedTotal)
        assertEquals(1_981L, nextDay.carriedDebt)
        assertEquals(36L, nextDay.requiredPace)
        assertEquals(0L, nextDay.todayProgress)
        assertEquals(36L, nextDay.todayRemaining)
    }

    @Test
    fun calculator_keepsRecoveryTargetStableWhileTodayEntriesIncrease() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 1, 1),
                endDate = LocalDate.of(2026, 1, 10),
            )
        val firstEntry =
            progressEntry(1L, 1L, 5L, LocalDate.of(2026, 1, 3), "2026-01-03T08:00:00Z")
        val secondEntry =
            progressEntry(2L, 1L, 5L, LocalDate.of(2026, 1, 3), "2026-01-03T09:00:00Z")

        val afterFirst = ChallengeCalculator().calculate(challenge, listOf(firstEntry), LocalDate.of(2026, 1, 3))
        val afterSecond =
            ChallengeCalculator().calculate(
                challenge,
                listOf(firstEntry, secondEntry),
                LocalDate.of(2026, 1, 3),
            )

        assertEquals(13L, afterFirst.requiredPace)
        assertEquals(8L, afterFirst.todayRemaining)
        assertEquals(afterFirst.requiredPace, afterSecond.requiredPace)
        assertEquals(3L, afterSecond.todayRemaining)
    }

    @Test
    fun calculator_hidesTodayTargetBeforeAndAfterChallengePeriod() {
        val challenge =
            challenge(
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 100L,
                startDate = LocalDate.of(2026, 8, 2),
                endDate = LocalDate.of(2026, 8, 4),
            )

        val before = ChallengeCalculator().calculate(challenge, emptyList(), LocalDate.of(2026, 8, 1))
        val after = ChallengeCalculator().calculate(challenge, emptyList(), LocalDate.of(2026, 8, 5))

        assertEquals(ChallengeStatus.NOT_STARTED, before.status)
        assertNull(before.todayRemaining)
        assertEquals(0L, before.requiredPace)
        assertEquals(ChallengeStatus.EXPIRED_INCOMPLETE, after.status)
        assertNull(after.todayRemaining)
        assertEquals(0L, after.requiredPace)
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
