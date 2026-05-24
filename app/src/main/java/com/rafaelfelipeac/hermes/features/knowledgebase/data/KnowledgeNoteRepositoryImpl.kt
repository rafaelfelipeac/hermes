package com.rafaelfelipeac.hermes.features.knowledgebase.data

import com.rafaelfelipeac.hermes.features.knowledgebase.data.local.KnowledgeNoteDao
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.repository.KnowledgeNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KnowledgeNoteRepositoryImpl
    @Inject
    constructor(
        private val dao: KnowledgeNoteDao,
    ) : KnowledgeNoteRepository {
        override fun observeNotes(): Flow<List<KnowledgeNote>> {
            return dao.observeAll().map { notes -> notes.map { it.toDomain() } }
        }

        override fun observeNote(noteId: Long): Flow<KnowledgeNote?> {
            return observeNotes().map { notes -> notes.firstOrNull { it.id == noteId } }
        }

        override fun observeNotesForSource(sourceWorkoutId: Long): Flow<List<KnowledgeNote>> {
            return dao.observeForSource(sourceWorkoutId).map { notes -> notes.map { it.toDomain() } }
        }

        override suspend fun getNotes(): List<KnowledgeNote> {
            return dao.getAll().map { it.toDomain() }
        }

        override suspend fun getNote(noteId: Long): KnowledgeNote? {
            return dao.getById(noteId)?.toDomain()
        }

        override suspend fun searchNotes(query: String): List<KnowledgeNote> {
            return dao.search(query.trim()).map { it.toDomain() }
        }

        override suspend fun upsertSessionNote(
            sourceWorkoutId: Long,
            sourceType: KnowledgeNoteSourceType,
            sourceTitle: String?,
            body: String,
            noteId: Long?,
        ): Long {
            val now = Instant.now().toEpochMilli()
            val entity =
                KnowledgeNote(
                    id = noteId ?: 0L,
                    kind = KnowledgeNoteKind.SESSION,
                    status = KnowledgeNoteStatus.ACTIVE,
                    title = null,
                    body = body,
                    sourceWorkoutId = sourceWorkoutId,
                    sourceType = sourceType,
                    sourceTitle = sourceTitle,
                    categoryId = null,
                    triggerScope = null,
                    createdAt = noteId?.let { dao.getById(it)?.createdAt } ?: now,
                    updatedAt = now,
                ).toEntity()

            return if (noteId == null) {
                dao.insert(entity)
            } else {
                dao.insert(entity)
            }
        }

        override suspend fun upsertNote(
            kind: KnowledgeNoteKind,
            title: String,
            body: String,
            triggerScope: KnowledgeNoteTriggerScope?,
            categoryId: Long?,
            noteId: Long?,
        ): Long {
            val now = Instant.now().toEpochMilli()
            val existing = noteId?.let { dao.getById(it) }?.toDomain()
            val entity =
                KnowledgeNote(
                    id = noteId ?: 0L,
                    kind = kind,
                    status = existing?.status ?: KnowledgeNoteStatus.ACTIVE,
                    title = title,
                    body = body,
                    sourceWorkoutId = null,
                    sourceType = null,
                    sourceTitle = null,
                    categoryId = categoryId,
                    triggerScope = triggerScope,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ).toEntity()

            return dao.insert(entity)
        }

        override suspend fun setArchived(
            noteId: Long,
            archived: Boolean,
        ) {
            dao.updateStatus(
                noteId = noteId,
                status = if (archived) KnowledgeNoteStatus.ARCHIVED.name else KnowledgeNoteStatus.ACTIVE.name,
                updatedAt = Instant.now().toEpochMilli(),
            )
        }

        override suspend fun replaceAll(notes: List<KnowledgeNote>) {
            dao.deleteAll()
            if (notes.isNotEmpty()) {
                dao.insertAll(notes.map { it.toEntity() })
            }
        }
    }
