package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class TrophiesContentTest {
    @get:Rule
    val composeRule = createComposeRule()

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
            )
        }

        composeRule.onNodeWithTag(TROPHIES_EMPTY_STATE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(emptyTitle).assertIsDisplayed()
    }

    @Test
    fun clickingViewAllShowsFamilyDetailContent() {
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
        composeRule.onNodeWithText("Full-Time").assertIsDisplayed()
    }

    @Test
    fun categoriesFamilyShowsCategorySectionTitlesInDetail() {
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
        composeRule.onNodeWithText("Podium Place").assertIsDisplayed()
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
                                                (1..7).map { index ->
                                                    sampleCard(
                                                        stableId = "full_time_$index",
                                                        currentValue = index,
                                                    )
                                                },
                                        ),
                                    ),
                            ),
                        ),
                ),
        )

        composeRule.onAllNodesWithText("Full-Time").assertCountEquals(6)
        composeRule.onNodeWithTag(viewAllTag(TrophyFamilyUi.FOLLOW_THROUGH)).performClick()
        composeRule.onAllNodesWithText("Full-Time").assertCountEquals(7)
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
                onDismiss = {},
            )
        }

        composeRule.onNodeWithTag(TROPHIES_DETAIL_DIALOG_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(close).assertIsDisplayed()
        composeRule.onNodeWithText(requirement).assertIsDisplayed()
    }

    @Test
    fun trophySelectionFromOverviewInvokesCallback() {
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
            )
        }

        composeRule.onNodeWithText("Full-Time").performClick()

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

    private fun setContentWithFamilySelection(
        state: TrophyPageState,
        initialSelectedFamilyName: String? = null,
        onTrophySelected: (TrophyCardUi) -> Unit = {},
    ) {
        composeRule.setContent {
            var selectedFamilyName by remember { mutableStateOf(initialSelectedFamilyName) }

            TrophiesContent(
                state = state,
                selectedFamilyName = selectedFamilyName,
                onFamilySelected = { family -> selectedFamilyName = family.name },
                onBackFromFamily = { selectedFamilyName = null },
                onTrophySelected = onTrophySelected,
            )
        }
    }
}
