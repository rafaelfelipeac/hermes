package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import java.time.LocalDate

internal data class WorkoutChangeDependencies(
    val repository: WeeklyTrainingRepository,
    val userActionLogger: UserActionLogger,
    val weekStartDate: LocalDate,
    val displayStartDay: WeekStartDay,
    val unassignedStorageWeekStart: LocalDate,
)
