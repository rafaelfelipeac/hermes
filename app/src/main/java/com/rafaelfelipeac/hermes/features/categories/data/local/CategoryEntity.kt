package com.rafaelfelipeac.hermes.features.categories.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

private const val CATEGORY_TABLE_NAME = "categories"

@Entity(tableName = CATEGORY_TABLE_NAME)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val colorId: String,
    val sortOrder: Int,
    val isHidden: Boolean,
    val isSystem: Boolean,
)
