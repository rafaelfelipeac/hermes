package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ActivityUiFormatterChallengeTest : ActivityUiFormatterTestFixture() {
    @Test
    fun challengeCreateSubtitle_usesTargetContextWithoutValueChange() {
        assertEquals(
            "Target: 10 per day.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.CREATE_CHALLENGE),
                challengeMetadata(),
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeTotalTargetSubtitle_usesTotalCopy() {
        assertEquals(
            "Target: 120 total.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.CREATE_CHALLENGE),
                challengeMetadata() +
                    mapOf(
                        UserActionMetadataKeys.CHALLENGE_TARGET_TYPE to "TOTAL",
                        UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "120",
                    ),
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeLifecycleSubtitle_ignoresRedundantStatusTransitions() {
        val lifecycleMetadata =
            challengeMetadata() +
                mapOf(
                    UserActionMetadataKeys.CHALLENGE_OLD_STATUS to "ARCHIVED",
                    UserActionMetadataKeys.CHALLENGE_NEW_STATUS to "ARCHIVED",
                    UserActionMetadataKeys.CHALLENGE_LIFECYCLE to "ARCHIVED",
                )

        assertEquals(
            "Target: 10 per day.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.ARCHIVE_CHALLENGE),
                lifecycleMetadata,
                Locale.US,
            ),
        )
        assertEquals(
            "Target: 10 per day.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.REACTIVATE_CHALLENGE),
                lifecycleMetadata +
                    mapOf(
                        UserActionMetadataKeys.CHALLENGE_OLD_STATUS to "ACTIVE",
                        UserActionMetadataKeys.CHALLENGE_NEW_STATUS to "ACTIVE",
                    ),
                Locale.US,
            ),
        )
        assertEquals(
            "Target: 10 per day.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.DELETE_CHALLENGE),
                lifecycleMetadata,
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeUpdateSubtitle_omitsFalseTargetChangeWhenEqual() {
        assertEquals(
            "Aug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.UPDATE_CHALLENGE),
                challengeMetadata() +
                    mapOf(
                        UserActionMetadataKeys.OLD_TYPE to "DAILY",
                        UserActionMetadataKeys.NEW_TYPE to "DAILY",
                        UserActionMetadataKeys.CHALLENGE_OLD_VALUE to "10",
                        UserActionMetadataKeys.CHALLENGE_NEW_VALUE to "10",
                    ),
                Locale.US,
            ),
        )
    }

    @Test
    fun challengeUpdateSubtitle_showsRealTargetChange() {
        assertEquals(
            "Target changed from 10 per day to 15 per day.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.UPDATE_CHALLENGE),
                challengeMetadata() +
                    mapOf(
                        UserActionMetadataKeys.OLD_TYPE to "DAILY",
                        UserActionMetadataKeys.NEW_TYPE to "DAILY",
                        UserActionMetadataKeys.CHALLENGE_OLD_VALUE to "10",
                        UserActionMetadataKeys.CHALLENGE_NEW_VALUE to "15",
                        UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "15",
                    ),
                Locale.US,
            ),
        )
        assertEquals(
            "Target changed from 10 per day to 120 total.\nAug 1, 2026 to Aug 31, 2026.\nCategory \"Strength\".",
            formatter.buildSubtitle(
                challengeRecord(UserActionType.UPDATE_CHALLENGE),
                challengeMetadata() +
                    mapOf(
                        UserActionMetadataKeys.OLD_TYPE to "DAILY",
                        UserActionMetadataKeys.NEW_TYPE to "TOTAL",
                        UserActionMetadataKeys.CHALLENGE_OLD_VALUE to "10",
                        UserActionMetadataKeys.CHALLENGE_NEW_VALUE to "120",
                        UserActionMetadataKeys.CHALLENGE_TARGET_TYPE to "TOTAL",
                        UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "120",
                    ),
                Locale.US,
            ),
        )
    }
}
