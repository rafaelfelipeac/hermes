package com.rafaelfelipeac.hermes.features.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXs
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
) {
    SettingsDetailScreen(
        title = stringResource(R.string.settings_backup_title),
        onBack = onBack,
        onHelpClick = onHelpClick,
        helpContentDescription = stringResource(R.string.settings_backup_help_title),
        contentInsideCard = false,
        modifier = modifier,
    ) {
        SettingsCard {
            SettingsBackupActionRow(
                label = stringResource(R.string.settings_export_backup_title),
                detail = backupExportLabel(state.lastBackupExportedAt),
                onClick = onExportClick,
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

            SettingsBackupActionRow(
                label = stringResource(R.string.settings_import_backup_title),
                detail = backupImportLabel(state.lastBackupImportedAt),
                onClick = onImportClick,
            )
        }

        SettingsCard {
            SettingsBackupActionRow(
                label = stringResource(R.string.settings_backup_folder_title),
                detail = backupFolderLabel(state.backupFolderUri),
                onClick = onSelectFolderClick,
            )

            if (state.backupFolderUri != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = SpacingXs))

                SettingsBackupActionRow(
                    label = stringResource(R.string.settings_backup_folder_clear),
                    detail = stringResource(R.string.settings_backup_folder_clear_detail),
                    onClick = onClearFolderClick,
                )
            }
        }
    }
}

@Composable
internal fun SettingsBackupActionRow(
    label: String,
    detail: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(SpacingXxs),
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
            )
            Text(
                text = detail,
                style = typography.bodySmall,
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
        )
    }
}

@Composable
private fun backupExportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp) ?: never

    return stringResource(R.string.settings_backup_last_exported, formatted)
}

@Composable
private fun backupImportLabel(rawTimestamp: String?): String {
    val never = stringResource(R.string.settings_backup_never)
    val formatted = formatBackupTimestamp(rawTimestamp) ?: never

    return stringResource(R.string.settings_backup_last_imported, formatted)
}

@Composable
private fun backupFolderLabel(rawUri: String?): String {
    return if (rawUri.isNullOrBlank()) {
        stringResource(R.string.settings_backup_folder_default)
    } else {
        stringResource(R.string.settings_backup_folder_selected)
    }
}

@Composable
private fun formatBackupTimestamp(rawTimestamp: String?): String? {
    if (rawTimestamp.isNullOrBlank()) return null

    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current).get(0) ?: Locale.getDefault()
    val zoneId = ZoneId.systemDefault()
    val formatter =
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .withZone(zoneId)

    return runCatching {
        formatter.format(Instant.parse(rawTimestamp))
    }.getOrDefault(rawTimestamp)
}
