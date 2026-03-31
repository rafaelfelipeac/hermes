package com.rafaelfelipeac.hermes.features.trophies.presentation

data class TrophyPageState(
    val families: List<TrophyFamilySectionUi> = emptyList(),
)

data class TrophyFamilySectionUi(
    val family: TrophyFamilyUi,
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val sections: List<TrophySectionUi> = emptyList(),
)

data class FeaturedTrophyUi(
    val trophy: TrophyCardUi,
    val mode: FeaturedTrophyMode,
)

enum class FeaturedTrophyMode {
    RECENT_UNLOCK,
    NEAREST_PROGRESS,
}

data class TrophySectionUi(
    val stableId: String,
    val title: String? = null,
    val accentColorId: String? = null,
    val trophies: List<TrophyCardUi>,
)

enum class TrophyFamilyUi {
    FOLLOW_THROUGH,
    CONSISTENCY,
    ADAPTABILITY,
    MOMENTUM,
    BUILDER,
    CATEGORIES,
}

data class TrophyCardUi(
    val stableId: String,
    val trophyId: com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId,
    val family: TrophyFamilyUi,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val categoryColorId: String? = null,
    val currentValue: Int,
    val target: Int,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
)
