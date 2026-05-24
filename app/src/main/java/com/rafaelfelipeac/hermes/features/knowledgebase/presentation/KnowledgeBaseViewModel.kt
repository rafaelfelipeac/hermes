package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.repository.KnowledgeNoteRepository
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeBaseSection
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeNoteDraft
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeNoteUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KnowledgeBaseViewModel
    @Inject
    constructor(
        private val repository: KnowledgeNoteRepository,
        private val categoryRepository: CategoryRepository,
        private val stringProvider: StringProvider,
    ) : ViewModel() {
        private val query = MutableStateFlow("")
        private val selectedSection = MutableStateFlow(KnowledgeBaseSection.SESSION)
        private val categoriesFlow = categoryRepository.observeCategories()

        val state: StateFlow<KnowledgeBaseState> =
            combine(repository.observeNotes(), categoriesFlow, query, selectedSection) { notes, categories, currentQuery, currentSection ->
                val categoriesById = categories.associate { it.id to it.toUi() }
                val filtered = notes.map { note -> note.toUi(categoriesById) }.filter { noteUi ->
                    matchesQuery(noteUi, currentQuery)
                }

                KnowledgeBaseState(
                    query = currentQuery,
                    selectedSection = currentSection,
                    notes = filtered,
                    sessionNotes = filtered.filter { it.kind == KnowledgeNoteKind.SESSION },
                    notesList = filtered.filter { it.kind == KnowledgeNoteKind.NOTE },
                    importantNotes = filtered.filter { it.kind == KnowledgeNoteKind.IMPORTANT },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = KnowledgeBaseState(),
            )

        fun updateQuery(value: String) {
            query.value = value
        }

        fun selectSection(section: KnowledgeBaseSection) {
            selectedSection.value = section
        }

        fun saveNote(draft: KnowledgeNoteDraft) {
            if (draft.kind == KnowledgeNoteKind.SESSION) {
                val sourceId = draft.sourceWorkoutId ?: return
                val body = draft.body.trim()
                if (body.isBlank()) return

                viewModelScope.launch {
                    repository.upsertSessionNote(
                        sourceWorkoutId = sourceId,
                        sourceType = draft.sourceType ?: KnowledgeNoteSourceType.WORKOUT,
                        sourceTitle = draft.sourceTitle,
                        body = body,
                        noteId = draft.id,
                    )
                }
                return
            }

            val title = draft.title.trim()
            val body = draft.body.trim()
            if (title.isBlank() || body.isBlank()) return

            viewModelScope.launch {
                repository.upsertNote(
                    kind = draft.kind,
                    title = title,
                    body = body,
                    triggerScope = if (draft.kind == KnowledgeNoteKind.IMPORTANT) draft.triggerScope else null,
                    categoryId = if (draft.kind == KnowledgeNoteKind.IMPORTANT) draft.categoryId else null,
                    noteId = draft.id,
                )
            }
        }

        fun archive(noteId: Long) {
            viewModelScope.launch {
                repository.setArchived(noteId, true)
            }
        }

        fun unarchive(noteId: Long) {
            viewModelScope.launch {
                repository.setArchived(noteId, false)
            }
        }

        private fun KnowledgeNote.toUi(categoriesById: Map<Long, CategoryUi>): KnowledgeNoteUi {
            val sourceLabel =
                when (sourceType) {
                    KnowledgeNoteSourceType.EVENT ->
                        sourceTitle?.takeIf { it.isNotBlank() }
                            ?: stringProvider.get(R.string.knowledge_base_session_source_event)
                    KnowledgeNoteSourceType.WORKOUT ->
                        sourceTitle?.takeIf { it.isNotBlank() }
                            ?: stringProvider.get(R.string.knowledge_base_session_source_workout)
                    null -> null
                }
            val preview = body.take(120)
            val categoryName = categoryId?.let(categoriesById::get)?.name

            return KnowledgeNoteUi(
                id = id,
                kind = kind,
                status = status,
                title = title ?: sourceLabel.orEmpty(),
                body = body,
                sourceWorkoutId = sourceWorkoutId,
                sourceType = sourceType,
                sourceTitle = sourceTitle,
                categoryId = categoryId,
                categoryName = categoryName,
                triggerScope = triggerScope,
                preview = preview,
                sourceLabel = sourceLabel,
            )
        }

        private fun matchesQuery(
            note: KnowledgeNoteUi,
            query: String,
        ): Boolean {
            val normalized = query.trim()
            if (normalized.isBlank()) return true
            val haystack =
                listOfNotNull(
                    note.title,
                    note.body,
                    note.preview,
                    note.sourceLabel,
                    note.categoryName,
                    note.triggerScope?.name,
                    note.kind.name,
                ).joinToString(" ").lowercase()
            return normalized.lowercase() in haystack
        }
    }
