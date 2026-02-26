package com.rafaelfelipeac.hermes.features.backup.domain.model

data class BackupCategoryRecord(
    val id: Long,
    val name: String,
    val colorId: String,
    val sortOrder: Int,
    val isHidden: Boolean,
    val isSystem: Boolean,
)
