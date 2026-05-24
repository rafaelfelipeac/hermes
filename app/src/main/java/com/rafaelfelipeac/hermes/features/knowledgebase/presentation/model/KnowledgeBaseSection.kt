package com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model

import androidx.annotation.StringRes
import com.rafaelfelipeac.hermes.R

enum class KnowledgeBaseSection(
    @StringRes val labelRes: Int,
) {
    SESSION(R.string.knowledge_base_session_notes_title),
    NOTES(R.string.knowledge_base_notes_title),
    IMPORTANT(R.string.knowledge_base_important_notes_title),
}
