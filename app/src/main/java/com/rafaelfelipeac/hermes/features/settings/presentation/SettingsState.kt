package com.rafaelfelipeac.hermes.features.settings.presentation

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit

data class SettingsState(
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val slotModePolicy: SlotModePolicy,
    val weekStartDay: WeekStartDay,
    val distanceUnit: DistanceUnit = DistanceUnit.KILOMETERS,
    val paceUnit: PaceUnit = PaceUnit.MIN_PER_KM,
    val weightUnit: WeightUnit = WeightUnit.KILOGRAMS,
    val lastBackupExportedAt: String? = null,
    val lastBackupImportedAt: String? = null,
    val backupFolderUri: String? = null,
)
