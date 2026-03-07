package com.rafaelfelipeac.hermes.features.settings.presentation

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay

data class SettingsState(
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val slotModePolicy: SlotModePolicy,
    val weekStartDay: WeekStartDay,
    val lastBackupExportedAt: String?,
    val lastBackupImportedAt: String?,
    val backupFolderUri: String?,
)
