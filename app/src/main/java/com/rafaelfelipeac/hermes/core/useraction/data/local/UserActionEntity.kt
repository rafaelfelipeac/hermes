package com.rafaelfelipeac.hermes.core.useraction.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_actions")
data class UserActionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val actionType: String,
    val entityType: String,
    val entityId: Long?,
    val metadata: String?,
    val timestamp: Long,
)
