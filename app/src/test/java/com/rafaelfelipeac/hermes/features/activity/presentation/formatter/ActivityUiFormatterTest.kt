package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ActivityUiFormatterTest {
    private val stringProvider =
        object : StringProvider {
            override fun get(
                id: Int,
                vararg args: Any,
            ): String {
                val firstArg = args.firstOrNull()?.toString().orEmpty()
                return firstArgStrings[id]?.invoke(firstArg)
                    ?: argsStrings[id]?.invoke(args)
                    ?: id.toString()
            }

            override fun getForLanguage(
                languageTag: String?,
                id: Int,
                vararg args: Any,
            ): String = get(id, *args)
        }

    private val formatter = ActivityUiFormatter(stringProvider)

    private val firstArgStrings =
        mapOf<Int, (String) -> String>(
            R.string.activity_action_create_race_event to { "You created the event $it." },
            R.string.activity_value_quoted to { "\"$it\"" },
            R.string.activity_action_change_distance_unit to { "You changed the distance unit." },
            R.string.activity_action_use_pace_calculator to { "You used the pace calculator." },
            R.string.activity_action_create_personal_record_entry to { "You added a $it PR result." },
            R.string.activity_action_restore_challenge_named to { "You restored the challenge $it." },
            R.string.activity_action_create_challenge_progress_entry to { "You logged challenge progress." },
            R.string.activity_action_update_challenge_progress_entry to { "You updated challenge progress." },
            R.string.activity_action_delete_challenge_progress_entry to { "You deleted challenge progress." },
            R.string.activity_action_restore_challenge_progress_entry to { "You restored challenge progress." },
            R.string.activity_action_complete_challenge to { "You completed a challenge." },
            R.string.activity_action_create_challenge_progress_entry_named to { "You logged progress for $it." },
            R.string.activity_action_update_challenge_progress_entry_named to { "You updated progress for $it." },
            R.string.activity_action_delete_challenge_progress_entry_named to { "You deleted progress for $it." },
            R.string.activity_action_restore_challenge_progress_entry_named to { "You restored progress for $it." },
            R.string.activity_action_complete_challenge_named to { "You completed the challenge $it." },
            R.string.activity_subtitle_challenge_recovered to { "Recovered." },
            R.string.challenge_target_type_daily to { "Daily" },
            R.string.challenge_target_type_total to { "Total" },
            R.string.activity_value_unknown to { "Unknown" },
            R.string.activity_subtitle_separator to { "\n" },
            R.string.activity_week_date_pattern to { "MMM d, uuuu" },
            R.string.settings_unit_kilometers to { "km" },
            R.string.settings_unit_miles to { "mi" },
            R.string.personal_records_metric_distance to { "Distance" },
            R.string.activity_workout_fallback to { "untitled" },
        )

    private val argsStrings =
        mapOf<Int, (Array<out Any>) -> String>(
            R.string.activity_subtitle_challenge_progress_added to { "Added ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_updated to {
                "Changed ${it[0]} on ${it[1]} to ${it[2]} on ${it[3]}."
            },
            R.string.activity_subtitle_challenge_progress_updated_current to { "Updated ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_deleted to { "Deleted ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_progress_restored to { "Restored ${it[0]} on ${it[1]}." },
            R.string.activity_subtitle_challenge_target_daily to { "Target: ${it[0]} per day." },
            R.string.activity_subtitle_challenge_target_total to { "Target: ${it[0]} total." },
            R.string.activity_subtitle_challenge_target_change to { "Target changed from ${it[0]} to ${it[1]}." },
            R.string.activity_subtitle_challenge_target_value_daily to { "${it[0]} per day" },
            R.string.activity_subtitle_challenge_target_value_total to { "${it[0]} total" },
            R.string.activity_subtitle_challenge_category to { "Category ${it[0]}." },
            R.string.activity_subtitle_challenge_dates to { "${it[0]} to ${it[1]}." },
            R.string.activity_subtitle_change_value to { "From ${it[0]} to ${it[1]}." },
        )

    @Test
    fun buildTitle_raceEventCreate_usesEventNameWithoutCrashing() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.CREATE_RACE_EVENT.name,
                entityType = UserActionEntityType.RACE_EVENT.name,
                entityId = 42L,
                metadata = null,
                timestamp = 0L,
            )

        val title = formatter.buildTitle(record, emptyMap())
        val titleWithMetadata =
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.NEW_DESCRIPTION to "City 10K"),
            )

        assertEquals("You created the event \"untitled\".", title)
        assertEquals("You created the event \"City 10K\".", titleWithMetadata)
    }

    @Test
    fun distanceUnitChange_hasLocalizedTitleAndSubtitle() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.CHANGE_DISTANCE_UNIT.name,
                entityType = UserActionEntityType.SETTINGS.name,
                entityId = null,
                metadata = null,
                timestamp = 0L,
            )
        val metadata =
            mapOf(
                UserActionMetadataKeys.OLD_VALUE to "KILOMETERS",
                UserActionMetadataKeys.NEW_VALUE to "MILES",
            )

        assertEquals("You changed the distance unit.", formatter.buildTitle(record, metadata))
        assertEquals("From \"km\" to \"mi\".", formatter.buildSubtitle(record, metadata, Locale.US))
    }

    @Test
    fun paceCalculatorUse_hasLocalizedActivityTitle() {
        val record =
            UserActionRecord(
                id = 1L,
                actionType = UserActionType.USE_PACE_CALCULATOR.name,
                entityType = UserActionEntityType.APP.name,
                entityId = null,
                metadata = null,
                timestamp = 0L,
            )

        assertEquals("You used the pace calculator.", formatter.buildTitle(record, emptyMap()))
    }

    @Test
    fun personalRecordDistanceResult_hasSingleUnitAndSpacedSeparator() {
        val record = personalRecordEntryRecord()
        val metadata =
            mapOf(
                UserActionMetadataKeys.PERSONAL_RECORD_UNIT to "KILOMETER",
                UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE to "42.2",
                UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE to "2026-08-24",
            )

        assertEquals(
            "42.2 km\nAug 24, 2026",
            formatter.buildSubtitle(record, metadata, Locale.US),
        )
    }

    @Test
    fun personalRecordResultTitle_usesSeriesNameWithMetricFallback() {
        val record = personalRecordEntryRecord()

        assertEquals(
            "You added a 5 km PR result.",
            formatter.buildTitle(
                record,
                mapOf(
                    UserActionMetadataKeys.PERSONAL_RECORD_FAMILY_TITLE to "5 km",
                    UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "DISTANCE",
                ),
            ),
        )
        assertEquals(
            "You added a Distance PR result.",
            formatter.buildTitle(
                record,
                mapOf(UserActionMetadataKeys.PERSONAL_RECORD_METRIC_TYPE to "DISTANCE"),
            ),
        )
    }

    @Test
    fun personalRecordTimeResult_hasSpacedSeparator() {
        val record = personalRecordEntryRecord()
        val metadata =
            mapOf(
                UserActionMetadataKeys.PERSONAL_RECORD_UNIT to "SECOND",
                UserActionMetadataKeys.PERSONAL_RECORD_NEW_VALUE to "2360",
                UserActionMetadataKeys.PERSONAL_RECORD_RECORD_DATE to "2026-08-24",
            )

        assertEquals(
            "39:20\nAug 24, 2026",
            formatter.buildSubtitle(record, metadata, Locale.US),
        )
    }

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
        assertEquals(
            "Deleted 3 on Aug 31, 2026.\nTarget: 10 per day.\nCategory \"Strength\".\nAug 1, 2026 to Aug 31, 2026.",
            formatter.buildSubtitle(
                challengeProgressRecord(UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY),
                challengeProgressMetadata() + mapOf(UserActionMetadataKeys.CHALLENGE_RECOVERED to "false"),
                Locale.US,
            ),
        )
        assertEquals(
            "Restored 3 on Aug 31, 2026.\nTarget: 10 per day.\nCategory \"Strength\".\nAug 1, 2026 to Aug 31, 2026.",
            formatter.buildSubtitle(
                challengeProgressRecord(UserActionType.RESTORE_CHALLENGE_PROGRESS_ENTRY),
                challengeProgressMetadata() + mapOf(UserActionMetadataKeys.CHALLENGE_RECOVERED to "false"),
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

    private fun personalRecordEntryRecord(): UserActionRecord {
        return UserActionRecord(
            id = 1L,
            actionType = UserActionType.CREATE_PERSONAL_RECORD_ENTRY.name,
            entityType = UserActionEntityType.PERSONAL_RECORD.name,
            entityId = 42L,
            metadata = null,
            timestamp = 0L,
        )
    }

    private fun challengeProgressRecord(actionType: UserActionType): UserActionRecord {
        return UserActionRecord(
            id = 1L,
            actionType = actionType.name,
            entityType = UserActionEntityType.CHALLENGE.name,
            entityId = 42L,
            metadata = null,
            timestamp = 0L,
        )
    }

    private fun challengeRecord(actionType: UserActionType): UserActionRecord = challengeProgressRecord(actionType)

    private fun challengeMetadata(): Map<String, String> {
        return mapOf(
            UserActionMetadataKeys.CHALLENGE_TITLE to "Strength",
            UserActionMetadataKeys.CHALLENGE_TARGET_TYPE to "DAILY",
            UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "10",
            UserActionMetadataKeys.CHALLENGE_CATEGORY_NAME to "Strength",
            UserActionMetadataKeys.CHALLENGE_START_DATE to "2026-08-01",
            UserActionMetadataKeys.CHALLENGE_END_DATE to "2026-08-31",
        )
    }

    private fun challengeProgressMetadata(): Map<String, String> {
        return challengeMetadata() +
            mapOf(
                UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY to "3",
                UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE to "2026-08-31",
                UserActionMetadataKeys.CHALLENGE_RECOVERED to "true",
            )
    }
}
