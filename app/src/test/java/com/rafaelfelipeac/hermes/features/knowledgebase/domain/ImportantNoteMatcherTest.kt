package com.rafaelfelipeac.hermes.features.knowledgebase.domain

import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.ImportantNoteTarget
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportantNoteMatcherTest {
    @Test
    fun matchImportantNotes_filtersByScopeCategoryAndStatus() {
        val notes =
            listOf(
                importantNote(
                    id = 1L,
                    status = KnowledgeNoteStatus.ACTIVE,
                    triggerScope = KnowledgeNoteTriggerScope.WORKOUT,
                    categoryId = 9L,
                ),
                importantNote(
                    id = 2L,
                    status = KnowledgeNoteStatus.ACTIVE,
                    triggerScope = KnowledgeNoteTriggerScope.EVENT,
                    categoryId = 9L,
                ),
                importantNote(
                    id = 3L,
                    status = KnowledgeNoteStatus.ARCHIVED,
                    triggerScope = KnowledgeNoteTriggerScope.BOTH,
                    categoryId = 9L,
                ),
                note(
                    id = 4L,
                    kind = KnowledgeNoteKind.NOTE,
                    status = KnowledgeNoteStatus.ACTIVE,
                    title = "Standalone",
                    body = "Standalone note",
                    categoryId = 9L,
                    triggerScope = null,
                ),
                note(
                    id = 5L,
                    kind = KnowledgeNoteKind.SESSION,
                    status = KnowledgeNoteStatus.ACTIVE,
                    title = null,
                    body = "Session note",
                    categoryId = null,
                    triggerScope = null,
                ),
            )

        val matches = matchImportantNotes(notes, ImportantNoteTarget.WORKOUT, 9L)

        assertEquals(listOf(1L), matches.map { it.noteId })
    }

    @Test
    fun matchImportantNotes_allowsUncategorizedImportantNotes() {
        val notes =
            listOf(
                importantNote(
                    id = 1L,
                    status = KnowledgeNoteStatus.ACTIVE,
                    triggerScope = KnowledgeNoteTriggerScope.BOTH,
                    categoryId = null,
                ),
            )

        val matches = matchImportantNotes(notes, ImportantNoteTarget.EVENT, null)

        assertEquals(listOf(1L), matches.map { it.noteId })
    }

    private fun importantNote(
        id: Long,
        status: KnowledgeNoteStatus,
        triggerScope: KnowledgeNoteTriggerScope?,
        categoryId: Long?,
    ): KnowledgeNote {
        return note(
            id = id,
            kind = KnowledgeNoteKind.IMPORTANT,
            status = status,
            title = "Important",
            body = "Body",
            categoryId = categoryId,
            triggerScope = triggerScope,
        )
    }

    private fun note(
        id: Long,
        kind: KnowledgeNoteKind,
        status: KnowledgeNoteStatus,
        title: String?,
        body: String,
        categoryId: Long?,
        triggerScope: KnowledgeNoteTriggerScope?,
    ): KnowledgeNote {
        return KnowledgeNote(
            id = id,
            kind = kind,
            status = status,
            title = title,
            body = body,
            sourceWorkoutId = null,
            sourceType = null,
            sourceTitle = null,
            categoryId = categoryId,
            triggerScope = triggerScope,
            createdAt = 1L,
            updatedAt = 1L,
        )
    }
}
