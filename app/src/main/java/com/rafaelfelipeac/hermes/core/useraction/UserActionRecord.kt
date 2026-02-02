package com.rafaelfelipeac.hermes.core.useraction

data class UserActionRecord(
    val id: Long,
    val actionType: String,
    val entityType: String,
    val entityId: Long?,
    val metadata: String?,
    val timestamp: Long,
)
