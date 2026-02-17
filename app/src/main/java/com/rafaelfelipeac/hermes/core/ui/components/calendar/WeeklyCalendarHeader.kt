package com.rafaelfelipeac.hermes.core.ui.components.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Normal
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.WeeklyCalendarHeaderPreviewProvider
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.IndicatorSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SwipeThreshold
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDayIndicator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle.SHORT
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
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    weekStartDate: LocalDate,
    dayIndicators: Map<DayOfWeek, WorkoutDayIndicator>,
    onDateSelected: (LocalDate) -> Unit,
    onWeekChanged: (LocalDate) -> Unit,
) {
    val weekEndDate = weekStartDate.plusDays((WEEK_DAY_COUNT - 1).toLong())
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

            Text(text = formatWeekRange(weekStartDate, weekEndDate))

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
                    DayIndicator(
                        date = date,
                        isSelected = isSelected,
                        indicator = indicator,
                    )
                }
            }
        }
    }
}

@Composable
private fun DayIndicator(
    date: LocalDate,
    isSelected: Boolean,
    indicator: WorkoutDayIndicator?,
) {
    val label =
        date.dayOfWeek.getDisplayName(SHORT, Locale.getDefault())
            .take(1)
            .uppercase(Locale.getDefault())
    val isDarkTheme = colorScheme.background.luminance() < 0.5f
    val indicatorColor =
        indicator?.let {
            val baseColor =
                if (it.workout.isRestDay) {
                    colorScheme.surfaceColorAtElevation(ElevationSm)
                } else {
                    workoutIndicatorColor(
                        workout = it.workout,
                        isDarkTheme = isDarkTheme,
                        surface = colorScheme.surface,
                    )
                }
            if (it.workout.isRestDay) {
                baseColor
            } else {
                themedIndicatorColor(baseColor, isDarkTheme = isDarkTheme)
            }
        }
    val contentColor =
        when {
            indicator == null -> colorScheme.onSurface
            indicator.workout.isRestDay -> colorScheme.onSurfaceVariant
            indicatorColor != null -> readableContentOn(indicatorColor)
            else -> colorScheme.onSurface
        }
    val containerHeight = IndicatorSize + SpacingXxl
    val containerWidth = IndicatorSize + SpacingXs
    val shape = RoundedCornerShape(SpacingSm)

    Box(
        modifier =
            Modifier
                .size(width = containerWidth, height = containerHeight)
                .clip(shape)
                .background(indicatorColor ?: Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontWeight = if (isSelected) Bold else Normal,
                color = contentColor,
            )

            if (indicator?.isDayCompleted == true && indicator.workout.isRestDay.not()) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier =
                        Modifier
                            .size(SpacingXxl)
                            .padding(bottom = SpacingXs),
                )
            } else {
                Spacer(
                    modifier =
                        Modifier
                            .size(SpacingXxl)
                            .padding(vertical = SpacingXxs),
                )
            }
        }
    }
}

private fun formatWeekRange(
    start: LocalDate,
    end: LocalDate,
): String {
    val locale = Locale.getDefault()
    val startDay = start.dayOfMonth
    val endDay = end.dayOfMonth
    val startMonth = start.month.getDisplayName(SHORT, locale)
    val endMonth = end.month.getDisplayName(SHORT, locale)

    return when {
        start.year == end.year && start.month == end.month ->
            String.format(
                locale,
                SAME_MONTH_RANGE_FORMAT,
                startDay,
                endDay,
                startMonth,
                start.year,
            )

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

private fun readableContentOn(background: Color): Color {
    return if (background.luminance() > 0.5f) Color.Black else Color.White
}

private fun themedIndicatorColor(
    base: Color,
    isDarkTheme: Boolean,
): Color {
    val blend = if (isDarkTheme) 0.2f else 0.2f
    val target = if (isDarkTheme) Color.White else Color.Black
    return lerp(base, target, blend)
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
