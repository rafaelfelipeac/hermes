package com.rafaelfelipeac.hermes.features.knowledgebase.domain.repository

import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import kotlinx.coroutines.flow.Flow

interface KnowledgeNoteRepository {
    fun observeNotes(): Flow<List<KnowledgeNote>>

    fun observeNote(noteId: Long): Flow<KnowledgeNote?>

    fun observeNotesForSource(sourceWorkoutId: Long): Flow<List<KnowledgeNote>>

    suspend fun getNotes(): List<KnowledgeNote>

    suspend fun getNote(noteId: Long): KnowledgeNote?

    suspend fun searchNotes(query: String): List<KnowledgeNote>

    suspend fun upsertSessionNote(
        sourceWorkoutId: Long,
        sourceType: KnowledgeNoteSourceType,
        sourceTitle: String?,
        body: String,
        noteId: Long? = null,
    ): Long

    suspend fun upsertNote(
        kind: KnowledgeNoteKind,
        title: String,
        body: String,
        triggerScope: KnowledgeNoteTriggerScope? = null,
        categoryId: Long? = null,
        noteId: Long? = null,
    ): Long

    suspend fun setArchived(
        noteId: Long,
        archived: Boolean,
    )

    suspend fun replaceAll(notes: List<KnowledgeNote>)
}
