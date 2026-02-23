package com.rafaelfelipeac.hermes.features.settings.data

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.datastore.preferences.core.edit
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
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
                    prefs[THEME_MODE_KEY]?.let(ThemeMode::valueOf) ?: defaultThemeMode(context)
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
                    prefs[SLOT_MODE_POLICY_KEY]?.let(SlotModePolicy::valueOf)
                        ?: defaultSlotModePolicy()
                }
                .distinctUntilChanged()

        override fun initialThemeMode(): ThemeMode = defaultThemeMode(context)

        override fun initialLanguage(): AppLanguage = defaultLanguage()

        override fun initialSlotModePolicy(): SlotModePolicy = defaultSlotModePolicy()

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
    }

private fun defaultThemeMode(context: Context): ThemeMode {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return ThemeMode.LIGHT
    }

    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

    return if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ThemeMode.DARK else ThemeMode.LIGHT
}

private fun defaultLanguage(): AppLanguage {
    return SYSTEM
}

private fun defaultSlotModePolicy(): SlotModePolicy {
    return SlotModePolicy.AUTO_WHEN_MULTIPLE
}
