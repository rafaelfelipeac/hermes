package com.rafaelfelipeac.hermes.features.knowledgebase.domain

import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.ImportantNoteTarget
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteMatch
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope

fun matchImportantNotes(
    notes: List<KnowledgeNote>,
    target: ImportantNoteTarget,
    categoryId: Long?,
): List<KnowledgeNoteMatch> {
    return notes.asSequence()
        .filter { it.kind == KnowledgeNoteKind.IMPORTANT }
        .filter { it.status == KnowledgeNoteStatus.ACTIVE }
        .filter { note ->
            val scope = note.triggerScope ?: return@filter false
            when (target) {
                ImportantNoteTarget.WORKOUT -> scope == KnowledgeNoteTriggerScope.WORKOUT || scope == KnowledgeNoteTriggerScope.BOTH
                ImportantNoteTarget.EVENT -> scope == KnowledgeNoteTriggerScope.EVENT || scope == KnowledgeNoteTriggerScope.BOTH
            }
        }
        .filter { note ->
            note.categoryId == null ||
                note.categoryId == UNCATEGORIZED_ID ||
                note.categoryId == categoryId
        }
        .map { note ->
            KnowledgeNoteMatch(
                noteId = note.id,
                noteTitle = note.title.orEmpty(),
                reason = buildMatchReason(target, note.triggerScope),
            )
        }
        .toList()
}

private fun buildMatchReason(
    target: ImportantNoteTarget,
    scope: KnowledgeNoteTriggerScope?,
): String {
    return when (target) {
        ImportantNoteTarget.WORKOUT ->
            when (scope) {
                KnowledgeNoteTriggerScope.WORKOUT -> "Matches workouts."
                KnowledgeNoteTriggerScope.BOTH -> "Matches workouts and events."
                KnowledgeNoteTriggerScope.EVENT, null -> "Matches events only."
            }
        ImportantNoteTarget.EVENT ->
            when (scope) {
                KnowledgeNoteTriggerScope.EVENT -> "Matches events."
                KnowledgeNoteTriggerScope.BOTH -> "Matches workouts and events."
                KnowledgeNoteTriggerScope.WORKOUT, null -> "Matches workouts only."
            }
    }
}
