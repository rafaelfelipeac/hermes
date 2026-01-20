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
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.CompletedBlue
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceDark
import com.rafaelfelipeac.hermes.core.ui.theme.RestDaySurfaceLight
import com.rafaelfelipeac.hermes.core.ui.theme.TodoBlue
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val WEEK_DAY_COUNT = 7
private const val HEADER_TAG = "weekly-calendar-header"
private const val PREV_WEEK_TAG = "week-prev"
private const val NEXT_WEEK_TAG = "week-next"
private const val HEADER_DAY_TAG_PREFIX = "header-day-"
private const val SAME_MONTH_RANGE_FORMAT = "%d-%d %s %d"
private const val SAME_YEAR_RANGE_FORMAT = "%d %s - %d %s %d"
private const val CROSS_YEAR_RANGE_FORMAT = "%d %s %d - %d %s %d"
private const val WEEK_CHANGE_STEP = 1L

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
    val weekEndDate = weekStartDate.plusDays((WEEK_DAY_COUNT - 1).toLong())
    val swipeThreshold = with(LocalDensity.current) { Dimens.SwipeThreshold.toPx() }
    var dragAmount by remember { mutableStateOf(0f) }

    Column(
        modifier =
            modifier
                .testTag(HEADER_TAG)
                .pointerInput(selectedDate, onWeekChanged) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, amount ->
                            dragAmount += amount
                        },
                        onDragEnd = {
                            when {
                                dragAmount <= -swipeThreshold ->
                                    onWeekChanged(selectedDate.plusWeeks(WEEK_CHANGE_STEP))
                                dragAmount >= swipeThreshold ->
                                    onWeekChanged(selectedDate.minusWeeks(WEEK_CHANGE_STEP))
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
                onClick = { onWeekChanged(selectedDate.minusWeeks(WEEK_CHANGE_STEP)) },
                modifier = Modifier.testTag(PREV_WEEK_TAG),
            ) {
                Text(text = stringResource(R.string.week_previous))
            }

            Text(text = formatWeekRange(weekStartDate, weekEndDate))

            IconButton(
                onClick = { onWeekChanged(selectedDate.plusWeeks(WEEK_CHANGE_STEP)) },
                modifier = Modifier.testTag(NEXT_WEEK_TAG),
            ) {
                Text(text = stringResource(R.string.week_next))
            }
        }

        Spacer(modifier = Modifier.width(Dimens.SpacingMd))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            for (offset in 0 until WEEK_DAY_COUNT) {
                val date = weekStartDate.plusDays(offset.toLong())
                val isSelected = date == selectedDate
                val indicator = dayIndicators[date.dayOfWeek]

                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag("$HEADER_DAY_TAG_PREFIX$date")
                            .clickable { onDateSelected(date) }
                            .padding(vertical = Dimens.SpacingMd),
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
        Spacer(modifier = Modifier.size(Dimens.IndicatorSize))
    }
}

@Composable
private fun IndicatorDot(indicator: DayIndicator?) {
    if (indicator == null) {
        Spacer(modifier = Modifier.size(Dimens.IndicatorSize))
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
                .size(Dimens.IndicatorSize)
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
            String.format(locale, SAME_MONTH_RANGE_FORMAT, startDay, endDay, startMonth, start.year)
        start.year == end.year ->
            String.format(
                locale,
                SAME_YEAR_RANGE_FORMAT,
                startDay,
                startMonth,
                endDay,
                endMonth,
                start.year,
            )
        else ->
            String.format(
                locale,
                CROSS_YEAR_RANGE_FORMAT,
                startDay,
                startMonth,
                start.year,
                endDay,
                endMonth,
                end.year,
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun WeeklyCalendarHeaderPreview(
    @PreviewParameter(WeeklyCalendarHeaderPreviewProvider::class)
    preview: WeeklyCalendarHeaderPreviewData,
) {
    WeeklyCalendarHeader(
        selectedDate = preview.selectedDate,
        weekStartDate = preview.weekStartDate,
        dayIndicators = preview.dayIndicators,
        onDateSelected = {},
        onWeekChanged = {},
    )
}
