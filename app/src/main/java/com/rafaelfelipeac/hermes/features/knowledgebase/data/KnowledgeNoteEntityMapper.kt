package com.rafaelfelipeac.hermes.features.knowledgebase.data

import com.rafaelfelipeac.hermes.features.knowledgebase.data.local.KnowledgeNoteEntity
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope

internal fun KnowledgeNoteEntity.toDomain(): KnowledgeNote {
    return KnowledgeNote(
        id = id,
        kind = KnowledgeNoteKind.valueOf(kind),
        status = KnowledgeNoteStatus.valueOf(status),
        title = title,
        body = body,
        sourceWorkoutId = sourceWorkoutId,
        sourceType = sourceType?.let(KnowledgeNoteSourceType::valueOf),
        sourceTitle = sourceTitle,
        categoryId = categoryId,
        triggerScope = triggerScope?.let(KnowledgeNoteTriggerScope::valueOf),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

internal fun KnowledgeNote.toEntity(): KnowledgeNoteEntity {
    return KnowledgeNoteEntity(
        id = id,
        kind = kind.name,
        status = status.name,
        title = title,
        body = body,
        sourceWorkoutId = sourceWorkoutId,
        sourceType = sourceType?.name,
        sourceTitle = sourceTitle,
        categoryId = categoryId,
        triggerScope = triggerScope?.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
