package com.rafaelfelipeac.hermes.features.activity.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivitySectionUi
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ActivityContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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
            )
        }

        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithText(subtitle).assertExists()
        composeRule.onNodeWithText("09:30").assertExists()
    }
}
