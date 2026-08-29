package com.rafaelfelipeac.hermes.features.challenges.presentation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

class ChallengesScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun listShellShowsHermesBackButtonFabAndCenteredEmptyStates() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var backCalls = 0
        val viewModel = createViewModel()

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = { backCalls++ },
                    viewModel = viewModel,
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.trophies_back))
            .assertIsDisplayed()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.challenges_create))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.challenges_active_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(context.getString(R.string.challenges_empty_active_title))
            .assertIsDisplayed()
        assertEmptyStateCentered(CHALLENGES_TAG_ACTIVE_EMPTY_STATE)

        composeRule.onNodeWithText(context.getString(R.string.challenges_archived_title)).performClick()
        composeRule
            .onNodeWithText(context.getString(R.string.challenges_empty_archived_title))
            .assertIsDisplayed()
        assertEmptyStateCentered(CHALLENGES_TAG_ARCHIVED_EMPTY_STATE)

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.trophies_back))
            .performClick()

        composeRule.runOnIdle {
            assertEquals(1, backCalls)
        }
    }

    @Test
    fun activeCardsShowProgressBarsAndArchivedCardsDoNot() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 10L,
                title = "August distance",
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val archivedChallenge =
            sampleChallenge(
                id = 11L,
                title = "Archived climb",
                lifecycle = ChallengeLifecycle.ARCHIVED,
                archivedAt = Instant.parse("2026-08-03T10:00:00Z"),
            )
        val progress =
            sampleProgressEntry(
                id = 100L,
                challengeId = challenge.id,
                quantity = 4_000L,
                entryDate = LocalDate.of(2026, 8, 16),
            )
        val viewModel = createViewModel(listOf(challenge, archivedChallenge), listOf(progress))

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS), useUnmergedTree = true).assertCountEquals(1)

        composeRule.onNodeWithText(context.getString(R.string.challenges_archived_title)).performClick()
        composeRule.onNodeWithText(archivedChallenge.title).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun detailScreenKeepsToolbarActionsQuickAddChipsAndInlineHistory() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 20L,
                title = "August target",
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val progressEntries =
            listOf(
                sampleProgressEntry(
                    id = 200L,
                    challengeId = challenge.id,
                    quantity = 34L,
                    entryDate = LocalDate.of(2026, 8, 28),
                ),
                sampleProgressEntry(
                    id = 201L,
                    challengeId = challenge.id,
                    quantity = 23L,
                    entryDate = LocalDate.of(2026, 8, 28),
                ),
                sampleProgressEntry(
                    id = 202L,
                    challengeId = challenge.id,
                    quantity = 12L,
                    entryDate = LocalDate.of(2026, 8, 27),
                ),
                sampleProgressEntry(
                    id = 203L,
                    challengeId = challenge.id,
                    quantity = 6L,
                    entryDate = LocalDate.of(2026, 8, 27),
                ),
            )
        val viewModel = createViewModel(listOf(challenge), progressEntries)

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule.onNodeWithTag(CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ADD_PROGRESS_BUTTON), useUnmergedTree = true).assertCountEquals(0)
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_with_quantity,
                    25,
                    "3",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_with_quantity,
                    100,
                    "10",
                ),
            )
            .assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.challenges_actions_menu))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_edit)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_archive)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete)).assertIsDisplayed()

        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 28))).assertIsDisplayed()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 27))).assertIsDisplayed()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 27))).performScrollTo()
        composeRule.onNodeWithText("34").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()
        composeRule.onNodeWithText("6").assertIsDisplayed()
    }

    @Test
    fun archivedDetailToolbarShowsReactivateAndDelete() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 20L,
                title = "August target",
                lifecycle = ChallengeLifecycle.ARCHIVED,
                archivedAt = Instant.parse("2026-08-03T10:00:00Z"),
            )
        val viewModel = createViewModel(listOf(challenge))

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.challenges_archived_title)).performClick()
        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.challenges_actions_menu))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_reactivate)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete)).assertIsDisplayed()
    }

    @Test
    fun openingEditorAndBackingOutReturnsToDetail() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 30L,
                title = "August target",
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val viewModel = createViewModel(listOf(challenge))

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.challenges_actions_menu))
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_edit)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_editor_title)).assertIsDisplayed()

        composeRule.activityRule.scenario.onActivity {
            it.onBackPressedDispatcher.onBackPressed()
        }

        composeRule.onNodeWithTag(CHALLENGES_TAG_DETAIL_ADD_PROGRESS_FAB).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ADD_PROGRESS_BUTTON), useUnmergedTree = true).assertCountEquals(0)
    }

    private fun createViewModel(
        challenges: List<Challenge> = emptyList(),
        progressEntries: List<ChallengeProgressEntry> = emptyList(),
    ): ChallengesViewModel {
        val repository = FakeChallengeRepository(challenges, progressEntries)
        val categoryRepository = FakeCategoryRepository()
        return ChallengesViewModel(
            repository = repository,
            categoryRepository = categoryRepository,
            userActionLogger = NoOpUserActionLogger,
            stringProvider = AndroidStringProviderAdapter(ApplicationProvider.getApplicationContext()),
            clock = Clock.fixed(Instant.parse("2026-08-28T12:00:00Z"), ZoneOffset.UTC),
        )
    }

    private fun sampleChallenge(
        id: Long = 1L,
        title: String = "Challenge",
        lifecycle: ChallengeLifecycle = ChallengeLifecycle.ACTIVE,
        archivedAt: Instant? = null,
    ): Challenge {
        return Challenge(
            id = id,
            title = title,
            description = "Description",
            targetType = ChallengeTargetType.DAILY,
            targetQuantity = 10L,
            startDate = LocalDate.of(2026, 8, 1),
            endDate = LocalDate.of(2026, 8, 31),
            lifecycle = lifecycle,
            archivedAt = archivedAt,
            createdAt = Instant.parse("2026-08-01T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T12:00:00Z"),
        )
    }

    private fun sampleProgressEntry(
        id: Long = 1L,
        challengeId: Long = 1L,
        quantity: Long = 2_000L,
        entryDate: LocalDate = LocalDate.of(2026, 8, 2),
    ): ChallengeProgressEntry {
        return ChallengeProgressEntry(
            id = id,
            challengeId = challengeId,
            quantity = quantity,
            entryDate = entryDate,
            occurredAt = Instant.parse("2026-08-02T09:00:00Z"),
            createdAt = Instant.parse("2026-08-02T09:00:00Z"),
            updatedAt = Instant.parse("2026-08-02T09:00:00Z"),
        )
    }

    private fun historyGroupTag(date: LocalDate): String = "$CHALLENGES_TAG_DETAIL_HISTORY_GROUP_PREFIX$date"

    private fun assertEmptyStateCentered(emptyStateTag: String) {
        val rootBounds = composeRule.onNodeWithTag(CHALLENGES_TAG_ROOT).fetchSemanticsNode().boundsInRoot
        val emptyStateBounds = composeRule.onNodeWithTag(emptyStateTag).fetchSemanticsNode().boundsInRoot
        val rootCenterY = (rootBounds.top + rootBounds.bottom) / 2f
        val emptyStateCenterY = (emptyStateBounds.top + emptyStateBounds.bottom) / 2f
        val allowableDrift = (rootBounds.bottom - rootBounds.top) * 0.15f

        assertTrue(abs(emptyStateCenterY - rootCenterY) <= allowableDrift)
    }

    private object NoOpUserActionLogger : UserActionLogger {
        override suspend fun log(action: UserAction) = Unit
    }

    private class AndroidStringProviderAdapter(
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
        ): String = context.getString(id, *args)
    }

    private class FakeChallengeRepository(
        initialChallenges: List<Challenge>,
        initialProgressEntries: List<ChallengeProgressEntry>,
    ) : ChallengeRepository {
        private val challenges = MutableStateFlow(initialChallenges)
        private val progressEntries = MutableStateFlow(initialProgressEntries)

        override fun observeActiveChallenges(): Flow<List<Challenge>> =
            challenges.map { items -> items.filter { it.lifecycle == ChallengeLifecycle.ACTIVE } }

        override fun observeArchivedChallenges(): Flow<List<Challenge>> =
            challenges.map { items -> items.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED } }

        override fun observeChallenge(id: Long): Flow<Challenge?> =
            challenges.map { items ->
                items.firstOrNull { it.id == id }
            }

        override fun observeProgressEntries(challengeId: Long): Flow<List<ChallengeProgressEntry>> =
            progressEntries.map { items -> items.filter { it.challengeId == challengeId } }

        override fun observeAllProgressEntries(): Flow<List<ChallengeProgressEntry>> = progressEntries

        override suspend fun getActiveChallenges(): List<Challenge> {
            return challenges.value.filter { it.lifecycle == ChallengeLifecycle.ACTIVE }
        }

        override suspend fun getArchivedChallenges(): List<Challenge> {
            return challenges.value.filter { it.lifecycle == ChallengeLifecycle.ARCHIVED }
        }

        override suspend fun getChallenge(id: Long): Challenge? = challenges.value.firstOrNull { it.id == id }

        override suspend fun getChallengeDateBounds(id: Long) = null

        override suspend fun getProgressEntries(challengeId: Long): List<ChallengeProgressEntry> =
            progressEntries.value.filter { it.challengeId == challengeId }

        override suspend fun getAllChallenges(): List<Challenge> = challenges.value

        override suspend fun getAllProgressEntries(): List<ChallengeProgressEntry> = progressEntries.value

        override suspend fun insertChallenge(challenge: Challenge): Long {
            challenges.value = challenges.value.filterNot { it.id == challenge.id } + challenge
            return challenge.id
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
            progressEntries.value = progressEntries.value.filterNot { it.challengeId == id }
        }

        override suspend fun insertProgressEntry(entry: ChallengeProgressEntry): Long {
            progressEntries.value = progressEntries.value.filterNot { it.id == entry.id } + entry
            return entry.id
        }

        override suspend fun restoreProgressEntry(entry: ChallengeProgressEntry): Long = insertProgressEntry(entry)

        override suspend fun updateProgressEntry(entry: ChallengeProgressEntry) {
            progressEntries.value = progressEntries.value.map { if (it.id == entry.id) entry else it }
        }

        override suspend fun deleteProgressEntry(id: Long) {
            progressEntries.value = progressEntries.value.filterNot { it.id == id }
        }

        override suspend fun replaceChallenges(challenges: List<Challenge>) {
            this.challenges.value = challenges
        }

        override suspend fun replaceProgressEntries(entries: List<ChallengeProgressEntry>) {
            progressEntries.value = entries
        }

        override suspend fun deleteAllChallenges() {
            challenges.value = emptyList()
        }

        override suspend fun deleteAllProgressEntries() {
            progressEntries.value = emptyList()
        }
    }

    private class FakeCategoryRepository : CategoryRepository {
        private val categories = MutableStateFlow(emptyList<Category>())

        override fun observeCategories(): Flow<List<Category>> = categories

        override suspend fun getCategories(): List<Category> = categories.value

        override suspend fun getCategory(id: Long): Category? = null

        override suspend fun getCount(): Int = 0

        override suspend fun insertCategory(category: Category): Long = 0L

        override suspend fun insertCategories(categories: List<Category>): List<Long> = emptyList()

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
}
