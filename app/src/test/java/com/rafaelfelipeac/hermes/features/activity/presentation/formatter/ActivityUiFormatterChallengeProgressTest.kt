package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ActivityUiFormatterChallengeProgressTest : ActivityUiFormatterTestFixture() {
    @Test
    fun restoreChallenge_usesDedicatedActivityTitle() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.RESTORE_CHALLENGE.name,
                entityType = UserActionEntityType.CHALLENGE.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals(
            "You restored the challenge \"August distance\".",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.CHALLENGE_TITLE to "August distance"),
            ),
        )
    }

    @Test
    fun challengeProgressTitle_usesChallengeTitleWithUnknownFallback() {
        val record = challengeProgressRecord(UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY)

        assertEquals(
            "You deleted progress for \"Strength\".",
            formatter.buildTitle(record, challengeProgressMetadata()),
        )
        assertEquals(
            "You deleted challenge progress.",
            formatter.buildTitle(record, emptyMap()),
        )
    }

    @Test
    fun challengeProgressTitle_usesCompletionCopyWhenProgressCompletesChallenge() {
        val completionMetadata =
            mapOf(
                UserActionMetadataKeys.WAS_COMPLETED to false.toString(),
                UserActionMetadataKeys.IS_COMPLETED to true.toString(),
            )

        listOf(
            UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY,
            UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY,
        ).forEach { actionType ->
            assertEquals(
                "You completed the challenge \"Strength\".",
                formatter.buildTitle(
                    challengeProgressRecord(actionType),
                    challengeProgressMetadata() + completionMetadata,
                ),
            )
        }

        assertEquals(
            "You completed a challenge.",
            formatter.buildTitle(
                challengeProgressRecord(UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY),
                completionMetadata,
            ),
        )
    }

    @Test
    fun challengeProgressCreateSubtitle_usesProgressContextWithoutRepeatingChallengeTitle() {
        val record = challengeProgressRecord(UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY)
        val expected =
            listOf(
                "Added 3 on Aug 31, 2026.",
                "Target: 10 per day.",
                "Category \"Strength\".",
                "Aug 1, 2026 to Aug 31, 2026.",
                "Recovered.",
            ).joinToString("\n")

        assertEquals(
            expected,
            formatter.buildSubtitle(record, challengeProgressMetadata(), Locale.US),
        )
    }

    @Test
    fun challengeProgressUpdateSubtitle_usesOldAndNewProgressValues() {
        val record = challengeProgressRecord(UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY)
        val expected =
            listOf(
                "Changed 3 on Aug 30, 2026 to 5 on Aug 31, 2026.",
                "Target: 10 per day.",
                "Category \"Strength\".",
                "Aug 1, 2026 to Aug 31, 2026.",
            ).joinToString("\n")

        assertEquals(
            expected,
            formatter.buildSubtitle(
                record,
                challengeProgressMetadata() +
                    mapOf(
                        UserActionMetadataKeys.CHALLENGE_OLD_VALUE to "3",
                        UserActionMetadataKeys.CHALLENGE_NEW_VALUE to "5",
                        UserActionMetadataKeys.CHALLENGE_OLD_DATE to "2026-08-30",
                        UserActionMetadataKeys.CHALLENGE_NEW_DATE to "2026-08-31",
                        UserActionMetadataKeys.CHALLENGE_RECOVERED to "false",
                    ),
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeProgressDeleteAndRestoreSubtitles_areActionSpecific() {
        val activeMetadata =
            challengeProgressMetadata() +
                mapOf(UserActionMetadataKeys.CHALLENGE_RECOVERED to "false")

        assertEquals(
            "Deleted 3 on Aug 31, 2026.\nTarget: 10 per day.\nCategory \"Strength\".\nAug 1, 2026 to Aug 31, 2026.",
            formatter.buildSubtitle(
                challengeProgressRecord(UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY),
                activeMetadata,
                Locale.US,
            ),
        )
        assertEquals(
            "Restored 3 on Aug 31, 2026.\nTarget: 10 per day.\nCategory \"Strength\".\nAug 1, 2026 to Aug 31, 2026.",
            formatter.buildSubtitle(
                challengeProgressRecord(UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY),
                activeMetadata,
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeProgressCreateSubtitle_handlesMissingOptionalMetadata() {
        val record = challengeProgressRecord(UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY)

        assertEquals(
            "Added 3 on Aug 31, 2026.",
            formatter.buildSubtitle(
                record,
                mapOf(
                    UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY to "3",
                    UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE to "2026-08-31",
                ),
                Locale.US,
            ),
        )
    }
}
