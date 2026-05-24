package com.rafaelfelipeac.hermes.features.knowledgebase.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "knowledge_notes")
data class KnowledgeNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val kind: String,
    val status: String,
    val title: String?,
    val body: String,
    val sourceWorkoutId: Long?,
    val sourceType: String?,
    val sourceTitle: String?,
    val categoryId: Long?,
    val triggerScope: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
