package com.rafaelfelipeac.hermes.features.trophies.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class TrophyBadgeTest {
    @Test
    fun badgeTierStyle_usesPatchForRanksBelowTwo() {
        assertPatchStyle(0)
        assertPatchStyle(1)
    }

    @Test
    fun badgeTierStyle_usesMedalForRankTwo() {
        val style = 2.toBadgeTierStyle()

        assertEquals(0.82f, style.badgeScale, 0f)
        assertEquals(0.0f, style.innerRingAlpha, 0f)
        assertEquals(1.0f, style.iconScale, 0f)
    }

    @Test
    fun badgeTierStyle_usesCrestForRanksThreeAndAbove() {
        val style = 3.toBadgeTierStyle()
        val higherStyle = 5.toBadgeTierStyle()

        assertEquals(0.94f, style.badgeScale, 0f)
        assertEquals(0.94f, higherStyle.badgeScale, 0f)
        assertEquals(0.46f, style.middleRingAlpha, 0f)
        assertEquals(0.46f, style.innerRingAlpha, 0f)
        assertEquals(1.08f, style.iconScale, 0f)
    }

    private fun assertPatchStyle(rank: Int) {
        val style = rank.toBadgeTierStyle()

        assertEquals(0.72f, style.badgeScale, 0f)
        assertEquals(0.0f, style.middleRingAlpha, 0f)
        assertEquals(0.0f, style.innerRingAlpha, 0f)
        assertEquals(0.96f, style.iconScale, 0f)
    }
}
