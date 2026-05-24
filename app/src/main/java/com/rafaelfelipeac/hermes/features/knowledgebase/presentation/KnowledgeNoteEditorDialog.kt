package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeNoteDraft

internal const val KNOWLEDGE_BASE_EDITOR_TITLE_TAG = "knowledge_base_editor_title"
internal const val KNOWLEDGE_BASE_EDITOR_TITLE_FIELD_TAG = "knowledge_base_editor_title_field"
internal const val KNOWLEDGE_BASE_EDITOR_BODY_FIELD_TAG = "knowledge_base_editor_body_field"
internal const val KNOWLEDGE_BASE_EDITOR_TRIGGER_FIELD_TAG = "knowledge_base_editor_trigger_field"

@Composable
internal fun KnowledgeNoteEditorDialog(
    draft: KnowledgeNoteDraft?,
    onDismiss: () -> Unit,
    onSave: (KnowledgeNoteDraft) -> Unit,
) {
    if (draft == null) return

    var title by remember(draft.id) { mutableStateOf(draft.title) }
    var body by remember(draft.id) { mutableStateOf(draft.body) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text =
                    when (draft.kind) {
                        KnowledgeNoteKind.SESSION -> stringResource(R.string.knowledge_base_session_notes_title)
                        KnowledgeNoteKind.NOTE -> stringResource(R.string.knowledge_base_notes_title)
                        KnowledgeNoteKind.IMPORTANT -> stringResource(R.string.knowledge_base_important_notes_title)
                    },
                modifier = Modifier.testTag(KNOWLEDGE_BASE_EDITOR_TITLE_TAG),
            )
        },
        text = {
            Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(SpacingMd)) {
                if (draft.kind != KnowledgeNoteKind.SESSION) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.knowledge_base_note_title_label)) },
                        modifier = Modifier.fillMaxWidth().testTag(KNOWLEDGE_BASE_EDITOR_TITLE_FIELD_TAG),
                    )
                }
                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text(stringResource(R.string.knowledge_base_note_body_label)) },
                    modifier = Modifier.fillMaxWidth().testTag(KNOWLEDGE_BASE_EDITOR_BODY_FIELD_TAG),
                )
                if (draft.kind == KnowledgeNoteKind.IMPORTANT) {
                    OutlinedTextField(
                        value = draft.triggerScope?.name.orEmpty(),
                        onValueChange = {},
                        label = { Text(stringResource(R.string.knowledge_base_trigger_label)) },
                        modifier = Modifier.fillMaxWidth().testTag(KNOWLEDGE_BASE_EDITOR_TRIGGER_FIELD_TAG),
                        enabled = false,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        draft.copy(
                            title = title,
                            body = body,
                            triggerScope = draft.triggerScope ?: KnowledgeNoteTriggerScope.BOTH,
                        ),
                    )
                },
            ) {
                Text(stringResource(R.string.save_changes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_workout_cancel))
            }
        },
    )
}
