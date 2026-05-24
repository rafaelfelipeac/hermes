package com.rafaelfelipeac.hermes.features.activity.presentation.formatter

import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_BODY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_KIND
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
                    R.string.activity_action_create_note ->
                        "Created $firstArg"
                    R.string.activity_action_update_note ->
                        "Updated $firstArg"
                    R.string.activity_action_archive_note ->
                        "Archived $firstArg"
                    R.string.activity_action_restore_note ->
                        "Restored $firstArg"
                    R.string.activity_action_delete_note ->
                        "Deleted $firstArg"
                    R.string.activity_note_kind_important -> "important note"
                    R.string.activity_value_quoted ->
                        "\"$firstArg\""
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
    fun buildTitle_noteCreate_usesNoteKindLabel() {
        val record =
            UserActionRecord(
                id = 2L,
                actionType = UserActionType.CREATE_NOTE.name,
                entityType = UserActionEntityType.NOTE.name,
                entityId = 99L,
                metadata =
                    com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer.toJson(
                        mapOf(
                            NOTE_KIND to "IMPORTANT_NOTE",
                            NOTE_TITLE to "Heel pain",
                            NOTE_BODY to "Reduce pace",
                            NOTE_TRIGGER_ENTITY_TYPE to UserActionEntityType.WORKOUT.name,
                            NOTE_TRIGGER_ENTITY_ID to "10",
                        ),
                    ),
                timestamp = 0L,
            )

        val title = formatter.buildTitle(record, formatter.parseMetadata(record.metadata))

        assertTrue(title.contains("important note"))
    }
}
