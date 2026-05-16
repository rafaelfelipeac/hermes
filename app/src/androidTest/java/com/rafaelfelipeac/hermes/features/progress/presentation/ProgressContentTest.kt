package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyMode
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyFamilyUi
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class ProgressContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingSupportCardsOpensTheirDestinations() {
        var openedEvent = false
        var openedTrophy = false
        var openedActivity = false

        val state =
            ProgressState(
                emptyReason = null,
                sections =
                    listOf(
                        ProgressSectionUi.SupportingProgress(
                            nextFocus = null,
                            upcomingEvent =
                                ProgressUpcomingEventUi(
                                    id = 91L,
                                    title = "Long run",
                                    date = LocalDate.of(2026, 5, 15),
                                    daysUntil = 2,
                                ),
                            trophyHighlight =
                                FeaturedTrophyUi(
                                    trophy =
                                        TrophyCardUi(
                                            stableId = "race_ready",
                                            trophyId = TrophyId.RACE_READY,
                                            family = TrophyFamilyUi.RACE_EVENTS,
                                            sortOrder = 0,
                                            badgeRank = 0,
                                            currentValue = 2,
                                            target = 5,
                                            isUnlocked = false,
                                        ),
                                    mode = FeaturedTrophyMode.RECENT_UNLOCK,
                                ),
                        ),
                        ProgressSectionUi.RecentActivity(
                            items =
                                listOf(
                                    ActivityItemUi(
                                        id = 11L,
                                        title = "You completed an event.",
                                        subtitle = null,
                                        time = "21:53",
                                    ),
                                ),
                        ),
                    ),
            )

        composeRule.setContent {
            ProgressContent(
                state = state,
                locale = Locale.US,
                onOpenActivity = { openedActivity = true },
                onOpenActivityItem = { openedActivity = true },
                onOpenWorkout = {},
                onOpenEvent = { openedEvent = true },
                onOpenTrophy = { openedTrophy = true },
            )
        }

        composeRule.onNodeWithText("Long run").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Race Ready").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("You completed an event.").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertTrue(openedEvent)
            assertTrue(openedTrophy)
            assertTrue(openedActivity)
        }
    }

    @Test
    fun clickingWeeklyReadoutNextFocusOpensWorkout() {
        var openedWorkout = false

        val state =
            ProgressState(
                emptyReason = null,
                sections =
                    listOf(
                        ProgressSectionUi.WeeklyReadout(
                            readout =
                                ProgressWeeklyReadoutUi(
                                    plannedWorkouts = 2,
                                    completedWorkouts = 1,
                                    completionPercent = 50,
                                    nextFocus =
                                        ProgressNextFocusUi(
                                            id = 41L,
                                            title = "Easy run",
                                            date = LocalDate.of(2026, 5, 15),
                                            daysUntil = 2,
                                        ),
                                ),
                        ),
                    ),
            )

        composeRule.setContent {
            ProgressContent(
                state = state,
                locale = Locale.US,
                onOpenActivity = {},
                onOpenActivityItem = {},
                onOpenWorkout = { openedWorkout = true },
                onOpenEvent = {},
                onOpenTrophy = {},
            )
        }

        composeRule.onNodeWithText("Easy run").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertTrue(openedWorkout)
        }
    }
}
