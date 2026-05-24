package com.rafaelfelipeac.hermes.features.knowledgebase.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rafaelfelipeac.hermes.core.database.HermesDatabase
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteKind
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteSourceType
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteStatus
import com.rafaelfelipeac.hermes.features.knowledgebase.domain.model.KnowledgeNoteTriggerScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KnowledgeNoteRepositoryImplTest {
    private lateinit var database: HermesDatabase
    private lateinit var repository: KnowledgeNoteRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room.inMemoryDatabaseBuilder(context, HermesDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = KnowledgeNoteRepositoryImpl(database.knowledgeNoteDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsertSessionNote_searchesAndObservesBySource() =
        runTest {
            val noteId =
                repository.upsertSessionNote(
                    sourceWorkoutId = 42L,
                    sourceType = KnowledgeNoteSourceType.WORKOUT,
                    sourceTitle = "Tempo",
                    body = "Hydrate early",
                )

            val note = repository.getNote(noteId)
            assertEquals(KnowledgeNoteKind.SESSION, note?.kind)
            assertEquals("Hydrate early", note?.body)
            assertEquals(42L, note?.sourceWorkoutId)

            val bySource = repository.observeNotesForSource(42L).first()
            assertEquals(1, bySource.size)
            assertEquals(noteId, bySource.single().id)

            val searchResults = repository.searchNotes("hydrate")
            assertEquals(1, searchResults.size)
            assertEquals(noteId, searchResults.single().id)
        }

    @Test
    fun upsertImportantNote_archiveKeepsItSearchable() =
        runTest {
            val noteId =
                repository.upsertNote(
                    kind = KnowledgeNoteKind.IMPORTANT,
                    title = "Fueling",
                    body = "Take gel before speedwork",
                    triggerScope = KnowledgeNoteTriggerScope.WORKOUT,
                    categoryId = 7L,
                )

            repository.setArchived(noteId, true)

            val note = repository.getNote(noteId)
            assertEquals(KnowledgeNoteStatus.ARCHIVED, note?.status)

            val searchResults = repository.searchNotes("gel")
            assertEquals(1, searchResults.size)
            assertTrue(searchResults.single().status == KnowledgeNoteStatus.ARCHIVED)
        }
}
