package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : SettingsRepository {
        private val dataStore = context.settingsDataStore

        override val themeMode: Flow<ThemeMode> =
            dataStore.data
                .map { prefs ->
                    prefs[THEME_MODE_KEY]
                        ?.let { raw -> runCatching { ThemeMode.valueOf(raw) }.getOrNull() }
                        ?: defaultThemeMode()
                }
                .distinctUntilChanged()

        override val language: Flow<AppLanguage> =
            dataStore.data
                .map { prefs ->
                    prefs[LANGUAGE_KEY]?.let(AppLanguage::fromTag) ?: defaultLanguage()
                }
                .distinctUntilChanged()

        override val slotModePolicy: Flow<SlotModePolicy> =
            dataStore.data
                .map { prefs ->
                    prefs[SLOT_MODE_POLICY_KEY]
                        ?.let { raw -> runCatching { SlotModePolicy.valueOf(raw) }.getOrNull() }
                        ?: defaultSlotModePolicy()
                }
                .distinctUntilChanged()

        override val weekStartDay: Flow<WeekStartDay> =
            dataStore.data
                .map { prefs ->
                    WeekStartDay.fromStoredValue(prefs[WEEK_START_DAY_KEY])
                }
                .distinctUntilChanged()

        override val lastBackupExportedAt: Flow<String?> =
            dataStore.data
                .map { prefs -> prefs[LAST_BACKUP_EXPORTED_AT_KEY] }
                .distinctUntilChanged()

        override val lastBackupImportedAt: Flow<String?> =
            dataStore.data
                .map { prefs -> prefs[LAST_BACKUP_IMPORTED_AT_KEY] }
                .distinctUntilChanged()

        override val backupFolderUri: Flow<String?> =
            dataStore.data
                .map { prefs -> prefs[BACKUP_FOLDER_URI_KEY] }
                .distinctUntilChanged()

        override fun initialThemeMode(): ThemeMode = defaultThemeMode()

        override fun initialLanguage(): AppLanguage = defaultLanguage()

        override fun initialSlotModePolicy(): SlotModePolicy = defaultSlotModePolicy()

        override fun initialWeekStartDay(): WeekStartDay = defaultWeekStartDay()

        override suspend fun setThemeMode(mode: ThemeMode) {
            dataStore.edit { prefs ->
                prefs[THEME_MODE_KEY] = mode.name
            }
        }

        override suspend fun setLanguage(language: AppLanguage) {
            dataStore.edit { prefs ->
                prefs[LANGUAGE_KEY] = language.tag
            }
        }

        override suspend fun setSlotModePolicy(policy: SlotModePolicy) {
            dataStore.edit { prefs ->
                prefs[SLOT_MODE_POLICY_KEY] = policy.name
            }
        }

        override suspend fun setWeekStartDay(weekStartDay: WeekStartDay) {
            dataStore.edit { prefs ->
                prefs[WEEK_START_DAY_KEY] = weekStartDay.name
            }
        }

        override suspend fun setLastBackupExportedAt(value: String) {
            dataStore.edit { prefs ->
                prefs[LAST_BACKUP_EXPORTED_AT_KEY] = value
            }
        }

        override suspend fun setLastBackupImportedAt(value: String) {
            dataStore.edit { prefs ->
                prefs[LAST_BACKUP_IMPORTED_AT_KEY] = value
            }
        }

        override suspend fun setBackupFolderUri(value: String?) {
            dataStore.edit { prefs ->
                if (value == null) {
                    prefs.remove(BACKUP_FOLDER_URI_KEY)
                } else {
                    prefs[BACKUP_FOLDER_URI_KEY] = value
                }
            }
        }
    }

private fun defaultThemeMode(): ThemeMode {
    return ThemeMode.SYSTEM
}

private fun defaultLanguage(): AppLanguage {
    return SYSTEM
}

private fun defaultSlotModePolicy(): SlotModePolicy {
    return SlotModePolicy.AUTO_WHEN_MULTIPLE
}

private fun defaultWeekStartDay(): WeekStartDay {
    return WeekStartDay.MONDAY
}
