package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.currentLocale
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SettingsRowMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import java.time.ZoneId

@Composable
internal fun SettingsBackupScreen(
    state: SettingsState,
    onBack: () -> Unit,
    onHelpClick: () -> Unit,
    modifier: Modifier = Modifier,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit,
    onSelectFolderClick: () -> Unit,
    onClearFolderClick: () -> Unit,
    isOperationInProgress: Boolean = false,
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_backup_title),
        onBack = onBack,
        onHelpClick = onHelpClick,
        helpContentDescription = stringResource(R.string.settings_backup_help_title),
        contentInsideCard = false,
        modifier = modifier,
    ) {
        SettingsBackupActionRow(
            label = stringResource(R.string.settings_export_backup_title),
            detail = backupExportLabel(state.lastBackupExportedAt),
            onClick = onExportClick,
            enabled = !isOperationInProgress,
        )

        SettingsBackupActionRow(
            label = stringResource(R.string.settings_import_backup_title),
            detail = backupImportLabel(state.lastBackupImportedAt),
            onClick = onImportClick,
            enabled = !isOperationInProgress,
        )

        HorizontalDivider(color = colorScheme.outlineVariant)

        SettingsBackupActionRow(
            label = stringResource(R.string.settings_backup_folder_title),
            detail = backupFolderLabel(state.backupFolderUri),
            onClick = onSelectFolderClick,
            enabled = !isOperationInProgress,
        )

        if (state.backupFolderUri != null) {
            SettingsBackupActionRow(
                label = stringResource(R.string.settings_backup_folder_clear),
                detail = stringResource(R.string.settings_backup_folder_clear_detail),
                onClick = onClearFolderClick,
                enabled = !isOperationInProgress,
            )
        }
    }
}

@Composable
internal fun SettingsBackupActionRow(
    label: String,
    detail: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    val textColor = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant
    val detailColor = colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        enabled = enabled,
        tonalElevation = ElevationSm,
        shape = shapes.medium,
        modifier =
            Modifier
                .fillMaxWidth()
                .then(if (enabled) Modifier else Modifier.semantics { disabled() }),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = SettingsRowMinHeight)
                    .padding(horizontal = SpacingLg, vertical = SpacingMd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SpacingXxs),
            ) {
                Text(
                    text = label,
                    style = typography.bodyLarge,
                    color = textColor,
                )
                Text(
                    text = detail,
                    style = typography.bodySmall,
                    color = detailColor,
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = detailColor,
            )
        }
    }
}

@Composable
private fun backupExportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp, currentLocale(), ZoneId.systemDefault()) ?: never

    return stringResource(R.string.settings_backup_last_exported, formatted)
}

@Composable
private fun backupImportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp, currentLocale(), ZoneId.systemDefault()) ?: never

    return stringResource(R.string.settings_backup_last_imported, formatted)
}

@Composable
private fun backupFolderLabel(rawUri: String?): String = stringResource(backupFolderLabelRes(rawUri))
