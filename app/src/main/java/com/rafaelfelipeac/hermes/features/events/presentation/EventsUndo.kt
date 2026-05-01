package com.rafaelfelipeac.hermes.features.events.presentation

import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.UndoMessage
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi

data class EventUndoState(
    val id: Long,
    val message: UndoMessage,
    val action: PendingEventUndoAction,
)

sealed class PendingEventUndoAction {
    data class Delete(val event: WorkoutUi) : PendingEventUndoAction()

    data class Completion(
        val event: WorkoutUi,
        val previousCompleted: Boolean,
        val newCompleted: Boolean,
    ) : PendingEventUndoAction()
}
