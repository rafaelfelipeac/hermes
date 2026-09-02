package com.rafaelfelipeac.hermes.features.challenges.presentation.model

import androidx.compose.runtime.saveable.listSaver
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import com.rafaelfelipeac.hermes.features.challenges.presentation.ChallengeListTab
import java.time.LocalDate

internal data class ChallengeEditorDraft(
    val originTab: ChallengeListTab,
    val origin: ChallengeEditorOrigin,
    val editorState: ChallengeEditorState,
) {
    companion object {
        val Saver =
            listSaver<ChallengeEditorDraft?, Any?>(
                save =
                    { draft ->
                        if (draft == null) {
                            emptyList()
                        } else {
                            listOf(
                                draft.originTab.name,
                                draft.origin.name,
                                draft.editorState.challengeId,
                                draft.editorState.categoryId,
                                draft.editorState.title,
                                draft.editorState.description,
                                draft.editorState.targetType.name,
                                draft.editorState.targetQuantityText,
                                draft.editorState.startDate?.toEpochDay(),
                                draft.editorState.endDate?.toEpochDay(),
                                draft.editorState.lifecycle.name,
                                draft.editorState.isDirty,
                                draft.editorState.validationMessage,
                            )
                        }
                    },
                restore =
                    { restored ->
                        if (restored.isEmpty()) {
                            null
                        } else {
                            ChallengeEditorDraft(
                                originTab = ChallengeListTab.valueOf(restored[0] as String),
                                origin = ChallengeEditorOrigin.valueOf(restored[1] as String),
                                editorState =
                                    ChallengeEditorState(
                                        challengeId = restored[2] as Long?,
                                        categoryId = restored[3] as Long?,
                                        title = restored[4] as String,
                                        description = restored[5] as String,
                                        targetType = ChallengeTargetType.valueOf(restored[6] as String),
                                        targetQuantityText = restored[7] as String,
                                        startDate = (restored[8] as Long?)?.let(LocalDate::ofEpochDay),
                                        endDate = (restored[9] as Long?)?.let(LocalDate::ofEpochDay),
                                        lifecycle = ChallengeLifecycle.valueOf(restored[10] as String),
                                        isDirty = restored[11] as Boolean,
                                        validationMessage = restored[12] as String?,
                                    ),
                            )
                        }
                    },
            )
    }
}

internal enum class ChallengeEditorOrigin {
    LIST,
    DETAIL,
}
