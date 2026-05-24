package com.rafaelfelipeac.hermes.features.knowledgebase.domain.model

data class KnowledgeNote(
    val id: Long,
    val kind: KnowledgeNoteKind,
    val status: KnowledgeNoteStatus,
    val title: String?,
    val body: String,
    val sourceWorkoutId: Long?,
    val sourceType: KnowledgeNoteSourceType?,
    val sourceTitle: String?,
    val categoryId: Long?,
    val triggerScope: KnowledgeNoteTriggerScope?,
    val createdAt: Long,
    val updatedAt: Long,
)
