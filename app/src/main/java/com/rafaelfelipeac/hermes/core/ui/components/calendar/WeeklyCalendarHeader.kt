package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SwipeThreshold
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDayIndicator
import java.time.DayOfWeek
import java.time.LocalDate

private const val WEEK_DAY_COUNT = 7
private const val HEADER_TAG = "weekly-calendar-header"
private const val PREV_WEEK_TAG = "week-prev"
private const val NEXT_WEEK_TAG = "week-next"
private const val HEADER_DAY_TAG_PREFIX = "header-day-"
private const val WEEK_CHANGE_STEP = 1L

@Composable
fun WeeklyCalendarHeader(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    weekStartDate: LocalDate,
    dayIndicators: Map<DayOfWeek, WorkoutDayIndicator>,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (LocalDate) -> Unit,
) {
    val weekEndDate = weekStartDate.plusDays((WEEK_DAY_COUNT - 1).toLong())
    val locale = currentLocale()
    val swipeThreshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    var dragAmount by remember { mutableFloatStateOf(0f) }

    Column(
        modifier =
            modifier
                .testTag(HEADER_TAG)
                .pointerInput(selectedDate, onWeekChanged) {
                    detectHorizontalDragGestures(
                        onDragStart = { dragAmount = 0f },
                        onHorizontalDrag = { _, dragDelta -> dragAmount += dragDelta },
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
                Icon(
                    imageVector = Icons.Outlined.ChevronLeft,
                    contentDescription = stringResource(R.string.weekly_training_week_previous),
                )
            }

            Text(text = formatWeekRange(weekStartDate, weekEndDate, locale))

            IconButton(
                onClick = { onWeekChanged(selectedDate.plusWeeks(WEEK_CHANGE_STEP)) },
                modifier = Modifier.testTag(NEXT_WEEK_TAG),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = stringResource(R.string.weekly_training_week_next),
                )
            }
        }

        Spacer(modifier = Modifier.width(SpacingMd))

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
                            .padding(vertical = SpacingMd),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WeeklyCalendarDayIndicator(
                        date = date,
                        isSelected = isSelected,
                        indicator = indicator,
                    )
                }
            }
        }
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
