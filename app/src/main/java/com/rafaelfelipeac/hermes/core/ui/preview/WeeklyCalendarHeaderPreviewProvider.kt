package com.rafaelfelipeac.hermes.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.rafaelfelipeac.hermes.core.ui.components.calendar.DayIndicator
import java.time.DayOfWeek
import java.time.LocalDate

private const val PREVIEW_YEAR = 2026
private const val PREVIEW_MONTH = 1
private const val PREVIEW_SELECTED_DAY = 15
private const val PREVIEW_WEEK_START_DAY = 12

data class WeeklyCalendarHeaderPreviewData(
    val selectedDate: LocalDate,
    val weekStartDate: LocalDate,
    val dayIndicators: Map<DayOfWeek, DayIndicator>,
)

class WeeklyCalendarHeaderPreviewProvider :
    PreviewParameterProvider<WeeklyCalendarHeaderPreviewData> {
    override val values =
        sequenceOf(
            WeeklyCalendarHeaderPreviewData(
                selectedDate = LocalDate.of(PREVIEW_YEAR, PREVIEW_MONTH, PREVIEW_SELECTED_DAY),
                weekStartDate = LocalDate.of(PREVIEW_YEAR, PREVIEW_MONTH, PREVIEW_WEEK_START_DAY),
                dayIndicators =
                    mapOf(
                        DayOfWeek.MONDAY to DayIndicator.Workout,
                        DayOfWeek.WEDNESDAY to DayIndicator.Completed,
                        DayOfWeek.FRIDAY to DayIndicator.RestDay,
                    ),
            ),
        )
}
