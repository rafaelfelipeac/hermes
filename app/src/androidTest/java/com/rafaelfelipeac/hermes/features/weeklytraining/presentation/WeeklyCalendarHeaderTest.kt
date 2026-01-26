package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.rafaelfelipeac.hermes.core.ui.components.calendar.WeeklyCalendarHeader
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class WeeklyCalendarHeaderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingDayInvokesOnDateSelected() {
        val weekStart = LocalDate.of(2026, 1, 12)
        val selectedDate = LocalDate.of(2026, 1, 14)
        var selected: LocalDate? = null

        composeRule.setContent {
            WeeklyCalendarHeader(
                selectedDate = selectedDate,
                weekStartDate = weekStart,
                dayIndicators = emptyMap(),
                onDateSelected = { selected = it },
                onWeekChanged = {},
            )
        }

        composeRule.onNodeWithTag("header-day-$weekStart").performClick()

        composeRule.runOnIdle {
            assertEquals(weekStart, selected)
        }
    }

    @Test
    fun swipeLeftInvokesOnWeekChanged() {
        val weekStart = LocalDate.of(2026, 1, 12)
        val selectedDate = LocalDate.of(2026, 1, 15)
        var changed: LocalDate? = null

        composeRule.setContent {
            WeeklyCalendarHeader(
                selectedDate = selectedDate,
                weekStartDate = weekStart,
                dayIndicators = emptyMap(),
                onDateSelected = {},
                onWeekChanged = { changed = it },
            )
        }

        composeRule.onNodeWithTag("weekly-calendar-header").performTouchInput {
            swipeLeft()
        }

        composeRule.runOnIdle {
            assertEquals(selectedDate.plusWeeks(1), changed)
        }
    }
}
