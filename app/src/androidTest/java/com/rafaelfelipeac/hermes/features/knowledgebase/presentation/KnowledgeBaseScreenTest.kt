package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.activity.ComponentActivity
import android.content.Context
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNote
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.repository.KnowledgeNoteRepository
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

private const val SESSION_NOTE_TITLE = "Keep cadence relaxed"
private const val NOTE_TITLE = "Hydration reminder"
private const val IMPORTANT_NOTE_TITLE = "Race week reminder"
private const val SESSION_SOURCE_WORKOUT_ID = 101L

class KnowledgeBaseScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun searchFiltersNotesAcrossSections() {
        val viewModel = knowledgeBaseViewModel()

        composeRule.setContent {
            KnowledgeBaseScreen(
                onBack = {},
                onOpenWorkout = {},
                onOpenEvent = {},
                viewModel = viewModel,
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.knowledge_base_notes_title)).performClick()
        composeRule.onNodeWithTag(KNOWLEDGE_BASE_SEARCH_TAG).performTextInput("Hydration")
        composeRule.onNodeWithText(NOTE_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithText("Gear checklist").assertCountEquals(0)
    }

    @Test
    fun editingAStandaloneNoteUpdatesItsTitle() {
        val viewModel = knowledgeBaseViewModel()

        composeRule.setContent {
            KnowledgeBaseScreen(
                onBack = {},
                onOpenWorkout = {},
                onOpenEvent = {},
                viewModel = viewModel,
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.knowledge_base_notes_title)).performClick()
        composeRule.onNodeWithText(NOTE_TITLE).performClick()
        composeRule.onNodeWithTag(KNOWLEDGE_BASE_EDITOR_TITLE_FIELD_TAG).performTextClearance()
        composeRule.onNodeWithTag(KNOWLEDGE_BASE_EDITOR_TITLE_FIELD_TAG).performTextInput("Hydration plan")
        composeRule.onNodeWithTag(KNOWLEDGE_BASE_EDITOR_BODY_FIELD_TAG).performTextClearance()
        composeRule.onNodeWithTag(KNOWLEDGE_BASE_EDITOR_BODY_FIELD_TAG).performTextInput("Carry electrolytes on long runs.")
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.save_changes)).performClick()

        composeRule.onNodeWithText("Hydration plan").assertIsDisplayed()
    }

    @Test
    fun archiveToggleUpdatesTheNoteState() {
        val viewModel = knowledgeBaseViewModel()

        composeRule.setContent {
            KnowledgeBaseScreen(
                onBack = {},
                onOpenWorkout = {},
                onOpenEvent = {},
                viewModel = viewModel,
            )
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.knowledge_base_notes_title)).performClick()
        composeRule.onNodeWithText(NOTE_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.knowledge_base_archive)).performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.knowledge_base_unarchive)).assertIsDisplayed()
    }

    @Test
    fun openingSessionNoteSourceDispatchesWorkoutCallback() {
        val viewModel = knowledgeBaseViewModel()
        var workoutId: Long? = null

        composeRule.setContent {
            KnowledgeBaseScreen(
                onBack = {},
                onOpenWorkout = { workoutId = it },
                onOpenEvent = {},
                viewModel = viewModel,
            )
        }

        composeRule.onNodeWithText(SESSION_NOTE_TITLE).performClick()

        composeRule.runOnIdle {
            assertEquals(SESSION_SOURCE_WORKOUT_ID, workoutId)
        }
    }

    private fun knowledgeBaseViewModel(): KnowledgeBaseViewModel {
        return KnowledgeBaseViewModel(
            repository = FakeKnowledgeNoteRepository(),
            categoryRepository = FakeCategoryRepository(),
            stringProvider = FakeStringProvider(composeRule.activity),
        )
    }

    private class FakeStringProvider(
        private val context: Context,
    ) : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String = context.getString(id, *args)

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = get(id, *args)
    }

    private class FakeCategoryRepository : CategoryRepository {
        override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

        override suspend fun getCategories(): List<Category> = emptyList()

        override suspend fun getCategory(id: Long): Category? = null

        override suspend fun getCount(): Int = 0

        override suspend fun insertCategory(category: Category): Long = category.id

        override suspend fun insertCategories(categories: List<Category>): List<Long> = categories.map { it.id }

        override suspend fun updateCategory(category: Category) = Unit

        override suspend fun updateCategoryName(
            id: Long,
            name: String,
        ) = Unit

        override suspend fun updateCategoryColor(
            id: Long,
            colorId: String,
        ) = Unit

        override suspend fun updateCategoryVisibility(
            id: Long,
            isHidden: Boolean,
        ) = Unit

        override suspend fun updateCategorySortOrder(
            id: Long,
            sortOrder: Int,
        ) = Unit

        override suspend fun deleteCategory(id: Long) = Unit
    }

    private class FakeKnowledgeNoteRepository : KnowledgeNoteRepository {
        private val notesFlow =
            MutableStateFlow(
                listOf(
                    KnowledgeNote(
                        id = 1L,
                        kind = KnowledgeNoteKind.SESSION,
                        status = KnowledgeNoteStatus.ACTIVE,
                        title = SESSION_NOTE_TITLE,
                        body = "Ease off the pace on long runs when the weather is warm.",
                        sourceWorkoutId = SESSION_SOURCE_WORKOUT_ID,
                        sourceType = KnowledgeNoteSourceType.WORKOUT,
                        sourceTitle = "Workout 101",
                        categoryId = null,
                        triggerScope = null,
                        createdAt = 0L,
                        updatedAt = 0L,
                    ),
                    KnowledgeNote(
                        id = 2L,
                        kind = KnowledgeNoteKind.NOTE,
                        status = KnowledgeNoteStatus.ACTIVE,
                        title = NOTE_TITLE,
                        body = "Start drinking early on hot days and bring electrolytes for longer sessions.",
                        sourceWorkoutId = null,
                        sourceType = null,
                        sourceTitle = null,
                        categoryId = null,
                        triggerScope = null,
                        createdAt = 0L,
                        updatedAt = 0L,
                    ),
                    KnowledgeNote(
                        id = 3L,
                        kind = KnowledgeNoteKind.IMPORTANT,
                        status = KnowledgeNoteStatus.ACTIVE,
                        title = IMPORTANT_NOTE_TITLE,
                        body = "Protect the last 3 days before a race from hard sessions.",
                        sourceWorkoutId = null,
                        sourceType = null,
                        sourceTitle = null,
                        categoryId = null,
                        triggerScope = KnowledgeNoteTriggerScope.BOTH,
                        createdAt = 0L,
                        updatedAt = 0L,
                    ),
                ),
            )

        override fun observeNotes(): Flow<List<KnowledgeNote>> = notesFlow

        override fun observeNote(noteId: Long): Flow<KnowledgeNote?> =
            flowOf(notesFlow.value.firstOrNull { it.id == noteId })

        override fun observeNotesForSource(sourceWorkoutId: Long): Flow<List<KnowledgeNote>> =
            flowOf(notesFlow.value.filter { it.sourceWorkoutId == sourceWorkoutId })

        override suspend fun getNotes(): List<KnowledgeNote> = notesFlow.value

        override suspend fun getNote(noteId: Long): KnowledgeNote? = notesFlow.value.firstOrNull { it.id == noteId }

        override suspend fun searchNotes(query: String): List<KnowledgeNote> {
            val normalized = query.trim().lowercase()
            return notesFlow.value.filter {
                listOfNotNull(
                    it.title,
                    it.body,
                    it.sourceTitle,
                    it.triggerScope?.name,
                    it.kind.name,
                ).joinToString(" ").lowercase().contains(normalized)
            }
        }

        override suspend fun upsertSessionNote(
            sourceWorkoutId: Long,
            sourceType: KnowledgeNoteSourceType,
            sourceTitle: String?,
            body: String,
            noteId: Long?,
        ): Long {
            val id = noteId ?: (notesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
            val note =
                KnowledgeNote(
                    id = id,
                    kind = KnowledgeNoteKind.SESSION,
                    status = KnowledgeNoteStatus.ACTIVE,
                    title = sourceTitle,
                    body = body,
                    sourceWorkoutId = sourceWorkoutId,
                    sourceType = sourceType,
                    sourceTitle = sourceTitle,
                    categoryId = null,
                    triggerScope = null,
                    createdAt = 0L,
                    updatedAt = 0L,
                )
            notesFlow.value = notesFlow.value.filterNot { it.id == id } + note
            return id
        }

        override suspend fun upsertNote(
            kind: KnowledgeNoteKind,
            title: String,
            body: String,
            triggerScope: KnowledgeNoteTriggerScope?,
            categoryId: Long?,
            noteId: Long?,
        ): Long {
            val id = noteId ?: (notesFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
            val existing = notesFlow.value.firstOrNull { it.id == id }
            val note =
                KnowledgeNote(
                    id = id,
                    kind = kind,
                    status = existing?.status ?: KnowledgeNoteStatus.ACTIVE,
                    title = title,
                    body = body,
                    sourceWorkoutId = existing?.sourceWorkoutId,
                    sourceType = existing?.sourceType,
                    sourceTitle = existing?.sourceTitle,
                    categoryId = categoryId,
                    triggerScope = triggerScope,
                    createdAt = existing?.createdAt ?: 0L,
                    updatedAt = 0L,
                )
            notesFlow.value = notesFlow.value.filterNot { it.id == id } + note
            return id
        }

        override suspend fun setArchived(
            noteId: Long,
            archived: Boolean,
        ) {
            notesFlow.value =
                notesFlow.value.map { note ->
                    if (note.id == noteId) {
                        note.copy(
                            status = if (archived) KnowledgeNoteStatus.ARCHIVED else KnowledgeNoteStatus.ACTIVE,
                        )
                    } else {
                        note
                    }
                }
        }

        override suspend fun replaceAll(notes: List<KnowledgeNote>) {
            notesFlow.value = notes
        }
    }
}
