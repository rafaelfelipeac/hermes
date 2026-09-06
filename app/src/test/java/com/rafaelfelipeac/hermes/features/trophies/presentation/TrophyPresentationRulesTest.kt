package com.rafaelfelipeac.hermes.features.trophies.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.util.Locale

class TrophyPresentationRulesTest {
    @Test
    fun trophyProgressFraction_clampsAtTarget() {
        assertEquals(0.4f, trophyProgressFraction(currentValue = 2, target = 5), 0f)
        assertEquals(1f, trophyProgressFraction(currentValue = 5, target = 5), 0f)
        assertEquals(1f, trophyProgressFraction(currentValue = 9, target = 5), 0f)
    }

    @Test
    fun trophyCardSemanticsLabel_skipsMissingCategoryName() {
        assertEquals(
            "Full Time. Unlock 1 of 4",
            trophyCardSemanticsLabel(
                name = "Full Time",
                categoryName = null,
                conditionLabel = "Unlock 1 of 4",
            ),
        )
        assertEquals(
            "Full Time. Run. Unlock 1 of 4",
            trophyCardSemanticsLabel(
                name = "Full Time",
                categoryName = "Run",
                conditionLabel = "Unlock 1 of 4",
            ),
        )
    }

    @Test
    fun buildTrophyShareMessage_keepsShareLayoutWithOptionalDate() {
        assertEquals(
            "Title\nDescription\nUnlocked on Sep 5, 2026\n\nCTA",
            buildTrophyShareMessage(
                shareTitle = "Title",
                shareDescription = "Description",
                unlockedDateText = "Unlocked on Sep 5, 2026",
                shareCta = "CTA",
            ),
        )
        assertEquals(
            "Title\nDescription\n\nCTA",
            buildTrophyShareMessage(
                shareTitle = "Title",
                shareDescription = "Description",
                unlockedDateText = null,
                shareCta = "CTA",
            ),
        )
    }

    @Test
    fun resolveAppPackageName_stripsDebugSuffixOnlyWhenNeeded() {
        assertEquals("com.example", resolveAppPackageName("com.example.dev", isDebug = true))
        assertEquals("com.example.dev", resolveAppPackageName("com.example.dev", isDebug = false))
    }

    @Test
    fun unlockedDateFormatting_usesExplicitLocale() {
        val timestamp = Instant.parse("2026-09-05T15:00:00Z").toEpochMilli()

        assertEquals("Sep 5, 2026", formatUnlockedDateLabel(timestamp, Locale.US))
        assertEquals("9/5/26", formatUnlockedDateCompactLabel(timestamp, Locale.US))
    }
}
