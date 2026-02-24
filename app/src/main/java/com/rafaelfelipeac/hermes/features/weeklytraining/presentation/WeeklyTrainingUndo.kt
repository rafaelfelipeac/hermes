package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.TimeSlot
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import java.time.DayOfWeek
import java.time.LocalDate

data class UndoState(
    val id: Long,
    val message: UndoMessage,
    val action: PendingUndoAction,
)

enum class UndoMessage {
    Moved,
    Deleted,
    Completed,
    MarkedIncomplete,
    WeekCopied,
}

data class WorkoutPosition(
    val id: Long,
    val dayOfWeek: DayOfWeek?,
    val timeSlot: TimeSlot?,
    val order: Int,
)

sealed class PendingUndoAction {
    data class MoveOrReorder(
        val movedWorkoutId: Long,
        val movedEventType: EventType,
        val previousPositions: List<WorkoutPosition>,
        val weekStartDate: LocalDate,
    ) : PendingUndoAction()

    data class Delete(
        val workout: WorkoutUi,
        val weekStartDate: LocalDate,
        val previousPositions: List<WorkoutPosition>,
    ) : PendingUndoAction()

    data class Completion(
        val workout: WorkoutUi,
        val previousCompleted: Boolean,
        val newCompleted: Boolean,
        val weekStartDate: LocalDate,
    ) : PendingUndoAction()

    data class ReplaceWeek(
        val weekStartDate: LocalDate,
        val previousWorkouts: List<Workout>,
    ) : PendingUndoAction()
}
