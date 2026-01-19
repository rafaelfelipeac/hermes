package com.rafaelfelipeac.hermes.core.settings

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    val themeMode: Flow<ThemeMode> = dataStore.data
        .map { prefs ->
            prefs[THEME_MODE_KEY]?.let(ThemeMode::valueOf) ?: defaultThemeMode(context)
        }
        .distinctUntilChanged()

    val language: Flow<AppLanguage> = dataStore.data
        .map { prefs ->
            prefs[LANGUAGE_KEY]?.let(AppLanguage::fromTag) ?: defaultLanguage()
        }
        .distinctUntilChanged()

    fun initialThemeMode(): ThemeMode = defaultThemeMode(context)

    fun initialLanguage(): AppLanguage = defaultLanguage()

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = language.tag
        }
    }
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val tag: String) {
    SYSTEM("system"),
    ENGLISH("en"),
    PORTUGUESE_BRAZIL("pt-BR");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            return entries.firstOrNull { it.tag.equals(tag, ignoreCase = true) } ?: ENGLISH
        }
    }
}

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
private val LANGUAGE_KEY = stringPreferencesKey("language")

private fun defaultThemeMode(context: Context): ThemeMode {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return ThemeMode.LIGHT
    }
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ThemeMode.DARK else ThemeMode.LIGHT
}

private fun defaultLanguage(): AppLanguage {
    return AppLanguage.SYSTEM
}
