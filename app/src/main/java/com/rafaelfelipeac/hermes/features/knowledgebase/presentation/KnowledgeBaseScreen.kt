package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeBaseSection
import com.rafaelfelipeac.hermes.features.knowledgebase.presentation.model.KnowledgeNoteDraft

@Composable
fun KnowledgeBaseScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenEvent: (Long) -> Unit,
    viewModel: KnowledgeBaseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var draft by remember { mutableStateOf<KnowledgeNoteDraft?>(null) }
    val selectedSection = state.selectedSection
    val sectionNotes =
        when (selectedSection) {
            KnowledgeBaseSection.SESSION -> state.sessionNotes
            KnowledgeBaseSection.NOTES -> state.notesList
            KnowledgeBaseSection.IMPORTANT -> state.importantNotes
        }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(horizontal = SpacingXl, vertical = SpacingLg)) {
                KnowledgeBaseHeader(
                    title = stringResource(R.string.knowledge_base_title),
                    subtitle = stringResource(R.string.knowledge_base_session_notes_help),
                    icon = Icons.Outlined.MenuBook,
                )
                TabRow(selectedTabIndex = selectedSection.ordinal) {
                    KnowledgeBaseSection.entries.forEach { section ->
                        Tab(
                            selected = section == selectedSection,
                            onClick = { viewModel.selectSection(section) },
                            text = { Text(stringResource(section.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = SpacingXl, vertical = SpacingLg),
            verticalArrangement = Arrangement.spacedBy(SpacingLg),
        ) {
            item {
                KnowledgeSearchBar(
                    value = state.query,
                    onValueChange = viewModel::updateQuery,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            items(sectionNotes, key = { it.id }) { note ->
                KnowledgeNoteCard(
                    title = note.title,
                    preview = note.preview,
                    subtitle =
                        listOfNotNull(
                            note.sourceLabel,
                            note.categoryName,
                            note.triggerScope?.name,
                        ).joinToString(" • ").ifBlank { null },
                    onClick = {
                        when {
                            note.kind == KnowledgeNoteKind.SESSION && note.sourceWorkoutId != null ->
                                when (note.sourceType) {
                                    KnowledgeNoteSourceType.EVENT -> onOpenEvent(note.sourceWorkoutId)
                                    else -> onOpenWorkout(note.sourceWorkoutId)
                                }
                            else ->
                                draft =
                                    KnowledgeNoteDraft(
                                        id = note.id,
                                        kind = note.kind,
                                        title = note.title,
                                        body = note.body,
                                        sourceWorkoutId = note.sourceWorkoutId,
                                        sourceType = note.sourceType,
                                        sourceTitle = note.sourceTitle,
                                        categoryId = note.categoryId,
                                        triggerScope = note.triggerScope,
                                    )
                        }
                    },
                )
                TextButton(
                    onClick = {
                        if (note.isArchived) {
                            viewModel.unarchive(note.id)
                        } else {
                            viewModel.archive(note.id)
                        }
                    },
                ) {
                    Text(
                        text =
                            if (note.isArchived) {
                                stringResource(R.string.knowledge_base_unarchive)
                            } else {
                                stringResource(R.string.knowledge_base_archive)
                            },
                    )
                }
            }
        }
    }

    KnowledgeNoteEditorDialog(
        draft = draft,
        onDismiss = { draft = null },
        onSave = {
            viewModel.saveNote(it)
            draft = null
        },
    )
}
