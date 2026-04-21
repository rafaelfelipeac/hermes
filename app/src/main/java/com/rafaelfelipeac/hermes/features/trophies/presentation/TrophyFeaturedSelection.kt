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
                    { it.family.sortIndex() },
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

private fun TrophyFamilyUi.sortIndex(): Int {
    val index = TrophyViewModel.familyOrder.indexOf(this)
    return if (index >= 0) index else TrophyViewModel.familyOrder.size
}
