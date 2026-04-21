package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SECTION_HEADER_TAG_PREFIX
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WEEKLY_TRAINING_CONTENT_TAG
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WeeklyTrainingContent
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.WeeklyTrainingTestViewportHeight
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeeklyTrainingContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun changingSelectedDateScrollsToSection() {
        val weekStart = LocalDate.of(2026, 1, 12)
        val selectedDateState = mutableStateOf(weekStart)

        composeRule.setContent {
            WeeklyTrainingContent(
                selectedDate = selectedDateState.value,
                workouts = emptyList(),
                onWorkoutMoved = { _, _, _, _ -> },
                onWorkoutCompletionChanged = { _, _ -> },
                onWorkoutEdit = {},
                onWorkoutDelete = {},
                modifier = Modifier.height(WeeklyTrainingTestViewportHeight),
            )
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            selectedDateState.value = weekStart.plusDays(6)
        }

        composeRule.onNodeWithTag("$SECTION_HEADER_TAG_PREFIX${DayOfWeek.SUNDAY}").assertIsDisplayed()
    }

    @Test
    fun swipeLeftInvokesOnWeekChanged() {
        val selectedDate = LocalDate.of(2026, 1, 15)
        var changed: LocalDate? = null

        composeRule.setContent {
            WeeklyTrainingContent(
                selectedDate = selectedDate,
                workouts = emptyList(),
                onWorkoutMoved = { _, _, _, _ -> },
                onWorkoutCompletionChanged = { _, _ -> },
                onWorkoutEdit = {},
                onWorkoutDelete = {},
                onWeekChanged = { changed = it },
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(WEEKLY_TRAINING_CONTENT_TAG).performTouchInput {
            swipeLeft()
        }

        composeRule.runOnIdle {
            assertEquals(selectedDate.plusWeeks(1), changed)
        }
    }

    @Test
    fun focusedCategory_keepsWorkoutTextsVisible() {
        val weekStart = LocalDate.of(2026, 1, 12)

        composeRule.setContent {
            WeeklyTrainingContent(
                selectedDate = weekStart,
                workouts =
                    listOf(
                        WorkoutUi(
                            id = 1L,
                            weekStartDate = weekStart,
                            dayOfWeek = DayOfWeek.MONDAY,
                            type = "Run",
                            description = "",
                            isCompleted = false,
                            isRestDay = false,
                            categoryId = 10L,
                            categoryColorId = "run",
                            categoryName = "Run",
                            order = 0,
                            eventType = EventType.WORKOUT,
                        ),
                        WorkoutUi(
                            id = 2L,
                            weekStartDate = weekStart,
                            dayOfWeek = DayOfWeek.TUESDAY,
                            type = "Bike",
                            description = "",
                            isCompleted = false,
                            isRestDay = false,
                            categoryId = 20L,
                            categoryColorId = "bike",
                            categoryName = "Bike",
                            order = 0,
                            eventType = EventType.WORKOUT,
                        ),
                    ),
                focusedCategoryId = 10L,
                onWorkoutMoved = { _, _, _, _ -> },
                onWorkoutCompletionChanged = { _, _ -> },
                onWorkoutEdit = {},
                onWorkoutDelete = {},
            )
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Run").assertIsDisplayed()
        composeRule.onNodeWithText("Bike").assertIsDisplayed()
    }
}
