package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.runtime.Composable
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class DatePickerWeekStartTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun workoutDialog_honorsConfiguredWeekStart() {
        assertWeekStartIsVisible(
            weekStartDay = WeekStartDay.WEDNESDAY,
            content = {
                AddWorkoutDialog(
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onManageCategories = { _, _, _, _ -> },
                    isEdit = false,
                    categories = emptyList(),
                    selectedCategoryId = null,
                    weekStartDay = WeekStartDay.WEDNESDAY,
                    selectedDate = LocalDate.of(2026, 5, 24),
                )
            },
        )
    }

    @Test
    fun raceEventDialog_honorsConfiguredWeekStart() {
        assertWeekStartIsVisible(
            weekStartDay = WeekStartDay.WEDNESDAY,
            content = {
                AddRaceEventDialog(
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                    onManageCategories = { _, _, _, _ -> },
                    isEdit = false,
                    categories =
                        listOf(
                            CategoryUi(
                                id = 1L,
                                name = "Uncategorized",
                                colorId = "uncategorized",
                                sortOrder = 0,
                                isHidden = false,
                                isSystem = true,
                            ),
                        ),
                    selectedCategoryId = 1L,
                    weekStartDay = WeekStartDay.WEDNESDAY,
                    selectedDate = LocalDate.of(2026, 5, 24),
                )
            },
        )
    }

    private fun assertWeekStartIsVisible(
        weekStartDay: WeekStartDay,
        content: @Composable () -> Unit,
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val dateLabel = context.getString(R.string.race_event_dialog_date)

        composeRule.setContent(content)

        composeRule.onNodeWithText(dateLabel).performClick()

        composeRule.onNodeWithText("W").assertIsDisplayed()
        composeRule.onNodeWithText("M").assertIsDisplayed()

        val wLeft =
            composeRule.onNodeWithText("W", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
                .left
        val mLeft =
            composeRule.onNodeWithText("M", useUnmergedTree = true)
                .fetchSemanticsNode()
                .boundsInRoot
                .left

        assertTrue(
            "Expected ${weekStartDay.name} to appear before Monday in the date picker grid",
            wLeft < mLeft,
        )
    }
}
