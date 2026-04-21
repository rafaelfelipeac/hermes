package com.rafaelfelipeac.hermes.features.trophies.domain.model

data class TrophyProgress(
    val definition: TrophyDefinition,
    val currentValue: Int,
    val unlockedAt: Long? = null,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColorId: String? = null,
) {
    val target: Int
        get() = definition.target

    val sortOrder: Int
        get() = definition.sortOrder

    val badgeRank: Int
        get() = definition.badgeRank

    val isUnlocked: Boolean
        get() = unlockedAt != null
}
