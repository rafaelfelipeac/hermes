package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupUserActionRecord(
    val id: Long,
    val actionType: String,
    val entityType: String,
    val entityId: Long?,
    val metadata: String?,
    val timestamp: Long,
)
