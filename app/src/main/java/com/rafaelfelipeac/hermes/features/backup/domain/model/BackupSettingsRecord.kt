package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupSettingsRecord(
    val themeMode: String,
    val languageTag: String,
    val slotModePolicy: String,
    val weekStartDay: String,
    val distanceUnit: String,
    val paceUnit: String,
    val weightUnit: String,
)
