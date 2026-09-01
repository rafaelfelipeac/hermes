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
                return when (id) {
                    R.string.activity_action_create_race_event ->
                        "You created the event $firstArg."
                    R.string.activity_value_quoted ->
                        "\"$firstArg\""
                    R.string.activity_action_change_distance_unit -> "You changed the distance unit."
                    R.string.activity_action_use_pace_calculator -> "You used the pace calculator."
                    R.string.activity_action_create_personal_record_entry ->
                        "You added a $firstArg PR result."
                    R.string.activity_action_restore_challenge_named ->
                        "You restored the challenge $firstArg."
                    R.string.activity_action_create_challenge_progress_entry_named ->
                        "You logged progress for $firstArg."
                    R.string.activity_action_update_challenge_progress_entry_named ->
                        "You updated progress for $firstArg."
                    R.string.activity_action_delete_challenge_progress_entry_named ->
                        "You deleted progress for $firstArg."
                    R.string.activity_action_restore_challenge_progress_entry_named ->
                        "You restored progress for $firstArg."
                    R.string.activity_subtitle_challenge_progress_added ->
                        "Added ${args[0]} on ${args[1]}."
                    R.string.activity_subtitle_challenge_progress_updated ->
                        "Changed ${args[0]} on ${args[1]} to ${args[2]} on ${args[3]}."
                    R.string.activity_subtitle_challenge_progress_updated_current ->
                        "Updated ${args[0]} on ${args[1]}."
                    R.string.activity_subtitle_challenge_progress_deleted ->
                        "Deleted ${args[0]} on ${args[1]}."
                    R.string.activity_subtitle_challenge_progress_restored ->
                        "Restored ${args[0]} on ${args[1]}."
                    R.string.activity_subtitle_challenge_target ->
                        "${args[0]} target of ${args[1]}."
                    R.string.activity_subtitle_challenge_category ->
                        "Category ${args[0]}."
                    R.string.activity_subtitle_challenge_dates ->
                        "${args[0]} to ${args[1]}."
                    R.string.activity_subtitle_challenge_recovered -> "Recovered."
                    R.string.challenge_target_type_daily -> "Daily"
                    R.string.challenge_target_type_total -> "Total"
                    R.string.activity_value_unknown -> "Unknown"
                    R.string.activity_subtitle_separator -> "•"
                    R.string.activity_week_date_pattern -> "MMM d, uuuu"
                    R.string.settings_unit_kilometers -> "km"
                    R.string.settings_unit_miles -> "mi"
                    R.string.personal_records_metric_distance -> "Distance"
                    R.string.activity_subtitle_change_value -> "From ${args[0]} to ${args[1]}."
                    R.string.activity_workout_fallback -> "untitled"
                    else -> id.toString()
                }
            }

            override fun getForLanguage(
                languageTag: String?,
                id: Int,
                vararg args: Any,
            ): String = get(id, *args)
        }

    private val formatter = ActivityUiFormatter(stringProvider)

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
            "42.2 km • Aug 24, 2026",
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
            "39:20 • Aug 24, 2026",
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
    fun challengeProgressCreateSubtitle_usesProgressContextWithoutRepeatingChallengeTitle() {
        val record = challengeProgressRecord(UserActionType.CREATE_CHALLENGE_PROGRESS_ENTRY)
        val expected =
            listOf(
                "Added 3 on Aug 31, 2026.",
                "Daily target of 10.",
                "Category \"Strength\".",
                "Aug 1, 2026 to Aug 31, 2026.",
                "Recovered.",
            ).joinToString(" • ")

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
                "Daily target of 10.",
                "Category \"Strength\".",
                "Aug 1, 2026 to Aug 31, 2026.",
            ).joinToString(" • ")

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
            "Deleted 3 on Aug 31, 2026. • Daily target of 10. • Category \"Strength\". • Aug 1, 2026 to Aug 31, 2026.",
            formatter.buildSubtitle(
                challengeProgressRecord(UserActionType.DELETE_CHALLENGE_PROGRESS_ENTRY),
                challengeProgressMetadata() + mapOf(UserActionMetadataKeys.CHALLENGE_RECOVERED to "false"),
                Locale.US,
            ),
        )
        assertEquals(
            "Restored 3 on Aug 31, 2026. • Daily target of 10. • Category \"Strength\". • Aug 1, 2026 to Aug 31, 2026.",
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

    private fun challengeProgressMetadata(): Map<String, String> {
        return mapOf(
            UserActionMetadataKeys.CHALLENGE_TITLE to "Strength",
            UserActionMetadataKeys.CHALLENGE_TARGET_TYPE to "DAILY",
            UserActionMetadataKeys.CHALLENGE_TARGET_QUANTITY to "10",
            UserActionMetadataKeys.CHALLENGE_CATEGORY_NAME to "Strength",
            UserActionMetadataKeys.CHALLENGE_START_DATE to "2026-08-01",
            UserActionMetadataKeys.CHALLENGE_END_DATE to "2026-08-31",
            UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY to "3",
            UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE to "2026-08-31",
            UserActionMetadataKeys.CHALLENGE_RECOVERED to "true",
        )
    }
}
