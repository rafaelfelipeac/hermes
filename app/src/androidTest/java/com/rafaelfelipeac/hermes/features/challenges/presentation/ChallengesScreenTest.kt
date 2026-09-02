package com.rafaelfelipeac.hermes.features.challenges.presentation

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.CYCLING_ID
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.challenges.domain.model.Challenge
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeProgressEntry
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeQuantity
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.domain.repository.ChallengeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.text.NumberFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
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
        assertEmptyStateContentCentered(
            CHALLENGES_TAG_ACTIVE_EMPTY_STATE,
            context.getString(R.string.challenges_empty_active_title),
            context.getString(R.string.challenges_empty_active_body),
        )

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
                categoryId = CYCLING_ID,
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val archivedChallenge =
            sampleChallenge(
                id = 11L,
                title = "Archived climb",
                targetType = ChallengeTargetType.TOTAL,
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
        val category = sampleCategory()
        val viewModel = createViewModel(listOf(challenge, archivedChallenge), listOf(progress), listOf(category))

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenge_target_type_daily)).assertIsDisplayed()
        composeRule.onNodeWithText(category.name).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_status_exceeded)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_progress_value_with_percent,
                    context.getString(
                        R.string.challenges_progress_value,
                        ChallengeQuantity.format(progress.quantity, Locale.getDefault()),
                        ChallengeQuantity.format(310L, Locale.getDefault()),
                    ),
                    context.getString(R.string.challenges_progress_percent, formatPercent(1_290.3)),
                ),
            )
            .assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS), useUnmergedTree = true).assertCountEquals(1)

        composeRule.onNodeWithText(context.getString(R.string.challenges_archived_title)).performClick()
        composeRule.onNodeWithText(archivedChallenge.title).assertIsDisplayed()
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_ACTIVE_CARD_PROGRESS), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun detailScreenShowsSummaryTodayQuickAddAndInlineHistory() {
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
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_progress_value_with_percent,
                    context.getString(
                        R.string.challenges_progress_value,
                        ChallengeQuantity.format(75L, Locale.getDefault()),
                        ChallengeQuantity.format(310L, Locale.getDefault()),
                    ),
                    context.getString(R.string.challenges_progress_percent, formatPercent(24.2)),
                ),
            )
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_today_label)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_today_completed_label)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_today_remaining_label)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_required_pace_label)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_debt_label)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_history_day_completed,
                    ChallengeQuantity.format(57L, Locale.getDefault()),
                ),
            )
            .performScrollTo()
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_quick_add_title)).assertIsDisplayed()
        composeRule
            .onNodeWithTag(CHALLENGES_TAG_DETAIL_QUICK_ADD)
            .performScrollTo()
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "3",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "5",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "10",
                ),
            )
            .assertIsDisplayed()

        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "3",
                ),
            )
            .performClick()
        composeRule.waitForIdle()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "3",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "5",
                ),
            )
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(
                    R.string.challenges_quick_add_button,
                    "10",
                ),
            )
            .assertIsDisplayed()

        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 28))).performScrollTo()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 28))).assertIsDisplayed()
        composeRule
            .onAllNodesWithContentDescription(context.getString(R.string.challenges_progress_entry_actions_menu))[0]
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_edit)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_edit)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_edit_progress)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.add_workout_cancel)).performClick()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 27))).assertIsDisplayed()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 27))).performScrollTo()
        composeRule.onNodeWithText("34").assertIsDisplayed()
        composeRule.onNodeWithText("23").assertIsDisplayed()
        composeRule.onNodeWithText("12").assertIsDisplayed()
        composeRule.onNodeWithText("6").assertIsDisplayed()
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 28))).performScrollTo()
        composeRule
            .onAllNodesWithContentDescription(context.getString(R.string.challenges_progress_entry_actions_menu))[0]
            .performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete_progress_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_delete_confirm)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_undo_deleted_progress_entry)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.weekly_training_undo_action)).assertIsDisplayed()
    }

    @Test
    fun completedChallengeShowsHeroAndDoesNotReplayConfettiOnOpen() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 25L,
                title = "Finished target",
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 10L,
            )
        val viewModel =
            createViewModel(
                challenges = listOf(challenge),
                progressEntries = listOf(sampleProgressEntry(challengeId = challenge.id, quantity = 10L)),
            )

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(onBack = {}, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule.onNodeWithTag(CHALLENGES_TAG_COMPLETION_CELEBRATION).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_completion_title)).assertIsDisplayed()
        composeRule
            .onNodeWithText(
                context.getString(R.string.challenges_progress_percent, formatPercent(100.0)),
                substring = true,
            ).assertIsDisplayed()
        composeRule.onAllNodesWithTag(CHALLENGES_TAG_COMPLETION_CONFETTI).assertCountEquals(0)
    }

    @Test
    fun archivedDetailHidesQuickAddCard() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 21L,
                title = "Archived target",
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
        composeRule.onAllNodes(hasTestTag(CHALLENGES_TAG_DETAIL_QUICK_ADD), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun detailScreenUsesRecoveryAdjustedRemainingForDailyChallenges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 22L,
                title = "Daily recovery",
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val progress =
            listOf(
                sampleProgressEntry(
                    id = 220L,
                    challengeId = challenge.id,
                    quantity = 5L,
                    entryDate = LocalDate.of(2026, 8, 28),
                ),
            )
        val viewModel = createViewModel(listOf(challenge), progress)

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_today_remaining_label)).assertIsDisplayed()
        composeRule.onNodeWithText("72").assertIsDisplayed()
    }

    @Test
    fun detailScreenUsesRecoveryAdjustedRemainingForTotalChallenges() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 23L,
                title = "Total recovery",
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 100L,
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val progress =
            listOf(
                sampleProgressEntry(
                    id = 230L,
                    challengeId = challenge.id,
                    quantity = 5L,
                    entryDate = LocalDate.of(2026, 8, 28),
                ),
            )
        val viewModel = createViewModel(listOf(challenge), progress)

        composeRule.setContent {
            HermesTheme {
                ChallengesScreen(
                    onBack = {},
                    viewModel = viewModel,
                )
            }
        }

        composeRule.onNodeWithText(challenge.title).performClick()
        composeRule.onNodeWithText(context.getString(R.string.challenges_today_remaining_label)).assertIsDisplayed()
        composeRule.onNodeWithText("19").assertIsDisplayed()
    }

    @Test
    fun totalChallengeQuickAddUsesInitialAverageAndStaysStableAfterProgress() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 25L,
                title = "Total quick add",
                targetType = ChallengeTargetType.TOTAL,
                targetQuantity = 120L,
                lifecycle = ChallengeLifecycle.ACTIVE,
            )
        val progress =
            listOf(
                sampleProgressEntry(
                    id = 250L,
                    challengeId = challenge.id,
                    quantity = 90L,
                    entryDate = LocalDate.of(2026, 8, 27),
                ),
            )
        val viewModel = createViewModel(listOf(challenge), progress)

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
            .onNodeWithText(context.getString(R.string.challenges_quick_add_button, "1"))
            .assertIsDisplayed()
            .performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(context.getString(R.string.challenges_quick_add_button, "1")).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_quick_add_button, "2")).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.challenges_quick_add_button, "4")).assertIsDisplayed()
    }

    @Test
    fun archivedDetailHidesProgressEntryActions() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val challenge =
            sampleChallenge(
                id = 24L,
                title = "Archived progress",
                lifecycle = ChallengeLifecycle.ARCHIVED,
                archivedAt = Instant.parse("2026-08-03T10:00:00Z"),
            )
        val progress =
            listOf(
                sampleProgressEntry(
                    id = 240L,
                    challengeId = challenge.id,
                    quantity = 5L,
                    entryDate = LocalDate.of(2026, 8, 28),
                ),
            )
        val viewModel = createViewModel(listOf(challenge), progress)

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
        composeRule.onNodeWithTag(historyGroupTag(LocalDate.of(2026, 8, 28))).performScrollTo()
        composeRule
            .onAllNodesWithContentDescription(context.getString(R.string.challenges_progress_entry_actions_menu))
            .assertCountEquals(0)
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
    }

    private fun createViewModel(
        challenges: List<Challenge> = emptyList(),
        progressEntries: List<ChallengeProgressEntry> = emptyList(),
        categories: List<Category> = emptyList(),
    ): ChallengesViewModel {
        val repository = FakeChallengeRepository(challenges, progressEntries)
        val categoryRepository = FakeCategoryRepository(categories)
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
        categoryId: Long? = null,
        targetType: ChallengeTargetType = ChallengeTargetType.DAILY,
        targetQuantity: Long = 10L,
        lifecycle: ChallengeLifecycle = ChallengeLifecycle.ACTIVE,
        archivedAt: Instant? = null,
    ): Challenge {
        return Challenge(
            id = id,
            categoryId = categoryId,
            title = title,
            description = "Description",
            targetType = targetType,
            targetQuantity = targetQuantity,
            startDate = LocalDate.of(2026, 8, 1),
            endDate = LocalDate.of(2026, 8, 31),
            lifecycle = lifecycle,
            archivedAt = archivedAt,
            createdAt = Instant.parse("2026-08-01T12:00:00Z"),
            updatedAt = Instant.parse("2026-08-01T12:00:00Z"),
        )
    }

    private fun sampleCategory(): Category {
        return Category(
            id = CYCLING_ID,
            name = "Cycling",
            colorId = COLOR_CYCLING,
            sortOrder = 0,
            isHidden = false,
            isSystem = false,
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

    private fun formatPercent(value: Double): String =
        NumberFormat.getNumberInstance(Locale.getDefault()).apply {
            minimumFractionDigits = 1
            maximumFractionDigits = 1
        }.format(value)

    private fun assertEmptyStateCentered(emptyStateTag: String) {
        val rootBounds = composeRule.onNodeWithTag(CHALLENGES_TAG_ROOT).fetchSemanticsNode().boundsInRoot
        val emptyStateBounds = composeRule.onNodeWithTag(emptyStateTag).fetchSemanticsNode().boundsInRoot
        val rootCenterY = (rootBounds.top + rootBounds.bottom) / 2f
        val emptyStateCenterY = (emptyStateBounds.top + emptyStateBounds.bottom) / 2f
        val allowableDrift = (rootBounds.bottom - rootBounds.top) * 0.10f

        assertTrue(abs(emptyStateCenterY - rootCenterY) <= allowableDrift)
    }

    private fun assertEmptyStateContentCentered(
        emptyStateTag: String,
        title: String,
        body: String,
    ) {
        val cardBounds = composeRule.onNodeWithTag(emptyStateTag).fetchSemanticsNode().boundsInRoot
        val cardCenterX = (cardBounds.left + cardBounds.right) / 2f
        val allowableDrift = (cardBounds.right - cardBounds.left) * 0.1f
        val titleBounds = composeRule.onNodeWithText(title).fetchSemanticsNode().boundsInRoot
        val bodyBounds = composeRule.onNodeWithText(body).fetchSemanticsNode().boundsInRoot
        assertTrue(abs(((titleBounds.left + titleBounds.right) / 2f) - cardCenterX) <= allowableDrift)
        assertTrue(abs(((bodyBounds.left + bodyBounds.right) / 2f) - cardCenterX) <= allowableDrift)
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

    private class FakeCategoryRepository(
        initialCategories: List<Category>,
    ) : CategoryRepository {
        private val categories = MutableStateFlow(initialCategories)

        override fun observeCategories(): Flow<List<Category>> = categories

        override suspend fun getCategories(): List<Category> = categories.value

        override suspend fun getCategory(id: Long): Category? = categories.value.firstOrNull { it.id == id }

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
