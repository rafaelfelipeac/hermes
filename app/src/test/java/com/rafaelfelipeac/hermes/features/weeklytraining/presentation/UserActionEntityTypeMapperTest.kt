package com.rafaelfelipeac.hermes.features.weeklytraining.presentation

import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Test

class UserActionEntityTypeMapperTest {
    @Test
    fun moveAndReorderActionTypes_preserveNonWorkoutEventTypes() {
        assertEquals(UserActionType.REORDER_REST, EventType.REST.toReorderActionType())
        assertEquals(UserActionType.MOVE_REST, EventType.REST.toMoveActionType())
        assertEquals(UserActionType.UNDO_REORDER_REST, EventType.REST.toUndoReorderActionType())
        assertEquals(UserActionType.UNDO_MOVE_REST, EventType.REST.toUndoMoveActionType())

        assertEquals(UserActionType.REORDER_BUSY, EventType.BUSY.toReorderActionType())
        assertEquals(UserActionType.MOVE_BUSY, EventType.BUSY.toMoveActionType())
        assertEquals(UserActionType.UNDO_REORDER_BUSY, EventType.BUSY.toUndoReorderActionType())
        assertEquals(UserActionType.UNDO_MOVE_BUSY, EventType.BUSY.toUndoMoveActionType())

        assertEquals(UserActionType.REORDER_SICK, EventType.SICK.toReorderActionType())
        assertEquals(UserActionType.MOVE_SICK, EventType.SICK.toMoveActionType())
        assertEquals(UserActionType.UNDO_REORDER_SICK, EventType.SICK.toUndoReorderActionType())
        assertEquals(UserActionType.UNDO_MOVE_SICK, EventType.SICK.toUndoMoveActionType())
    }
}
