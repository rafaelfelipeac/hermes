package com.rafaelfelipeac.hermes.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDayIndicator
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

private const val PREVIEW_YEAR = 2026
private const val PREVIEW_MONTH = 1
private const val PREVIEW_SELECTED_DAY = 15
private const val PREVIEW_WEEK_START_DAY = 12
private const val PREVIEW_ORDER = 0

data class WeeklyCalendarHeaderPreviewData(
    val selectedDate: LocalDate,
    val weekStartDate: LocalDate,
    val dayIndicators: Map<DayOfWeek, WorkoutDayIndicator>,
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
                        DayOfWeek.MONDAY to
                            WorkoutDayIndicator(
                                workout =
                                    WorkoutUi(
                                        id = 1L,
                                        dayOfWeek = DayOfWeek.MONDAY,
                                        type = "Run",
                                        description = "Easy",
                                        isCompleted = false,
                                        isRestDay = false,
                                        categoryId = 1L,
                                        categoryColorId = "run",
                                        categoryName = "Run",
                                        order = PREVIEW_ORDER,
                                    ),
                                isDayCompleted = false,
                            ),
                        DayOfWeek.WEDNESDAY to
                            WorkoutDayIndicator(
                                workout =
                                    WorkoutUi(
                                        id = 2L,
                                        dayOfWeek = DayOfWeek.WEDNESDAY,
                                        type = "Swim",
                                        description = "Intervals",
                                        isCompleted = true,
                                        isRestDay = false,
                                        categoryId = 2L,
                                        categoryColorId = "swim",
                                        categoryName = "Swim",
                                        order = PREVIEW_ORDER,
                                    ),
                                isDayCompleted = true,
                            ),
                        DayOfWeek.FRIDAY to
                            WorkoutDayIndicator(
                                workout =
                                    WorkoutUi(
                                        id = 3L,
                                        dayOfWeek = DayOfWeek.FRIDAY,
                                        type = EMPTY,
                                        description = EMPTY,
                                        isCompleted = false,
                                        isRestDay = true,
                                        categoryId = null,
                                        categoryColorId = null,
                                        categoryName = null,
                                        order = PREVIEW_ORDER,
                                    ),
                                isDayCompleted = true,
                            ),
                    ),
            ),
        )
}
