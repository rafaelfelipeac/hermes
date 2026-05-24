package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_BODY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_KIND
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TITLE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NOTE_TRIGGER_ENTITY_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues.NOTE_KIND_IMPORTANT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

data class ImportantNoteBadgeUi(
    val noteId: Long,
    val title: String,
    val summary: String? = null,
)

data class ImportantNotesModalState(
    val itemId: Long,
    val itemTitle: String,
    val notes: List<ImportantNoteBadgeUi>,
)

fun enrichWorkoutsWithImportantNotes(
    workouts: List<WorkoutUi>,
    actions: List<UserActionRecord>,
): List<WorkoutUi> {
    val notesByTarget = importantNotesByTarget(actions)

    return workouts.map { workout ->
        val targetKey = noteTargetKey(workout.eventType.toUserActionEntityType(), workout.id)
        workout.copy(
            importantNotes = notesByTarget[targetKey].orEmpty(),
        )
    }
}

fun buildImportantNotesModalState(
    workouts: List<WorkoutUi>,
    itemId: Long?,
): ImportantNotesModalState? {
    val selectedItemId = itemId ?: return null
    val workout = workouts.firstOrNull { it.id == selectedItemId } ?: return null
    val notes = workout.importantNotes

    return if (notes.isEmpty()) {
        null
    } else {
        ImportantNotesModalState(
            itemId = workout.id,
            itemTitle = workout.type,
            notes = notes,
        )
    }
}

private fun importantNotesByTarget(
    actions: List<UserActionRecord>,
): Map<String, List<ImportantNoteBadgeUi>> {
    return actions
        .asSequence()
        .filter { it.entityType == UserActionEntityType.NOTE.name }
        .groupBy { it.entityId ?: return@groupBy Long.MIN_VALUE }
        .mapNotNull { (noteId, records) ->
            if (noteId == Long.MIN_VALUE) return@mapNotNull null

            val current = records.maxWithOrNull(compareBy<UserActionRecord> { it.timestamp }.thenBy { it.id }) ?: return@mapNotNull null
            val metadata = UserActionMetadataSerializer.fromJson(current.metadata)

            if (current.actionType !in activeNoteActions) return@mapNotNull null
            if (metadata[NOTE_KIND] != NOTE_KIND_IMPORTANT) return@mapNotNull null

            val targetEntityType =
                metadata[NOTE_TRIGGER_ENTITY_TYPE]
                    ?.takeIf { it.isNotBlank() }
                    ?.let { entityType ->
                        runCatching { UserActionEntityType.valueOf(entityType) }.getOrNull()
                    }
                    ?: return@mapNotNull null
            val targetEntityId =
                metadata[NOTE_TRIGGER_ENTITY_ID]
                    ?.toLongOrNull()
                    ?: return@mapNotNull null
            val title =
                metadata[NOTE_TITLE]
                    ?.takeIf { it.isNotBlank() }
                    ?: metadata[NOTE_BODY]?.takeIf { it.isNotBlank() }
                    ?: EMPTY
            val summary = metadata[NOTE_BODY]?.takeIf { it.isNotBlank() }

            noteTargetKey(targetEntityType, targetEntityId) to
                ImportantNoteBadgeUi(
                    noteId = noteId,
                    title = title,
                    summary = summary,
                )
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, notes) ->
            notes.sortedByDescending { it.noteId }
        }
}

private fun noteTargetKey(
    entityType: UserActionEntityType,
    entityId: Long,
): String {
    return "${entityType.name}:$entityId"
}

private val activeNoteActions =
    setOf(
        UserActionType.CREATE_NOTE.name,
        UserActionType.UPDATE_NOTE.name,
        UserActionType.RESTORE_NOTE.name,
    )
