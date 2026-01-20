package com.rafaelfelipeac.hermes.core.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

data class HermesAppPreviewData(
    val darkTheme: Boolean,
)

class HermesAppPreviewProvider : PreviewParameterProvider<HermesAppPreviewData> {
    override val values =
        sequenceOf(
            HermesAppPreviewData(darkTheme = false),
            HermesAppPreviewData(darkTheme = true),
        )
}
