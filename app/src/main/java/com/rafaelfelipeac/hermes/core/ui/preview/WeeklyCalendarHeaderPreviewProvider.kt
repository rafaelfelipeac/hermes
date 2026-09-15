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
private const val PREVIEW_WORKOUT_TYPE_RUN = "Run"
private const val PREVIEW_WORKOUT_TYPE_SWIM = "Swim"
private const val PREVIEW_WORKOUT_COLOR_RUN = "run"
private const val PREVIEW_WORKOUT_COLOR_SWIM = "swim"

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
                                        type = PREVIEW_WORKOUT_TYPE_RUN,
                                        description = "Easy",
                                        isCompleted = false,
                                        isRestDay = false,
                                        categoryId = 1L,
                                        categoryColorId = PREVIEW_WORKOUT_COLOR_RUN,
                                        categoryName = PREVIEW_WORKOUT_TYPE_RUN,
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
                                        type = PREVIEW_WORKOUT_TYPE_SWIM,
                                        description = "Intervals",
                                        isCompleted = true,
                                        isRestDay = false,
                                        categoryId = 2L,
                                        categoryColorId = PREVIEW_WORKOUT_COLOR_SWIM,
                                        categoryName = PREVIEW_WORKOUT_TYPE_SWIM,
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
