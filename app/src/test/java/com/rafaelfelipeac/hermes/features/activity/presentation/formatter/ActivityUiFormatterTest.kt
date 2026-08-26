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
}
