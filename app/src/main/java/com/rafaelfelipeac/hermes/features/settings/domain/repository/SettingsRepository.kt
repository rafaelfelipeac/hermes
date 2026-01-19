package com.rafaelfelipeac.hermes.features.settings.domain.repository

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val language: Flow<AppLanguage>

    fun initialThemeMode(): ThemeMode
    fun initialLanguage(): AppLanguage

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLanguage(language: AppLanguage)
}
