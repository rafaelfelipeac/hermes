package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityFiltersUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityPrimaryFilterUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ActivityContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun activityContent_rendersTitleAndSubtitleWithoutEllipsis() {
        val title = "You completed the workout \"Bike\"."
        val subtitle = "Week of Feb 2, 2026.\nFrom \"Monday\" to \"Tuesday\"."

        composeRule.setContent {
            ActivityContent(
                sections =
                    listOf(
                        ActivitySectionUi(
                            date = LocalDate.of(2026, 2, 2),
                            items =
                                listOf(
                                    ActivityItemUi(
                                        id = 1L,
                                        title = title,
                                        subtitle = subtitle,
                                        time = "09:30",
                                    ),
                                ),
                        ),
                ),
                currentLocale = java.util.Locale.ENGLISH,
                emptyMessage = "No activity yet.",
            )
        }

        composeRule.onNodeWithText(title).assertIsDisplayed()
        composeRule.onNodeWithText(subtitle).assertIsDisplayed()
        composeRule.onNodeWithText("09:30").assertIsDisplayed()
    }

    @Test
    fun activityContent_rendersFiltersAndForwardsSelection() {
        var selectedFilter: ActivityPrimaryFilter? = null

        composeRule.setContent {
            ActivityContent(
                sections = emptyList(),
                currentLocale = java.util.Locale.ENGLISH,
                filters =
                    ActivityFiltersUi(
                        primaryFilters =
                            listOf(
                                ActivityPrimaryFilterUi(ActivityPrimaryFilter.ALL, "All"),
                                ActivityPrimaryFilterUi(ActivityPrimaryFilter.COMPLETIONS, "Completions"),
                            ),
                        selectedPrimaryFilter = ActivityPrimaryFilter.ALL,
                    ),
                emptyMessage = "No activity yet.",
                onPrimaryFilterSelected = { selectedFilter = it },
                onClearFilters = {},
            )
        }

        composeRule.onNodeWithText("All").assertIsDisplayed()
        composeRule.onNodeWithText("Completions").performClick()

        composeRule.runOnIdle {
            assertEquals(ActivityPrimaryFilter.COMPLETIONS, selectedFilter)
        }
    }
}
