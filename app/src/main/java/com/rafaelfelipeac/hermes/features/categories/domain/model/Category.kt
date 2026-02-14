package com.rafaelfelipeac.hermes.features.categories.domain.model

data class Category(
    val id: Long,
    val name: String,
    val colorId: String,
    val sortOrder: Int,
    val isHidden: Boolean,
    val isSystem: Boolean,
)
