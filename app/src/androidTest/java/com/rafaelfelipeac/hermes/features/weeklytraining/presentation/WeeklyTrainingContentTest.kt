package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.SECTION_HEADER_TAG_PREFIX
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WEEKLY_TRAINING_CONTENT_TAG
import com.rafaelfelipeac.hermes.core.ui.components.calendar.weeklytraining.WeeklyTrainingContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class WeeklyTrainingContentTest {
    @get:Rule
    val composeRule = createComposeRule()

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
                modifier = Modifier.height(200.dp),
            )
        }

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

        composeRule.onNodeWithTag(WEEKLY_TRAINING_CONTENT_TAG).performTouchInput {
            swipeLeft()
        }

        composeRule.runOnIdle {
            assertEquals(selectedDate.plusWeeks(1), changed)
        }
    }
}
