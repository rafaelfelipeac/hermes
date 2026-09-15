package com.rafaelfelipeac.hermes.features.challenges.presentation

import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ChallengeProgressPresentationTest {
    @Test
    fun groupProgressByDate_sortsDaysAndEntriesAndSumsEachDay() {
        val groups =
            groupProgressByDate(
                listOf(
                    progressEntry(
                        id = 1L,
                        quantity = 3L,
                        date = LocalDate.of(2026, 9, 1),
                        occurredAt = "2026-09-01T08:00:00Z",
                    ),
                    progressEntry(
                        id = 2L,
                        quantity = 4L,
                        date = LocalDate.of(2026, 9, 2),
                        occurredAt = "2026-09-02T08:00:00Z",
                    ),
                    progressEntry(
                        id = 3L,
                        quantity = 5L,
                        date = LocalDate.of(2026, 9, 2),
                        occurredAt = "2026-09-02T09:00:00Z",
                    ),
                    progressEntry(
                        id = 4L,
                        quantity = 2L,
                        date = LocalDate.of(2026, 9, 2),
                        occurredAt = "2026-09-02T09:00:00Z",
                    ),
                ),
            )

        assertEquals(listOf(LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 1)), groups.map { it.date })
        assertEquals(11L, groups.first().completedQuantity)
        assertEquals(listOf(4L, 3L, 2L), groups.first().entries.map { it.id })
        assertEquals("challenges_detail_history_group_2026-09-02", challengeHistoryGroupTag(groups.first().date))
    }

    @Test
    fun initialAveragePace_roundsUpAcrossInclusiveDateRange() {
        val challenge =
            Challenge(
                id = 1L,
                title = "September distance",
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 10L,
                startDate = LocalDate.of(2026, 9, 1),
                endDate = LocalDate.of(2026, 9, 3),
                lifecycle = ChallengeLifecycle.ACTIVE,
                createdAt = Instant.parse("2026-09-01T00:00:00Z"),
                updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
            )

        assertEquals(4L, initialAveragePace(challenge))
    }

    @Test
    fun challengeEditorRules_keepDateAndOverflowBoundariesPure() {
        val challenge = challenge(startDate = LocalDate.of(2026, 9, 1), endDate = LocalDate.of(2026, 9, 3))

        assertEquals(
            true,
            canEditChallengeProgress(
                challenge = challenge,
                entryDate = LocalDate.of(2026, 9, 2),
                today = LocalDate.of(2026, 9, 2),
            ),
        )
        assertEquals(
            false,
            canEditChallengeProgress(
                challenge = challenge,
                entryDate = LocalDate.of(2026, 9, 3),
                today = LocalDate.of(2026, 9, 2),
            ),
        )
        assertEquals(
            false,
            isPlannedChallengeTargetSafe(
                targetType = ChallengeTargetType.DAILY,
                targetQuantity = Long.MAX_VALUE,
                startDate = LocalDate.of(2026, 9, 1),
                endDate = LocalDate.of(2026, 9, 2),
            ),
        )
        assertEquals(
            true,
            isChallengeProgressTotalSafe(
                entries = listOf(progressEntry(1L, Long.MAX_VALUE, LocalDate.of(2026, 9, 1), "2026-09-01T08:00:00Z")),
                challengeId = 1L,
                quantity = Long.MAX_VALUE,
                replacedEntryId = 1L,
            ),
        )
    }

    private fun challenge(
        startDate: LocalDate,
        endDate: LocalDate,
    ) = Challenge(
        id = 1L,
        title = "September distance",
        targetType = ChallengeTargetType.TOTAL,
        targetQuantity = 10L,
        startDate = startDate,
        endDate = endDate,
        lifecycle = ChallengeLifecycle.ACTIVE,
        createdAt = Instant.parse("2026-09-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-09-01T00:00:00Z"),
    )

    private fun progressEntry(
        id: Long,
        quantity: Long,
        date: LocalDate,
        occurredAt: String,
    ) = ChallengeProgressEntry(
        id = id,
        challengeId = 1L,
        quantity = quantity,
        entryDate = date,
        occurredAt = Instant.parse(occurredAt),
        createdAt = Instant.parse(occurredAt),
        updatedAt = Instant.parse(occurredAt),
    )
}
