package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeBaseSection
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeNoteUi

data class KnowledgeBaseState(
    val query: String = "",
    val selectedSection: KnowledgeBaseSection = KnowledgeBaseSection.SESSION,
    val notes: List<KnowledgeNoteUi> = emptyList(),
    val sessionNotes: List<KnowledgeNoteUi> = emptyList(),
    val notesList: List<KnowledgeNoteUi> = emptyList(),
    val importantNotes: List<KnowledgeNoteUi> = emptyList(),
)
