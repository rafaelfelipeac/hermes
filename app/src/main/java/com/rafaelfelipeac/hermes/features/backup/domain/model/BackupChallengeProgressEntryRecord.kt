package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupChallengeProgressEntryRecord(
    val id: Long,
    val challengeId: Long,
    val quantity: Long,
    val entryDate: String,
    val occurredAt: String,
    val createdAt: String,
    val updatedAt: String,
)
