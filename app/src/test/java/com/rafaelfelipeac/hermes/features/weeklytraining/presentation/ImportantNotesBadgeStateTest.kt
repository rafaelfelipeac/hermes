package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_BODY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_KIND
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.NOTE_KIND_IMPORTANT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.NOTE_KIND_SESSION
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ImportantNotesBadgeStateTest {
    @Test
    fun enrichWorkoutsWithImportantNotes_keepsOnlyActiveImportantNotes() {
        val workouts =
            listOf(
                workoutUi(id = 1L, title = "Morning Run"),
                workoutUi(id = 2L, title = "Tempo"),
            )
        val actions =
            listOf(
                noteAction(
                    id = 10L,
                    actionType = UserActionType.CREATE_NOTE,
                    noteKind = NOTE_KIND_IMPORTANT,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Heel pain",
                    body = "Reduce pace",
                    timestamp = 10L,
                ),
                noteAction(
                    id = 11L,
                    actionType = UserActionType.CREATE_NOTE,
                    noteKind = NOTE_KIND_SESSION,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Warm-up",
                    body = "Extra mobility",
                    timestamp = 11L,
                ),
                noteAction(
                    id = 10L,
                    actionType = UserActionType.ARCHIVE_NOTE,
                    noteKind = NOTE_KIND_IMPORTANT,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Heel pain",
                    body = "Reduce pace",
                    timestamp = 12L,
                ),
            )

        val enriched = enrichWorkoutsWithImportantNotes(workouts, actions)

        assertEquals(0, enriched.first { it.id == 1L }.importantNotes.size)
        assertEquals(0, enriched.first { it.id == 2L }.importantNotes.size)
    }

    @Test
    fun enrichWorkoutsWithImportantNotes_attachesMatchingImportantNotes() {
        val workouts =
            listOf(
                workoutUi(id = 1L, title = "Morning Run"),
                workoutUi(id = 2L, title = "Tempo"),
            )
        val actions =
            listOf(
                noteAction(
                    id = 10L,
                    actionType = UserActionType.CREATE_NOTE,
                    noteKind = NOTE_KIND_IMPORTANT,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Heel pain",
                    body = "Reduce pace",
                    timestamp = 10L,
                ),
                noteAction(
                    id = 10L,
                    actionType = UserActionType.UPDATE_NOTE,
                    noteKind = NOTE_KIND_IMPORTANT,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Heel pain",
                    body = "Warm up before speedwork",
                    timestamp = 11L,
                ),
                noteAction(
                    id = 11L,
                    actionType = UserActionType.CREATE_NOTE,
                    noteKind = NOTE_KIND_SESSION,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Warm-up",
                    body = "Extra mobility",
                    timestamp = 12L,
                ),
            )

        val enriched = enrichWorkoutsWithImportantNotes(workouts, actions)
        val notes = enriched.first { it.id == 1L }.importantNotes

        assertEquals(1, notes.size)
        assertEquals(10L, notes.single().noteId)
        assertEquals("Heel pain", notes.single().title)
        assertEquals("Warm up before speedwork", notes.single().summary)
        assertEquals(0, enriched.first { it.id == 2L }.importantNotes.size)
    }

    @Test
    fun importantNotesModalState_returnsSelectedItemListing() {
        val workouts =
            listOf(
                workoutUi(id = 1L, title = "Morning Run"),
                workoutUi(id = 2L, title = "Tempo"),
            )
        val actions =
            listOf(
                noteAction(
                    id = 10L,
                    actionType = UserActionType.CREATE_NOTE,
                    noteKind = NOTE_KIND_IMPORTANT,
                    targetEntityType = UserActionEntityType.WORKOUT,
                    targetEntityId = 1L,
                    title = "Heel pain",
                    body = "Reduce pace",
                    timestamp = 10L,
                ),
            )
        val enriched = enrichWorkoutsWithImportantNotes(workouts, actions)

        val modalState = buildImportantNotesModalState(enriched, 1L)

        assertEquals(1L, modalState?.itemId)
        assertEquals("Morning Run", modalState?.itemTitle)
        assertEquals(1, modalState?.notes?.size)
    }

    @Test
    fun importantNotesModalState_returnsNullForMissingItem() {
        val modalState = buildImportantNotesModalState(emptyList(), 1L)

        assertNull(modalState)
    }

    private fun workoutUi(
        id: Long,
        title: String,
    ): WorkoutUi {
        return WorkoutUi(
            id = id,
            weekStartDate = LocalDate.of(2026, 2, 2),
            dayOfWeek = java.time.DayOfWeek.MONDAY,
            type = title,
            description = title,
            isCompleted = false,
            isRestDay = false,
            categoryId = null,
            categoryColorId = null,
            categoryName = null,
            order = 0,
            eventType = EventType.WORKOUT,
        )
    }

    private fun noteAction(
        id: Long,
        actionType: UserActionType,
        noteKind: String,
        targetEntityType: UserActionEntityType,
        targetEntityId: Long,
        title: String,
        body: String,
        timestamp: Long,
    ): UserActionRecord {
        return UserActionRecord(
            id = id,
            actionType = actionType.name,
            entityType = UserActionEntityType.NOTE.name,
            entityId = id,
            metadata =
                UserActionMetadataSerializer.toJson(
                    mapOf(
                        NOTE_KIND to noteKind,
                        NOTE_TITLE to title,
                        NOTE_BODY to body,
                        NOTE_TRIGGER_ENTITY_TYPE to targetEntityType.name,
                        NOTE_TRIGGER_ENTITY_ID to targetEntityId.toString(),
                    ),
                ),
            timestamp = timestamp,
        )
    }
}
