package com.rafaelfelipeac.hermes.features.events.presentation.model

import androidx.compose.runtime.saveable.listSaver
import java.time.LocalDate

data class EventDialogDraft(
    val eventId: Long?,
    val title: String,
    val description: String,
    val categoryId: Long?,
    val eventDate: LocalDate?,
) {
    companion object {
        val Saver =
            listSaver<EventDialogDraft?, Any?>(
                save =
                    { draft ->
                        if (draft == null) {
                            emptyList()
                        } else {
                            listOf(
                                draft.eventId,
                                draft.title,
                                draft.description,
                                draft.categoryId,
                                draft.eventDate?.toEpochDay(),
                            )
                        }
                    },
                restore =
                    { restored ->
                        if (restored.isEmpty()) {
                            null
                        } else {
                            EventDialogDraft(
                                eventId = restored[0] as Long?,
                                title = restored[1] as String,
                                description = restored[2] as String,
                                categoryId = restored[3] as Long?,
                                eventDate = (restored[4] as Long?)?.let(LocalDate::ofEpochDay),
                            )
                        }
                    },
            )
    }
}
