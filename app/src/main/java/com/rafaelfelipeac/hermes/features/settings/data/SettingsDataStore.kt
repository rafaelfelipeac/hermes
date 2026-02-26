package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal const val SETTINGS_DATA_STORE_NAME = "settings"
internal const val THEME_MODE_KEY_NAME = "theme_mode"
internal const val LANGUAGE_KEY_NAME = "language"
internal const val SLOT_MODE_POLICY_KEY_NAME = "slot_mode_policy"
internal const val LAST_BACKUP_EXPORTED_AT_KEY_NAME = "last_backup_exported_at"
internal const val LAST_BACKUP_IMPORTED_AT_KEY_NAME = "last_backup_imported_at"

internal val Context.settingsDataStore by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME,
)

internal val THEME_MODE_KEY = stringPreferencesKey(THEME_MODE_KEY_NAME)
internal val LANGUAGE_KEY = stringPreferencesKey(LANGUAGE_KEY_NAME)
internal val SLOT_MODE_POLICY_KEY = stringPreferencesKey(SLOT_MODE_POLICY_KEY_NAME)
internal val LAST_BACKUP_EXPORTED_AT_KEY = stringPreferencesKey(LAST_BACKUP_EXPORTED_AT_KEY_NAME)
internal val LAST_BACKUP_IMPORTED_AT_KEY = stringPreferencesKey(LAST_BACKUP_IMPORTED_AT_KEY_NAME)
