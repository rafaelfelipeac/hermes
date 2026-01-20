package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyCalendarHeader(
    selectedDate: LocalDate,
    weekStartDate: LocalDate,
    dayIndicators: Map<DayOfWeek, DayIndicator>,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dayContent: @Composable (
        date: LocalDate,
        isSelected: Boolean,
        hasWorkout: Boolean,
    ) -> Unit = { date, isSelected, hasWorkout ->
        DefaultDayContent(date = date, isSelected = isSelected, hasWorkout = hasWorkout)
    },
) {
    val weekEndDate = weekStartDate.plusDays(6)
    val swipeThreshold = with(LocalDensity.current) { 72.dp.toPx() }
    var dragAmount by remember { mutableStateOf(0f) }

    Column(
        modifier =
            modifier
                .testTag("weekly-calendar-header")
                .pointerInput(selectedDate, onWeekChanged) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount ->
                            dragAmount += amount
                        },
                        onDragEnd = {
                            when {
                                dragAmount <= -swipeThreshold -> onWeekChanged(selectedDate.plusWeeks(1))
                                dragAmount >= swipeThreshold -> onWeekChanged(selectedDate.minusWeeks(1))
                            }
                            dragAmount = 0f
                        },
                        onDragCancel = { dragAmount = 0f },
                    )
                },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { onWeekChanged(selectedDate.minusWeeks(1)) },
                modifier = Modifier.testTag("week-prev"),
            ) {
                Text(text = stringResource(R.string.week_previous))
            }

            Text(text = formatWeekRange(weekStartDate, weekEndDate))

            IconButton(
                onClick = { onWeekChanged(selectedDate.plusWeeks(1)) },
                modifier = Modifier.testTag("week-next"),
            ) {
                Text(text = stringResource(R.string.week_next))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            for (offset in 0..6) {
                val date = weekStartDate.plusDays(offset.toLong())
                val isSelected = date == selectedDate
                val indicator = dayIndicators[date.dayOfWeek]

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag("header-day-$date")
                            .clickable { onDateSelected(date) }
                            .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    dayContent(date, isSelected, indicator != null)
                    IndicatorDot(indicator = indicator)
                }
            }
        }
    }
}

@Composable
private fun DefaultDayContent(
    date: LocalDate,
    isSelected: Boolean,
    hasWorkout: Boolean,
) {
    val label =
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            .take(1)
            .uppercase(Locale.getDefault())

    Text(
        text = label,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
    )
    if (!hasWorkout) {
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
private fun IndicatorDot(indicator: DayIndicator?) {
    if (indicator == null) {
        Spacer(modifier = Modifier.size(8.dp))
        return
    }

    val isDarkTheme = isSystemInDarkTheme()
    val completedColor = TodoBlue
    val restDayColor = if (isDarkTheme) RestDaySurfaceDark else RestDaySurfaceLight
    val color =
        when (indicator) {
            DayIndicator.Workout -> CompletedBlue
            DayIndicator.RestDay -> restDayColor
            DayIndicator.Completed -> completedColor
        }

    Box(
        modifier =
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
    )
}

private fun formatWeekRange(
    start: LocalDate,
    end: LocalDate,
): String {
    val locale = Locale.getDefault()
    val startDay = start.dayOfMonth
    val endDay = end.dayOfMonth
    val startMonth = start.month.getDisplayName(TextStyle.SHORT, locale)
    val endMonth = end.month.getDisplayName(TextStyle.SHORT, locale)

    return when {
        start.year == end.year && start.month == end.month ->
            "$startDay-$endDay $startMonth ${start.year}"
        start.year == end.year ->
            "$startDay $startMonth - $endDay $endMonth ${start.year}"
        else -> "$startDay $startMonth ${start.year} - $endDay $endMonth ${end.year}"
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyCalendarHeaderPreview() {
    val selectedDate = LocalDate.of(2026, 1, 15)
    val weekStartDate = LocalDate.of(2026, 1, 12)
    val dayIndicators =
        mapOf(
            DayOfWeek.MONDAY to DayIndicator.Workout,
            DayOfWeek.WEDNESDAY to DayIndicator.Completed,
            DayOfWeek.FRIDAY to DayIndicator.RestDay,
        )

    WeeklyCalendarHeader(
        selectedDate = selectedDate,
        weekStartDate = weekStartDate,
        dayIndicators = dayIndicators,
        onDateSelected = {},
        onWeekChanged = {},
    )
}
