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
                    R.string.settings_unit_kilometers -> "km"
                    R.string.settings_unit_miles -> "mi"
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
}
