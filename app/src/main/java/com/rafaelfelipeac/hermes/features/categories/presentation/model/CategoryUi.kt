package com.rafaelfelipeac.hermes.features.categories.presentation.model

data class CategoryUi(
    val id: Long,
    val name: String,
    val colorId: String,
    val sortOrder: Int,
    val isHidden: Boolean,
    val isSystem: Boolean,
)
