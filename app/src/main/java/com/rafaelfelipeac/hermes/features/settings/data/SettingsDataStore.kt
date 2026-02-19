package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

internal const val SETTINGS_DATA_STORE_NAME = "settings"
internal const val THEME_MODE_KEY_NAME = "theme_mode"
internal const val LANGUAGE_KEY_NAME = "language"

internal val Context.settingsDataStore by preferencesDataStore(
    name = SETTINGS_DATA_STORE_NAME,
)

internal val THEME_MODE_KEY = stringPreferencesKey(THEME_MODE_KEY_NAME)
internal val LANGUAGE_KEY = stringPreferencesKey(LANGUAGE_KEY_NAME)
