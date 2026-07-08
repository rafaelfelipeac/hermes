package com.rafaelfelipeac.hermes.features.settings.domain.repository

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.PaceUnit
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<AppLanguage>
    val slotModePolicy: Flow<SlotModePolicy>
    val weekStartDay: Flow<WeekStartDay>
    val distanceUnit: Flow<DistanceUnit>
    val paceUnit: Flow<PaceUnit>
    val weightUnit: Flow<WeightUnit>
    val lastBackupExportedAt: Flow<String?>
    val lastBackupImportedAt: Flow<String?>
    val backupFolderUri: Flow<String?>
    val lastSeenTrophyCelebrationToken: Flow<String?>

    fun initialThemeMode(): ThemeMode

    fun initialLanguage(): AppLanguage

    fun initialSlotModePolicy(): SlotModePolicy

    fun initialWeekStartDay(): WeekStartDay

    fun initialDistanceUnit(): DistanceUnit

    fun initialPaceUnit(): PaceUnit

    fun initialWeightUnit(): WeightUnit

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setLanguage(language: AppLanguage)

    suspend fun setSlotModePolicy(policy: SlotModePolicy)

    suspend fun setWeekStartDay(weekStartDay: WeekStartDay)

    suspend fun setDistanceUnit(distanceUnit: DistanceUnit)

    suspend fun setPaceUnit(paceUnit: PaceUnit)

    suspend fun setWeightUnit(weightUnit: WeightUnit)

    suspend fun setLastBackupExportedAt(value: String)

    suspend fun setLastBackupImportedAt(value: String)

    suspend fun setBackupFolderUri(value: String?)

    suspend fun setLastSeenTrophyCelebrationToken(value: String?)
}
