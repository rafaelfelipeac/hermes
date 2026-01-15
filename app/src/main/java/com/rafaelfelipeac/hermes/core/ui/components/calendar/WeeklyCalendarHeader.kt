package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rafaelfelipeac.hermes.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun WeeklyCalendarHeader(
    selectedDate: LocalDate,
    weekStartDate: LocalDate,
    daysWithWorkouts: Set<DayOfWeek>,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dayContent: @Composable (
        date: LocalDate,
        isSelected: Boolean,
        hasWorkout: Boolean
    ) -> Unit = { date, isSelected, hasWorkout ->
        DefaultDayContent(date = date, isSelected = isSelected, hasWorkout = hasWorkout)
    }
) {
    val weekEndDate = weekStartDate.plusDays(6)

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onWeekChanged(selectedDate.minusWeeks(1)) }) {
                Text(text = stringResource(R.string.week_previous))
            }

            Text(text = formatWeekRange(weekStartDate, weekEndDate))

            IconButton(onClick = { onWeekChanged(selectedDate.plusWeeks(1)) }) {
                Text(text = stringResource(R.string.week_next))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (offset in 0..6) {
                val date = weekStartDate.plusDays(offset.toLong())
                val isSelected = date == selectedDate
                val hasWorkout = daysWithWorkouts.contains(date.dayOfWeek)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    dayContent(date, isSelected, hasWorkout)
                }
            }
        }
    }
}

@Composable
private fun DefaultDayContent(
    date: LocalDate,
    isSelected: Boolean,
    hasWorkout: Boolean
) {
    val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
        .take(1)
        .uppercase(Locale.getDefault())
    val indicator = if (hasWorkout) {
        stringResource(R.string.workout_indicator)
    } else {
        stringResource(R.string.workout_indicator_empty)
    }

    Text(
        text = label,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
    Text(text = indicator)
}

private fun formatWeekRange(start: LocalDate, end: LocalDate): String {
    val locale = Locale.getDefault()
    val startDay = start.dayOfMonth
    val endDay = end.dayOfMonth
    val startMonth = start.month.getDisplayName(TextStyle.SHORT, locale)
    val endMonth = end.month.getDisplayName(TextStyle.SHORT, locale)

    return when (start.year) {
        end.year if start.month == end.month ->
            "$startDay-$endDay $startMonth ${start.year}"
        end.year ->
            "$startDay $startMonth - $endDay $endMonth ${start.year}"
        else -> "$startDay $startMonth ${start.year} - $endDay $endMonth ${end.year}"
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyCalendarHeaderPreview() {
    val selectedDate = LocalDate.of(2026, 1, 15)
    val weekStartDate = LocalDate.of(2026, 1, 12)
    val daysWithWorkouts = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)

    WeeklyCalendarHeader(
        selectedDate = selectedDate,
        weekStartDate = weekStartDate,
        daysWithWorkouts = daysWithWorkouts,
        onDateSelected = {},
        onWeekChanged = {}
    )
}
