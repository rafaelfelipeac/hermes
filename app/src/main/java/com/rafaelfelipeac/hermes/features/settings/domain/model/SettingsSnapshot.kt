package com.rafaelfelipeac.hermes.features.settings.domain.model

data class SettingsSnapshot(
    val themeMode: ThemeMode,
    val language: AppLanguage,
    val slotModePolicy: SlotModePolicy,
    val weekStartDay: WeekStartDay,
    val distanceUnit: DistanceUnit,
    val paceUnit: PaceUnit,
    val weightUnit: WeightUnit,
)
