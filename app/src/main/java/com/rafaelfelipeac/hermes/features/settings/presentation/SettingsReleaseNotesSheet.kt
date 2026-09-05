package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ReleaseNotesBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReleaseNotesBottomSheet(
    definition: ReleaseNotesDefinition,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag(SETTINGS_RELEASE_NOTES_SHEET_TAG),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingXl)
                    .padding(bottom = ReleaseNotesBottomPadding)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(SpacingMd),
        ) {
            Text(
                text =
                    stringResource(
                        R.string.settings_release_notes_title,
                        definition.normalizedVersion,
                    ),
                style = typography.titleMedium,
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                definition.sections.forEach { section ->
                    ReleaseNotesSection(
                        title = stringResource(section.titleRes),
                        items = stringArrayResource(section.itemsRes).toList(),
                    )
                }
            }
        }
    }
}

@Composable
private fun ReleaseNotesSection(
    title: String,
    items: List<String>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SpacingSm)) {
        Text(
            text = title,
            style =
                typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                ),
            color = colorScheme.primary,
        )

        Column(verticalArrangement = Arrangement.spacedBy(SpacingXs)) {
            items.forEach { item ->
                Row(horizontalArrangement = Arrangement.spacedBy(SpacingSm)) {
                    Text(
                        text = stringResource(R.string.settings_release_notes_bullet),
                        style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colorScheme.primary,
                    )
                    Text(
                        text = item,
                        style = typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
