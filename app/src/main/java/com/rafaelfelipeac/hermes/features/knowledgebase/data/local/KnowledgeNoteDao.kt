package com.rafaelfelipeac.hermes.features.knowledgebase.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeNoteDao {
    @Query("SELECT * FROM knowledge_notes ORDER BY updatedAt DESC, createdAt DESC, id DESC")
    fun observeAll(): Flow<List<KnowledgeNoteEntity>>

    @Query("SELECT * FROM knowledge_notes ORDER BY updatedAt DESC, createdAt DESC, id DESC")
    suspend fun getAll(): List<KnowledgeNoteEntity>

    @Query("SELECT * FROM knowledge_notes WHERE id = :noteId LIMIT 1")
    suspend fun getById(noteId: Long): KnowledgeNoteEntity?

    @Query("SELECT * FROM knowledge_notes WHERE sourceWorkoutId = :sourceWorkoutId ORDER BY updatedAt DESC, createdAt DESC, id DESC")
    fun observeForSource(sourceWorkoutId: Long): Flow<List<KnowledgeNoteEntity>>

    @Query(
        "SELECT * FROM knowledge_notes WHERE " +
            "LOWER(COALESCE(title, '')) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(body) LIKE '%' || LOWER(:query) || '%' OR " +
            "LOWER(COALESCE(sourceTitle, '')) LIKE '%' || LOWER(:query) || '%' " +
            "ORDER BY updatedAt DESC, createdAt DESC, id DESC",
    )
    suspend fun search(query: String): List<KnowledgeNoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: KnowledgeNoteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<KnowledgeNoteEntity>): List<Long>

    @Update
    suspend fun update(note: KnowledgeNoteEntity)

    @Query("UPDATE knowledge_notes SET status = :status, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updateStatus(
        noteId: Long,
        status: String,
        updatedAt: Long,
    )

    @Query("DELETE FROM knowledge_notes")
    suspend fun deleteAll()
}
