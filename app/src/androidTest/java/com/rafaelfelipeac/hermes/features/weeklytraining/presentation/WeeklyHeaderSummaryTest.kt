package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WeeklyHeaderSummaryUi
import org.junit.Rule
import org.junit.Test

class WeeklyHeaderSummaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun hidesSecondaryLineWhenRestBusyAndSickAreZero() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val restLabel = context.getString(R.string.weekly_training_summary_item_rest, 1)
        val busyLabel = context.getString(R.string.weekly_training_summary_item_busy, 1)
        val sickLabel = context.getString(R.string.weekly_training_summary_item_sick, 1)

        composeRule.setContent {
            WeeklyHeaderSummary(
                summary =
                    WeeklyHeaderSummaryUi(
                        plannedWorkouts = 7,
                        completedWorkouts = 6,
                        plannedRestEvents = 0,
                        plannedBusyEvents = 0,
                        plannedSickEvents = 0,
                        progress = 6f / 7f,
                    ),
            )
        }

        composeRule.onNodeWithTag(WEEKLY_SUMMARY_BLOCK_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(WEEKLY_SUMMARY_PROGRESS_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithText(restLabel).assertCountEquals(0)
        composeRule.onAllNodesWithText(busyLabel).assertCountEquals(0)
        composeRule.onAllNodesWithText(sickLabel).assertCountEquals(0)
    }

    @Test
    fun showsOnlyNonZeroSecondaryItems() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val restLabel = context.getString(R.string.weekly_training_summary_item_rest, 2)
        val busyLabel = context.getString(R.string.weekly_training_summary_item_busy, 1)
        val sickLabel = context.getString(R.string.weekly_training_summary_item_sick, 1)
        val separator = context.getString(R.string.weekly_training_summary_separator)
        val secondaryLine = listOf(restLabel, sickLabel).joinToString(separator = separator)

        composeRule.setContent {
            WeeklyHeaderSummary(
                summary =
                    WeeklyHeaderSummaryUi(
                        plannedWorkouts = 7,
                        completedWorkouts = 6,
                        plannedRestEvents = 2,
                        plannedBusyEvents = 0,
                        plannedSickEvents = 1,
                        progress = 6f / 7f,
                    ),
            )
        }

        composeRule.onNodeWithText(secondaryLine).assertIsDisplayed()
        composeRule.onAllNodesWithText(busyLabel).assertCountEquals(0)
    }
}
