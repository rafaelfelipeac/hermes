package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.ChallengeStatusSuccessDark
import com.rafaelfelipeac.hermes.core.ui.theme.ChallengeStatusSuccessLight
import com.rafaelfelipeac.hermes.core.ui.theme.ChallengeStatusWarningDark
import com.rafaelfelipeac.hermes.core.ui.theme.ChallengeStatusWarningLight
import com.rafaelfelipeac.hermes.core.ui.theme.isDarkBackground
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeStatus
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal const val CHALLENGES_TAG_DETAIL_HISTORY_GROUP_PREFIX = "challenges_detail_history_group_"

internal data class ChallengeProgressDateGroup(
    val date: LocalDate,
    val entries: List<ChallengeProgressEntry>,
    val completedQuantity: Long,
)

internal fun groupProgressByDate(entries: List<ChallengeProgressEntry>): List<ChallengeProgressDateGroup> {
    return entries
        .groupBy { it.entryDate }
        .toSortedMap(compareByDescending { it })
        .map { (date, dayEntries) ->
            ChallengeProgressDateGroup(
                date = date,
                entries =
                    dayEntries.sortedWith(
                        compareByDescending<ChallengeProgressEntry> { it.occurredAt }
                            .thenByDescending { it.id },
                    ),
                completedQuantity =
                    dayEntries.fold(0L) { total, entry ->
                        ChallengeQuantity.add(total, entry.quantity)
                    },
            )
        }
}

internal fun challengeHistoryGroupTag(date: LocalDate): String = "$CHALLENGES_TAG_DETAIL_HISTORY_GROUP_PREFIX$date"

internal fun initialAveragePace(challenge: Challenge): Long {
    val totalDays = (ChronoUnit.DAYS.between(challenge.startDate, challenge.endDate) + 1).coerceAtLeast(1L)
    return ChallengeQuantity.ceilDiv(challenge.targetQuantity, totalDays)
}

@Composable
internal fun challengeTargetTypeLabel(targetType: ChallengeTargetType): String {
    return when (targetType) {
        ChallengeTargetType.DAILY -> stringResource(R.string.challenge_target_type_daily)
        ChallengeTargetType.TOTAL -> stringResource(R.string.challenge_target_type_total)
    }
}

@Composable
internal fun challengeStatusLabel(status: ChallengeStatus): String {
    return stringResource(
        when (status) {
            ChallengeStatus.NOT_STARTED -> R.string.challenges_status_not_started
            ChallengeStatus.EXCEEDED -> R.string.challenges_status_exceeded
            ChallengeStatus.COMPLETED -> R.string.challenges_status_completed
            ChallengeStatus.EXPIRED_INCOMPLETE -> R.string.challenges_status_expired
            ChallengeStatus.AHEAD -> R.string.challenges_status_ahead
            ChallengeStatus.ON_TRACK -> R.string.challenges_status_on_track
            ChallengeStatus.BEHIND -> R.string.challenges_status_behind
        },
    )
}

@Composable
internal fun challengeProgressBarColor(): Color {
    return colorScheme.primary
}

@Composable
internal fun challengeStatusTextColor(status: ChallengeStatus): Color {
    val isDarkTheme = isDarkBackground(colorScheme.background)
    return when (status) {
        ChallengeStatus.ON_TRACK,
        ChallengeStatus.AHEAD,
        ChallengeStatus.NOT_STARTED,
        -> colorScheme.onSurfaceVariant

        ChallengeStatus.BEHIND -> if (isDarkTheme) ChallengeStatusWarningDark else ChallengeStatusWarningLight

        ChallengeStatus.COMPLETED,
        ChallengeStatus.EXCEEDED,
        -> if (isDarkTheme) ChallengeStatusSuccessDark else ChallengeStatusSuccessLight

        ChallengeStatus.EXPIRED_INCOMPLETE -> colorScheme.error
    }
}
