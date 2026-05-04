package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class TrophiesContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyStateWhenNoFamiliesExist() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val emptyTitle = context.getString(R.string.trophies_empty_title)

        composeRule.setContent {
            TrophiesContent(
                state = TrophyPageState(),
                selectedFamilyName = null,
                onFamilySelected = {},
                onBackFromFamily = {},
                onTrophySelected = {},
                overviewFirstVisibleItemIndex = 0,
                overviewFirstVisibleItemScrollOffset = 0,
                onOverviewScrollChanged = { _, _ -> },
                familyFirstVisibleItemIndex = mutableMapOf(),
                familyFirstVisibleItemScrollOffset = mutableMapOf(),
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TROPHIES_EMPTY_STATE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun headerShowsActivitiesActionInOverviewMode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activities = context.getString(R.string.trophies_activities_action)
        var clicked = false

        composeRule.setContent {
            TrophiesHeader(
                familySection = null,
                onBack = {},
                onOpenActivities = { clicked = true },
            )
        }

        composeRule.onNodeWithText(activities).assertIsDisplayed()
        composeRule.onNodeWithText(activities).performClick()
        composeRule.runOnIdle { assert(clicked) }
    }

    @Test
    fun headerShowsActivitiesActionInFamilyDetailMode() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activities = context.getString(R.string.trophies_activities_action)
        var clicked = false

        composeRule.setContent {
            TrophiesHeader(
                familySection =
                    TrophyFamilySectionUi(
                        family = TrophyFamilyUi.RACE_EVENTS,
                        unlockedCount = 1,
                        totalCount = 6,
                    ),
                onBack = {},
                onOpenActivities = { clicked = true },
            )
        }

        composeRule.onNodeWithText(activities).assertIsDisplayed()
        composeRule.onNodeWithText(activities).performClick()
        composeRule.runOnIdle { assert(clicked) }
    }

    @Test
    fun clickingViewAllShowsFamilyDetailContent() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fullTime = context.getString(R.string.trophies_name_full_time)

        setContentWithFamilySelection(
            state =
                TrophyPageState(
                    families =
                        listOf(
                            TrophyFamilySectionUi(
                                family = TrophyFamilyUi.FOLLOW_THROUGH,
                                unlockedCount = 1,
                                totalCount = 1,
                                sections =
                                    listOf(
                                        TrophySectionUi(
                                            stableId = "follow_through",
                                            trophies = listOf(sampleCard()),
                                        ),
                                    ),
                            ),
                        ),
                ),
        )

        composeRule.onNodeWithTag(viewAllTag(TrophyFamilyUi.FOLLOW_THROUGH)).performClick()

        composeRule.onNodeWithTag(familyDetailTag(TrophyFamilyUi.FOLLOW_THROUGH)).assertIsDisplayed()
        composeRule.onNodeWithText(fullTime).assertIsDisplayed()
    }

    @Test
    fun overviewScrollPositionIsPreservedWhenReturningFromFamilyDetail() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val categories = context.getString(R.string.trophies_family_categories)
        val state =
            TrophyPageState(
                families =
                    listOf(
                        familySection(TrophyFamilyUi.FOLLOW_THROUGH, "Follow Through"),
                        familySection(TrophyFamilyUi.CONSISTENCY, "Consistency"),
                        familySection(TrophyFamilyUi.ADAPTABILITY, "Adaptability"),
                        familySection(TrophyFamilyUi.MOMENTUM, "Momentum"),
                        familySection(TrophyFamilyUi.BUILDER, "Builder"),
                        familySection(TrophyFamilyUi.CATEGORIES, "Categories"),
                    ),
            )

        var selectedFamilyName by mutableStateOf<String?>(null)
        var overviewFirstVisibleItemIndex by mutableStateOf(0)
        var overviewFirstVisibleItemScrollOffset by mutableStateOf(0)
        val familyFirstVisibleItemIndex = mutableStateMapOf<String, Int>()
        val familyFirstVisibleItemScrollOffset = mutableStateMapOf<String, Int>()

        composeRule.setContent {
            TrophiesContent(
                state = state,
                selectedFamilyName = selectedFamilyName,
                onFamilySelected = { family -> selectedFamilyName = family.name },
                onBackFromFamily = { selectedFamilyName = null },
                onTrophySelected = {},
                overviewFirstVisibleItemIndex = overviewFirstVisibleItemIndex,
                overviewFirstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
                onOverviewScrollChanged = { index, offset ->
                    overviewFirstVisibleItemIndex = index
                    overviewFirstVisibleItemScrollOffset = offset
                },
                familyFirstVisibleItemIndex = familyFirstVisibleItemIndex,
                familyFirstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset,
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TROPHIES_OVERVIEW_LIST_TAG).performScrollToIndex(5)
        composeRule.onNodeWithText(categories).assertIsDisplayed()

        composeRule.onNodeWithTag(viewAllTag(TrophyFamilyUi.CATEGORIES)).performClick()
        composeRule.onNodeWithTag(familyDetailTag(TrophyFamilyUi.CATEGORIES)).assertIsDisplayed()

        composeRule.runOnIdle { selectedFamilyName = null }

        composeRule.onNodeWithText(categories).assertIsDisplayed()
    }

    @Test
    fun familyDetailScrollPositionIsPreservedWhenReopened() {
        val state =
            TrophyPageState(
                families =
                    listOf(
                        TrophyFamilySectionUi(
                            family = TrophyFamilyUi.CATEGORIES,
                            unlockedCount = 0,
                            totalCount = 5,
                            sections =
                                listOf(
                                    categorySection("Run"),
                                    categorySection("Cycling"),
                                    categorySection("Strength"),
                                    categorySection("Swim"),
                                    categorySection("Mobility"),
                                ),
                        ),
                    ),
            )

        var selectedFamilyName by mutableStateOf<String?>(TrophyFamilyUi.CATEGORIES.name)
        var overviewFirstVisibleItemIndex by mutableStateOf(0)
        var overviewFirstVisibleItemScrollOffset by mutableStateOf(0)
        val familyFirstVisibleItemIndex = mutableStateMapOf<String, Int>()
        val familyFirstVisibleItemScrollOffset = mutableStateMapOf<String, Int>()

        composeRule.setContent {
            TrophiesContent(
                state = state,
                selectedFamilyName = selectedFamilyName,
                onFamilySelected = { family -> selectedFamilyName = family.name },
                onBackFromFamily = { selectedFamilyName = null },
                onTrophySelected = {},
                overviewFirstVisibleItemIndex = overviewFirstVisibleItemIndex,
                overviewFirstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
                onOverviewScrollChanged = { index, offset ->
                    overviewFirstVisibleItemIndex = index
                    overviewFirstVisibleItemScrollOffset = offset
                },
                familyFirstVisibleItemIndex = familyFirstVisibleItemIndex,
                familyFirstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset,
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(familyListTag(TrophyFamilyUi.CATEGORIES)).performScrollToIndex(4)
        composeRule.onNodeWithText("Mobility").assertIsDisplayed()

        composeRule.runOnIdle { selectedFamilyName = null }
        composeRule.runOnIdle { selectedFamilyName = TrophyFamilyUi.CATEGORIES.name }

        composeRule.onNodeWithText("Mobility").assertIsDisplayed()
    }

    @Test
    fun categoriesFamilyShowsCategorySectionTitlesInDetail() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val podiumPlace = context.getString(R.string.trophies_name_podium_place)

        setContentWithFamilySelection(
            state =
                TrophyPageState(
                    families =
                        listOf(
                            TrophyFamilySectionUi(
                                family = TrophyFamilyUi.CATEGORIES,
                                unlockedCount = 0,
                                totalCount = 1,
                                sections =
                                    listOf(
                                        TrophySectionUi(
                                            stableId = "run",
                                            title = "Run",
                                            trophies =
                                                listOf(
                                                    sampleCard(
                                                        stableId = "podium_place_1",
                                                        trophyId = TrophyId.PODIUM_PLACE,
                                                        family = TrophyFamilyUi.CATEGORIES,
                                                        categoryName = "Run",
                                                    ),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                ),
        )

        composeRule.onNodeWithTag(viewAllTag(TrophyFamilyUi.CATEGORIES)).performClick()

        composeRule.onNodeWithText("Run").assertIsDisplayed()
        composeRule.onNodeWithText(podiumPlace).assertIsDisplayed()
    }

    @Test
    fun trophyCardShowsLockedProgressWithoutStatusLabel() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val progress = context.getString(R.string.trophies_unlock_target, 1, 5)

        setContentWithFamilySelection(
            state =
                TrophyPageState(
                    families =
                        listOf(
                            TrophyFamilySectionUi(
                                family = TrophyFamilyUi.FOLLOW_THROUGH,
                                unlockedCount = 0,
                                totalCount = 1,
                                sections =
                                    listOf(
                                        TrophySectionUi(
                                            stableId = "follow_through",
                                            trophies =
                                                listOf(
                                                    sampleCard(
                                                        currentValue = 1,
                                                        target = 5,
                                                        isUnlocked = false,
                                                    ),
                                                ),
                                        ),
                                    ),
                            ),
                        ),
                ),
            initialSelectedFamilyName = TrophyFamilyUi.FOLLOW_THROUGH.name,
        )

        composeRule.onNodeWithText(progress).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.trophies_status_locked)).assertCountEquals(0)
    }

    @Test
    fun overviewShowsOnlyPreviewItemsUntilViewAll() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val matchFitness = context.getString(R.string.trophies_name_match_fitness)

        setContentWithFamilySelection(
            state =
                TrophyPageState(
                    families =
                        listOf(
                            TrophyFamilySectionUi(
                                family = TrophyFamilyUi.FOLLOW_THROUGH,
                                unlockedCount = 0,
                                totalCount = 7,
                                sections =
                                    listOf(
                                        TrophySectionUi(
                                            stableId = "follow_through",
                                            trophies =
                                                (1..6).map { index ->
                                                    sampleCard(
                                                        stableId = "full_time_$index",
                                                        currentValue = index,
                                                    )
                                                } +
                                                    sampleCard(
                                                        stableId = "match_fitness_hidden",
                                                        trophyId = TrophyId.MATCH_FITNESS,
                                                        currentValue = 7,
                                                    ),
                                        ),
                                    ),
                            ),
                        ),
                ),
        )

        composeRule.onAllNodesWithText(matchFitness).assertCountEquals(0)
        composeRule.onNodeWithTag(viewAllTag(TrophyFamilyUi.FOLLOW_THROUGH)).performClick()
        composeRule.onNodeWithText(matchFitness).assertExists()
    }

    @Test
    fun detailDialogShowsConditionAndUnlockedDate() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val close = context.getString(R.string.trophies_detail_close)
        val requirement = context.getString(R.string.trophies_desc_complete_weeks_unlocked, 1)

        composeRule.setContent {
            TrophyDetailDialog(
                trophy =
                    sampleCard(
                        currentValue = 1,
                        target = 1,
                        isUnlocked = true,
                        unlockedAt = 1234L,
                    ),
                onShare = {},
                onDismiss = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TROPHIES_DETAIL_DIALOG_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(close).assertIsDisplayed()
        composeRule.onNodeWithText(requirement).assertIsDisplayed()
    }

    @Test
    fun detailDialogShowsShareButtonOnlyForUnlockedTrophies() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val share = context.getString(R.string.trophies_detail_share)
        var trophy by mutableStateOf(
            sampleCard(
                currentValue = 1,
                target = 1,
                isUnlocked = true,
                unlockedAt = 1234L,
            ),
        )

        composeRule.setContent {
            TrophyDetailDialog(
                trophy = trophy,
                onShare = {},
                onDismiss = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(share).assertIsDisplayed()

        composeRule.runOnIdle {
            trophy =
                sampleCard(
                    currentValue = 0,
                    target = 1,
                    isUnlocked = false,
                    unlockedAt = null,
                )
        }

        composeRule.onAllNodesWithText(share).assertCountEquals(0)
    }

    @Test
    fun trophySelectionFromOverviewInvokesCallback() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fullTime = context.getString(R.string.trophies_name_full_time)
        var selectedId: String? = null

        composeRule.setContent {
            TrophiesContent(
                state =
                    TrophyPageState(
                        families =
                            listOf(
                                TrophyFamilySectionUi(
                                    family = TrophyFamilyUi.FOLLOW_THROUGH,
                                    unlockedCount = 1,
                                    totalCount = 1,
                                    sections =
                                        listOf(
                                            TrophySectionUi(
                                                stableId = "follow_through",
                                                trophies = listOf(sampleCard()),
                                            ),
                                        ),
                                ),
                            ),
                    ),
                selectedFamilyName = null,
                onFamilySelected = {},
                onBackFromFamily = {},
                onTrophySelected = { selectedId = it.stableId },
                overviewFirstVisibleItemIndex = 0,
                overviewFirstVisibleItemScrollOffset = 0,
                onOverviewScrollChanged = { _, _ -> },
                familyFirstVisibleItemIndex = mutableMapOf(),
                familyFirstVisibleItemScrollOffset = mutableMapOf(),
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(fullTime).performClick()

        composeRule.runOnIdle {
            assertEquals("full_time", selectedId)
        }
    }

    private fun sampleCard(
        stableId: String = "full_time",
        trophyId: TrophyId = TrophyId.FULL_TIME,
        family: TrophyFamilyUi = TrophyFamilyUi.FOLLOW_THROUGH,
        categoryName: String? = null,
        currentValue: Int = 1,
        target: Int = 5,
        isUnlocked: Boolean = false,
        unlockedAt: Long? = 10L,
    ): TrophyCardUi {
        return TrophyCardUi(
            stableId = stableId,
            trophyId = trophyId,
            family = family,
            sortOrder = 10,
            badgeRank = 1,
            categoryName = categoryName,
            currentValue = currentValue,
            target = target,
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
        )
    }

    private fun viewAllTag(family: TrophyFamilyUi): String {
        return TROPHIES_VIEW_ALL_TAG_PREFIX + family.name.lowercase(Locale.ROOT)
    }

    private fun familyDetailTag(family: TrophyFamilyUi): String {
        return TROPHIES_FAMILY_DETAIL_TAG_PREFIX + family.name.lowercase(Locale.ROOT)
    }

    private fun familyListTag(family: TrophyFamilyUi): String {
        return TROPHIES_FAMILY_LIST_TAG_PREFIX + family.name.lowercase(Locale.ROOT)
    }

    private fun setContentWithFamilySelection(
        state: TrophyPageState,
        initialSelectedFamilyName: String? = null,
        onTrophySelected: (TrophyCardUi) -> Unit = {},
    ) {
        composeRule.setContent {
            var selectedFamilyName by remember { mutableStateOf(initialSelectedFamilyName) }
            var overviewFirstVisibleItemIndex by remember { mutableIntStateOf(0) }
            var overviewFirstVisibleItemScrollOffset by remember { mutableIntStateOf(0) }
            val familyFirstVisibleItemIndex = remember { mutableStateMapOf<String, Int>() }
            val familyFirstVisibleItemScrollOffset = remember { mutableStateMapOf<String, Int>() }

            TrophiesContent(
                state = state,
                selectedFamilyName = selectedFamilyName,
                onFamilySelected = { family -> selectedFamilyName = family.name },
                onBackFromFamily = { selectedFamilyName = null },
                onTrophySelected = onTrophySelected,
                overviewFirstVisibleItemIndex = overviewFirstVisibleItemIndex,
                overviewFirstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
                onOverviewScrollChanged = { index, offset ->
                    overviewFirstVisibleItemIndex = index
                    overviewFirstVisibleItemScrollOffset = offset
                },
                familyFirstVisibleItemIndex = familyFirstVisibleItemIndex,
                familyFirstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset,
            )
        }
        composeRule.waitForIdle()
    }

    private fun familySection(
        family: TrophyFamilyUi,
        title: String,
    ): TrophyFamilySectionUi {
        return TrophyFamilySectionUi(
            family = family,
            unlockedCount = 0,
            totalCount = 1,
            sections =
                listOf(
                    TrophySectionUi(
                        stableId = family.name.lowercase(Locale.ROOT),
                        title = title.takeIf { family == TrophyFamilyUi.CATEGORIES },
                        trophies =
                            listOf(
                                sampleCard(
                                    stableId = family.name.lowercase(Locale.ROOT),
                                    trophyId = TrophyId.FULL_TIME,
                                    family = family,
                                ),
                            ),
                    ),
                ),
        )
    }

    private fun categorySection(title: String): TrophySectionUi {
        return TrophySectionUi(
            stableId = title.lowercase(Locale.ROOT),
            title = title,
            trophies =
                listOf(
                    sampleCard(
                        stableId = title.lowercase(Locale.ROOT),
                        trophyId = TrophyId.PODIUM_PLACE,
                        family = TrophyFamilyUi.CATEGORIES,
                        categoryName = title,
                    ),
                ),
        )
    }
}
