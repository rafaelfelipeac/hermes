@file:Suppress("LongMethod", "MatchingDeclarationName")

package com.rafaelfelipeac.hermes.core.debug

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.MOBILITY_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.RUN_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.STRENGTH_ID
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.SWIM_ID
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.Instant
import java.time.LocalDate

internal data class ChallengeScenario(
    val challenge: Challenge,
    val entries: List<Pair<Long, LocalDate>>,
)

internal fun buildChallengeScenarios(
    stringProvider: StringProvider,
    today: LocalDate,
    now: Instant,
): List<ChallengeScenario> {
    return listOf(
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = RUN_ID,
                    title = stringProvider.get(R.string.mock_workout_type_cardio),
                    description = stringProvider.get(R.string.mock_workout_description_long_run),
                    targetType = ChallengeTargetType.TOTAL,
                    targetQuantity = 120L,
                    startDate = today.minusDays(10),
                    endDate = today.plusDays(10),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries =
                listOf(
                    12L to today.minusDays(9),
                    24L to today.minusDays(8),
                    18L to today.minusDays(8),
                    30L to today.minusDays(5),
                    16L to today.minusDays(3),
                    18L to today.minusDays(2),
                    10L to today,
                ),
        ),
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = STRENGTH_ID,
                    title = stringProvider.get(R.string.mock_workout_type_strength),
                    description = stringProvider.get(R.string.mock_workout_description_strength),
                    targetType = ChallengeTargetType.TOTAL,
                    targetQuantity = 240L,
                    startDate = today.minusDays(14),
                    endDate = today.plusDays(7),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries =
                listOf(
                    20L to today.minusDays(12),
                    20L to today.minusDays(12),
                    25L to today.minusDays(8),
                    30L to today.minusDays(4),
                    45L to today.minusDays(2),
                    35L to today.minusDays(1),
                    20L to today,
                ),
        ),
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = CYCLING_ID,
                    title = stringProvider.get(R.string.mock_workout_type_hiits),
                    description = stringProvider.get(R.string.mock_workout_description_hiits),
                    targetType = ChallengeTargetType.TOTAL,
                    targetQuantity = 180L,
                    startDate = today.minusDays(21),
                    endDate = today.plusDays(2),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries =
                listOf(
                    30L to today.minusDays(18),
                    45L to today.minusDays(11),
                    50L to today.minusDays(5),
                    40L to today.minusDays(1),
                    25L to today,
                ),
        ),
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = SWIM_ID,
                    title = stringProvider.get(R.string.mock_workout_type_yoga),
                    description = stringProvider.get(R.string.mock_workout_description_yoga),
                    targetType = ChallengeTargetType.DAILY,
                    targetQuantity = 1L,
                    startDate = today,
                    endDate = today.plusDays(13),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries = emptyList(),
        ),
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = STRENGTH_ID,
                    title = stringProvider.get(R.string.mock_workout_type_core),
                    description = stringProvider.get(R.string.mock_workout_description_core),
                    targetType = ChallengeTargetType.DAILY,
                    targetQuantity = 10L,
                    startDate = today.minusDays(244),
                    endDate = today.plusDays(120),
                    lifecycle = ChallengeLifecycle.ACTIVE,
                    archivedAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries =
                listOf(
                    10L to today.minusDays(20),
                    10L to today.minusDays(19),
                    5L to today.minusDays(12),
                    15L to today.minusDays(12),
                    10L to today.minusDays(6),
                    8L to today.minusDays(2),
                    12L to today.minusDays(1),
                ),
        ),
        ChallengeScenario(
            challenge =
                Challenge(
                    id = 0L,
                    categoryId = MOBILITY_ID,
                    title = stringProvider.get(R.string.mock_workout_type_mobility),
                    description = stringProvider.get(R.string.mock_workout_description_mobility),
                    targetType = ChallengeTargetType.TOTAL,
                    targetQuantity = 45L,
                    startDate = today.minusDays(20),
                    endDate = today.minusDays(3),
                    lifecycle = ChallengeLifecycle.ARCHIVED,
                    archivedAt = now,
                    createdAt = now,
                    updatedAt = now,
                ),
            entries =
                listOf(
                    5L to today.minusDays(19),
                    15L to today.minusDays(18),
                    15L to today.minusDays(12),
                    10L to today.minusDays(12),
                    15L to today.minusDays(6),
                ),
        ),
    )
}

internal fun buildChallengeProgressEntry(
    challengeId: Long,
    quantity: Long,
    entryDate: LocalDate,
    occurredAt: Instant,
    baseInstant: Instant,
): ChallengeProgressEntry {
    return ChallengeProgressEntry(
        id = 0L,
        challengeId = challengeId,
        quantity = quantity,
        entryDate = entryDate,
        occurredAt = occurredAt,
        createdAt = baseInstant,
        updatedAt = baseInstant,
    )
}
