package com.rafaelfelipeac.hermes.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.model.WorkoutUi
import java.time.DayOfWeek.MONDAY
import java.time.DayOfWeek.WEDNESDAY
import java.time.LocalDate

private const val PREVIEW_WORKOUT_TYPE_RUN = "Run"
private const val PREVIEW_WORKOUT_TYPE_SWIM = "Swim"
private const val PREVIEW_WORKOUT_TYPE_BIKE = "Bike"
private const val PREVIEW_WORKOUT_DESCRIPTION_RUN = "Easy 5k"
private const val PREVIEW_WORKOUT_DESCRIPTION_SWIM = "Intervals 10x100"
private const val PREVIEW_WORKOUT_DESCRIPTION_BIKE = "Tempo 45 min"
private const val PREVIEW_YEAR = 2026
private const val PREVIEW_MONTH = 1
private const val PREVIEW_DAY = 15
private const val PREVIEW_WORKOUT_ID_RUN = 1L
private const val PREVIEW_WORKOUT_ID_SWIM = 2L
private const val PREVIEW_WORKOUT_ID_BIKE = 3L
private const val PREVIEW_ORDER = 0

data class WeeklyTrainingContentPreviewData(
    val selectedDate: LocalDate,
    val workouts: List<WorkoutUi>,
)

class WeeklyTrainingContentPreviewProvider :
    PreviewParameterProvider<WeeklyTrainingContentPreviewData> {
    override val values =
        sequenceOf(
            WeeklyTrainingContentPreviewData(
                selectedDate = LocalDate.of(PREVIEW_YEAR, PREVIEW_MONTH, PREVIEW_DAY),
                workouts =
                    listOf(
                        WorkoutUi(
                            id = PREVIEW_WORKOUT_ID_RUN,
                            dayOfWeek = null,
                            type = PREVIEW_WORKOUT_TYPE_RUN,
                            description = PREVIEW_WORKOUT_DESCRIPTION_RUN,
                            isCompleted = false,
                            isRestDay = false,
                            order = PREVIEW_ORDER,
                        ),
                        WorkoutUi(
                            id = PREVIEW_WORKOUT_ID_SWIM,
                            dayOfWeek = MONDAY,
                            type = PREVIEW_WORKOUT_TYPE_SWIM,
                            description = PREVIEW_WORKOUT_DESCRIPTION_SWIM,
                            isCompleted = false,
                            isRestDay = false,
                            order = PREVIEW_ORDER,
                        ),
                        WorkoutUi(
                            id = PREVIEW_WORKOUT_ID_BIKE,
                            dayOfWeek = WEDNESDAY,
                            type = PREVIEW_WORKOUT_TYPE_BIKE,
                            description = PREVIEW_WORKOUT_DESCRIPTION_BIKE,
                            isCompleted = true,
                            isRestDay = false,
                            order = PREVIEW_ORDER,
                        ),
                    ),
            ),
        )
}
