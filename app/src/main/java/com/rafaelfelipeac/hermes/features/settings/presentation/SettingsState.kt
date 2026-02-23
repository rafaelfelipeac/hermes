package com.rafaelfelipeac.hermes.features.settings.presentation

import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.SlotModePolicy
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val slotModePolicy: SlotModePolicy,
)
