package com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model

import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope

data class KnowledgeNoteUi(
    val id: Long,
    val kind: KnowledgeNoteKind,
    val status: KnowledgeNoteStatus,
    val title: String,
    val body: String,
    val sourceWorkoutId: Long?,
    val sourceType: KnowledgeNoteSourceType?,
    val sourceTitle: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val triggerScope: KnowledgeNoteTriggerScope?,
    val preview: String,
    val sourceLabel: String?,
) {
    val isSession: Boolean = kind == KnowledgeNoteKind.SESSION
    val isArchived: Boolean = status == KnowledgeNoteStatus.ARCHIVED
}
