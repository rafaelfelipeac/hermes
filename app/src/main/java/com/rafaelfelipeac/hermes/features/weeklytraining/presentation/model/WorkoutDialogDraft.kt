package com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model

import androidx.compose.runtime.saveable.listSaver
import java.time.LocalDate

data class WorkoutDialogDraft(
    val workoutId: Long?,
    val type: String,
    val description: String,
    val categoryId: Long?,
    val eventDate: LocalDate? = null,
    val isRaceEvent: Boolean = false,
) {
    companion object {
        val Saver =
            listSaver<WorkoutDialogDraft?, Any?>(
                save =
                    { draft ->
                        if (draft == null) {
                            emptyList()
                        } else {
                            listOf(
                                draft.workoutId,
                                draft.type,
                                draft.description,
                                draft.categoryId,
                                draft.eventDate?.toEpochDay(),
                                draft.isRaceEvent,
                            )
                        }
                    },
                restore =
                    { restored ->
                        if (restored.isEmpty()) {
                            null
                        } else {
                            WorkoutDialogDraft(
                                workoutId = restored[0] as Long?,
                                type = restored[1] as String,
                                description = restored[2] as String,
                                categoryId = restored[3] as Long?,
                                eventDate = (restored[4] as Long?)?.let(LocalDate::ofEpochDay),
                                isRaceEvent = restored[5] as Boolean,
                            )
                        }
                    },
            )
    }
}
