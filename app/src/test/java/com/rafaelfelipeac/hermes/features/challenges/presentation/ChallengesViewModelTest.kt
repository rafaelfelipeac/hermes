@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.challenges.presentation

import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_FIRST_COMPLETION_AT
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_PROGRESS_QUANTITY
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CHALLENGE_RECOVERED
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.RESTORE_CHALLENGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_CHALLENGE_PROGRESS_ENTRY
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeDateBounds
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun invalidEditorSave_returnsFalseAndDoesNotPersist() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeChallengeRepository()
            val viewModel = createViewModel(repository)

            viewModel.beginCreateChallenge()
            viewModel.updateEditorUnit("km")
            viewModel.updateEditorTargetQuantity(ChallengeQuantity.format(10_000L, Locale.getDefault()))

            assertFalse(viewModel.saveEditorChallenge())
            runCurrent()
            assertTrue(repository.challenges.value.isEmpty())
        }

    @Test
    fun editingArchivedChallenge_preservesLifecycleInvariant() =
        runTest(mainDispatcherRule.testDispatcher) {
            val archivedAt = Instant.parse("2026-08-02T12:00:00Z")
            val archived = sampleChallenge(lifecycle = ChallengeLifecycle.ARCHIVED, archivedAt = archivedAt)
            val repository = FakeChallengeRepository(initialChallenges = listOf(archived))
            val viewModel = createViewModel(repository)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }

            viewModel.beginEditChallenge(archived.id)
            runCurrent()
            viewModel.updateEditorTitle("Updated challenge")

            assertTrue(viewModel.saveEditorChallenge())
            runCurrent()

            assertEquals(ChallengeLifecycle.ARCHIVED, repository.challenges.value.single().lifecycle)
            assertEquals(archivedAt, repository.challenges.value.single().archivedAt)
            stateJob.cancel()
        }

    @Test
    fun updateProgressEntry_logsReplayMetadata() =
        runTest(mainDispatcherRule.testDispatcher) {
            val challenge = sampleChallenge()
            val entry = sampleProgressEntry(quantity = 2_000L)
            val repository = FakeChallengeRepository(listOf(challenge), listOf(entry))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }
            viewModel.selectChallenge(challenge.id)
            runCurrent()
            val updatedQuantity = 4_000L

            assertTrue(
                viewModel.updateProgressEntry(
                    entryId = entry.id,
                    quantityText = ChallengeQuantity.format(updatedQuantity, Locale.getDefault()),
                    entryDate = TODAY,
                ),
            )
            runCurrent()

            val action = logger.actions.single()
            assertEquals(UPDATE_CHALLENGE_PROGRESS_ENTRY, action.actionType)
            assertEquals(updatedQuantity.toString(), action.metadata?.get(CHALLENGE_PROGRESS_QUANTITY))
            assertEquals(TODAY.toString(), action.metadata?.get(CHALLENGE_PROGRESS_DATE))
            stateJob.cancel()
        }

    @Test
    fun addProgressEntry_logsFreshRecoveryCalculation() =
        runTest(mainDispatcherRule.testDispatcher) {
            val challenge = sampleChallenge()
            val firstEntry = sampleProgressEntry(quantity = 500L, entryDate = LocalDate.of(2026, 8, 1))
            val repository = FakeChallengeRepository(listOf(challenge), listOf(firstEntry))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }
            viewModel.selectChallenge(challenge.id)
            runCurrent()

            assertTrue(
                viewModel.addProgressEntry(
                    challengeId = challenge.id,
                    quantityText = ChallengeQuantity.format(9_500L, Locale.getDefault()),
                    entryDate = TODAY,
                ),
            )
            runCurrent()

            val metadata = logger.actions.single().metadata
            assertEquals(true.toString(), metadata?.get(CHALLENGE_RECOVERED))
            assertNotNull(metadata?.get(CHALLENGE_FIRST_COMPLETION_AT)?.takeIf { it.isNotBlank() })
            stateJob.cancel()
        }

    @Test
    fun undoChallengeDeletion_logsDedicatedRestoreAction() =
        runTest(mainDispatcherRule.testDispatcher) {
            val challenge = sampleChallenge()
            val repository = FakeChallengeRepository(listOf(challenge))
            val logger = RecordingUserActionLogger()
            val viewModel = createViewModel(repository, logger)
            val stateJob = backgroundScope.launch { viewModel.state.collect { } }
            runCurrent()

            viewModel.deleteChallenge(challenge.id)
            runCurrent()
            viewModel.restoreUndo()
            runCurrent()

            assertEquals(RESTORE_CHALLENGE, logger.actions.last().actionType)
            assertEquals(challenge, repository.challenges.value.single())
            stateJob.cancel()
        }

    private fun createViewModel(
        repository: FakeChallengeRepository,
        logger: RecordingUserActionLogger = RecordingUserActionLogger(),
    ): ChallengesViewModel {
        return ChallengesViewModel(
            repository = repository,
            userActionLogger = logger,
            stringProvider = FakeStringProvider,
            clock = FIXED_CLOCK,
        )
    }

    private fun sampleChallenge(
        lifecycle: ChallengeLifecycle = ChallengeLifecycle.ACTIVE,
        archivedAt: Instant? = null,
    ): Challenge {
        return Challenge(
            id = 1L,
            title = "August distance",
            targetQuantity = 10_000L,
            unit = "km",
            startDate = LocalDate.of(2026, 8, 1),
            endDate = LocalDate.of(2026, 8, 10),
            lifecycle = lifecycle,
            archivedAt = archivedAt,
            createdAt = Instant.parse("2026-08-01T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T12:00:00Z"),
        )
    }

    private fun sampleProgressEntry(
        quantity: Long,
        entryDate: LocalDate = TODAY,
    ): ChallengeProgressEntry {
        return ChallengeProgressEntry(
            id = 10L,
            challengeId = 1L,
            quantity = quantity,
            entryDate = entryDate,
            occurredAt = Instant.parse("2026-08-01T13:00:00Z"),
            createdAt = Instant.parse("2026-08-01T13:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T13:00:00Z"),
        )
    }

    private class RecordingUserActionLogger : UserActionLogger {
        val actions = CopyOnWriteArrayList<UserAction>()

        override suspend fun log(action: UserAction) {
            actions += action
        }
    }

    private object FakeStringProvider : StringProvider {
        override fun get(
            id: Int,
            vararg args: Any,
        ): String = id.toString()

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String = id.toString()
    }

    private class FakeChallengeRepository(
        initialChallenges: List<Challenge> = emptyList(),
        initialEntries: List<ChallengeProgressEntry> = emptyList(),
    ) : ChallengeRepository {
        val challenges = MutableStateFlow(initialChallenges)
        private val entries = MutableStateFlow(initialEntries)

        override fun observeActiveChallenges(): Flow<List<Challenge>> =
            challenges.map { items -> items.filter { it.lifecycle == ChallengeLifecycle.ACTIVE } }

        override fun observeArchivedChallenges(): Flow<List<Challenge>> =
            challenges.map { items -> items.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED } }

        override fun observeChallenge(id: Long): Flow<Challenge?> =
            challenges.map { items ->
                items.firstOrNull { it.id == id }
            }

        override fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntry>> =
            entries.map { items -> items.filter { it.challengeId == challengeId } }

        override suspend fun getActiveChallenges(): List<Challenge> =
            challenges.value.filter { challenge ->
                challenge.lifecycle == ChallengeLifecycle.ACTIVE
            }

        override suspend fun getArchivedChallenges(): List<Challenge> =
            challenges.value.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED }

        override suspend fun getChallenge(id: Long): Challenge? = challenges.value.firstOrNull { it.id == id }

        override suspend fun getChallengeDateBounds(id: Long): ChallengeDateBounds? =
            getChallenge(id)?.let { ChallengeDateBounds(it.startDate, it.endDate) }

        override suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntry> =
            entries.value.filter { it.challengeId == challengeId }

        override suspend fun getAllChallenges(): List<Challenge> = challenges.value

        override suspend fun getAllProgressEntries(): List<ChallengeProgressEntry> = entries.value

        override suspend fun insertChallenge(challenge: Challenge): Long {
            val id = challenge.id.takeIf { it != 0L } ?: ((challenges.value.maxOfOrNull { it.id } ?: 0L) + 1L)
            challenges.value = challenges.value.filterNot { it.id == id } + challenge.copy(id = id)
            return id
        }

        override suspend fun updateChallenge(challenge: Challenge) {
            challenges.value = challenges.value.map { if (it.id == challenge.id) challenge else it }
        }

        override suspend fun archiveChallenge(
            id: Long,
            archivedAt: Instant,
        ) {
            challenges.value =
                challenges.value.map {
                    if (it.id == id) it.copy(lifecycle = ChallengeLifecycle.ARCHIVED, archivedAt = archivedAt) else it
                }
        }

        override suspend fun reactivateChallenge(id: Long) {
            challenges.value =
                challenges.value.map {
                    if (it.id == id) it.copy(lifecycle = ChallengeLifecycle.ACTIVE, archivedAt = null) else it
                }
        }

        override suspend fun deleteChallenge(id: Long) {
            challenges.value = challenges.value.filterNot { it.id == id }
            entries.value = entries.value.filterNot { it.challengeId == id }
        }

        override suspend fun insertProgressEntry(entry: ChallengeProgressEntry): Long {
            val id = entry.id.takeIf { it != 0L } ?: ((entries.value.maxOfOrNull { it.id } ?: 0L) + 1L)
            entries.value = entries.value.filterNot { it.id == id } + entry.copy(id = id)
            return id
        }

        override suspend fun restoreProgressEntry(entry: ChallengeProgressEntry): Long = insertProgressEntry(entry)

        override suspend fun updateProgressEntry(entry: ChallengeProgressEntry) {
            entries.value = entries.value.map { if (it.id == entry.id) entry else it }
        }

        override suspend fun deleteProgressEntry(id: Long) {
            entries.value = entries.value.filterNot { it.id == id }
        }

        override suspend fun replaceChallenges(challenges: List<Challenge>) {
            this.challenges.value = challenges
        }

        override suspend fun replaceProgressEntries(entries: List<ChallengeProgressEntry>) {
            this.entries.value = entries
        }

        override suspend fun deleteAllChallenges() {
            challenges.value = emptyList()
        }

        override suspend fun deleteAllProgressEntries() {
            entries.value = emptyList()
        }
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 8, 3)
        val FIXED_CLOCK: Clock = Clock.fixed(Instant.parse("2026-08-03T12:00:00Z"), ZoneOffset.UTC)
    }
}
