package com.rafaelfelipeac.hermes.features.trophies.presentation

internal fun selectFeaturedTrophy(cards: List<TrophyCardUi>): FeaturedTrophyUi? {
    val recentUnlock =
        cards
            .filter { it.unlockedAt != null }
            .maxByOrNull { it.unlockedAt ?: Long.MIN_VALUE }

    if (recentUnlock != null) {
        return FeaturedTrophyUi(
            trophy = recentUnlock,
            mode = FeaturedTrophyMode.RECENT_UNLOCK,
        )
    }

    val nearest =
        cards
            .filter { !it.isUnlocked }
            .minWithOrNull(
                compareBy<TrophyCardUi>(
                    { (it.target - it.currentValue).coerceAtLeast(0) },
                    { TrophyViewModel.familyOrder.indexOf(it.family) },
                    { it.stableId },
                ),
            )

    return nearest?.let {
        FeaturedTrophyUi(
            trophy = it,
            mode = FeaturedTrophyMode.NEAREST_PROGRESS,
        )
    }
}
