package com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model

import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope

data class KnowledgeNoteDraft(
    val id: Long? = null,
    val kind: KnowledgeNoteKind,
    val title: String = "",
    val body: String = "",
    val sourceWorkoutId: Long? = null,
    val sourceType: KnowledgeNoteSourceType? = null,
    val sourceTitle: String? = null,
    val categoryId: Long? = null,
    val triggerScope: KnowledgeNoteTriggerScope? = null,
)
