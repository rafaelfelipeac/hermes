package com.rafaelfelipeac.hermes.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

private const val PREVIEW_WORKOUT_TYPE = "Run"
private const val PREVIEW_WORKOUT_DESCRIPTION = "Easy 5k"

data class AddWorkoutDialogPreviewData(
    val isEdit: Boolean,
    val initialType: String,
    val initialDescription: String,
)

class AddWorkoutDialogPreviewProvider : PreviewParameterProvider<AddWorkoutDialogPreviewData> {
    override val values =
        sequenceOf(
            AddWorkoutDialogPreviewData(
                isEdit = false,
                initialType = "",
                initialDescription = "",
            ),
            AddWorkoutDialogPreviewData(
                isEdit = true,
                initialType = PREVIEW_WORKOUT_TYPE,
                initialDescription = PREVIEW_WORKOUT_DESCRIPTION,
            ),
        )
}
