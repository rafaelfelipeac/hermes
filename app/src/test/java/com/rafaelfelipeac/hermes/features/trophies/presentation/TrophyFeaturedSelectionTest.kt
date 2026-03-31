package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrophyFeaturedSelectionTest {
    @Test
    fun selectFeaturedTrophy_prefersMostRecentUnlock() {
        val result =
            selectFeaturedTrophy(
                listOf(
                    card(stableId = "a", unlockedAt = 10L, currentValue = 5, target = 5, isUnlocked = true),
                    card(stableId = "b", unlockedAt = 20L, currentValue = 3, target = 5, isUnlocked = true),
                ),
            )

        assertEquals(FeaturedTrophyMode.RECENT_UNLOCK, result?.mode)
        assertEquals("b", result?.trophy?.stableId)
    }

    @Test
    fun selectFeaturedTrophy_usesNearestProgressWhenNothingIsUnlocked() {
        val result =
            selectFeaturedTrophy(
                listOf(
                    card(stableId = "a", currentValue = 1, target = 5),
                    card(stableId = "b", currentValue = 4, target = 5),
                ),
            )

        assertEquals(FeaturedTrophyMode.NEAREST_PROGRESS, result?.mode)
        assertEquals("b", result?.trophy?.stableId)
    }

    @Test
    fun selectFeaturedTrophy_returnsNullWithoutCandidates() {
        assertNull(selectFeaturedTrophy(emptyList()))
    }

    private fun card(
        stableId: String,
        currentValue: Int = 0,
        target: Int = 1,
        isUnlocked: Boolean = false,
        unlockedAt: Long? = null,
    ): TrophyCardUi {
        return TrophyCardUi(
            stableId = stableId,
            trophyId = TrophyId.FULL_TIME,
            family = TrophyFamilyUi.FOLLOW_THROUGH,
            currentValue = currentValue,
            target = target,
            isUnlocked = isUnlocked,
            unlockedAt = unlockedAt,
        )
    }
}
