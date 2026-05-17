package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WeeklyHeaderSummaryUi
import org.junit.Rule
import org.junit.Test

private const val WEEKLY_SUMMARY_WORKOUTS_STAT_TAG = "weekly-summary-workouts-stat"
private const val WEEKLY_SUMMARY_EVENTS_STAT_TAG = "weekly-summary-events-stat"
private const val WEEKLY_SUMMARY_TOGGLE_TAG = "weekly-summary-toggle"

class WeeklyHeaderSummaryTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun startsCollapsed() {
        composeRule.setContent {
            WeeklyHeaderSummary(
                summary =
                    WeeklyHeaderSummaryUi(
                        plannedItems = 7,
                        completedItems = 6,
                        plannedWorkouts = 7,
                        completedWorkouts = 6,
                        plannedRaceEvents = 0,
                        completedRaceEvents = 0,
                        plannedRestEvents = 0,
                        plannedBusyEvents = 0,
                        plannedSickEvents = 0,
                        progress = 6f / 7f,
                    ),
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(WEEKLY_SUMMARY_BLOCK_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(WEEKLY_SUMMARY_PROGRESS_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(WEEKLY_SUMMARY_TOGGLE_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithTag(WEEKLY_SUMMARY_METRICS_ROW_TAG).assertCountEquals(0)
        composeRule.onAllNodesWithTag(WEEKLY_SUMMARY_WORKOUTS_STAT_TAG).assertCountEquals(0)
        composeRule.onAllNodesWithTag(WEEKLY_SUMMARY_EVENTS_STAT_TAG).assertCountEquals(0)
        composeRule.onAllNodesWithTag(WEEKLY_SUMMARY_SECONDARY_ROW_TAG).assertCountEquals(0)
    }

    @Test
    fun expandsToShowDetails() {
        composeRule.setContent {
            WeeklyHeaderSummary(
                summary =
                    WeeklyHeaderSummaryUi(
                        plannedItems = 8,
                        completedItems = 5,
                        plannedWorkouts = 7,
                        completedWorkouts = 6,
                        plannedRaceEvents = 1,
                        completedRaceEvents = 1,
                        plannedRestEvents = 2,
                        plannedBusyEvents = 0,
                        plannedSickEvents = 1,
                        progress = 6f / 7f,
                    ),
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(WEEKLY_SUMMARY_TOGGLE_TAG).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(WEEKLY_SUMMARY_SECONDARY_ROW_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(WEEKLY_SUMMARY_WORKOUTS_STAT_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(WEEKLY_SUMMARY_EVENTS_STAT_TAG).assertIsDisplayed()
    }
}
