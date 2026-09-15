package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.outlined.AddTask
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.ui.graphics.Color
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyDefinitions
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrophyIconTest {
    @Test
    fun trophyIcons_useExpectedRepresentationsForRepresentativeBlocks() {
        assertEquals(Icons.Outlined.AddTask.name, trophyIcon(TrophyId.CHALLENGE_ACCEPTED).name)
        assertEquals(Icons.Outlined.CalendarMonth.name, trophyIcon(TrophyId.FULL_TIME).name)
        assertEquals(Icons.Outlined.Explore.name, trophyIcon(TrophyId.COMEBACK_WEEK).name)
        assertEquals(Icons.Outlined.SportsScore.name, trophyIcon(TrophyId.RACE_READY).name)
        assertEquals(Icons.AutoMirrored.Outlined.LibraryBooks.name, trophyIcon(TrophyId.FIRST_BENCHMARK).name)
        assertEquals(Icons.Filled.EmojiEvents.name, trophyIcon(TrophyId.PODIUM_PLACE).name)
    }

    @Test
    fun trophyAccentColor_prefersCategoryColorWhenAvailable() {
        val trophy =
            TrophyCardUi(
                stableId = "podium_place_10",
                trophyId = TrophyId.PODIUM_PLACE,
                family = TrophyFamilyUi.CATEGORIES,
                sortOrder = 1,
                badgeRank = 1,
                categoryId = 10L,
                categoryName = "Run",
                categoryColorId = COLOR_RUN,
                currentValue = 1,
                target = 5,
                isUnlocked = false,
                unlockedAt = null,
            )

        assertEquals(categoryAccentColor(COLOR_RUN), trophyAccentColor(trophy))
    }

    @Test
    fun trophyAccentColor_usesFamilyFallbackWithoutCategoryColor() {
        val trophy =
            TrophyCardUi(
                stableId = "full_time",
                trophyId = TrophyId.FULL_TIME,
                family = TrophyFamilyUi.FOLLOW_THROUGH,
                sortOrder = 1,
                badgeRank = 1,
                currentValue = 1,
                target = 5,
                isUnlocked = false,
                unlockedAt = null,
            )

        assertEquals(Color(0xFF4277B8), trophyAccentColor(trophy))
        assertTrue(trophyAccentColor(trophy) != categoryAccentColor(COLOR_RUN))
    }

    @Test
    fun trophyLevels_reuseOneIconWithinEachBlock() {
        val definitions = TrophyDefinitions.supportedV1 + TrophyDefinitions.categoryTemplates
        val iconsByBlock =
            definitions
                .groupBy { it.family to it.metric }
                .mapValues { (_, matches) -> matches.map { trophyIcon(it.id).name }.toSet() }
        val blocksWithMultipleIcons = iconsByBlock.filterValues { it.size > 1 }

        assertTrue(
            "Multiple icons used within trophy blocks: $blocksWithMultipleIcons",
            blocksWithMultipleIcons.isEmpty(),
        )
    }

    @Test
    fun trophyIcons_areNotReusedAcrossBlocks() {
        val definitions = TrophyDefinitions.supportedV1 + TrophyDefinitions.categoryTemplates
        val blocksByIcon =
            definitions
                .groupBy { trophyIcon(it.id).name }
                .mapValues { (_, matches) -> matches.map { it.family to it.metric }.toSet() }
        val reusedIcons = blocksByIcon.filterValues { it.size > 1 }

        assertTrue("Icons reused across trophy blocks: $reusedIcons", reusedIcons.isEmpty())
    }

    @Test
    fun trophyBlocks_haveAtMostThreeLevels() {
        val definitions = TrophyDefinitions.supportedV1 + TrophyDefinitions.categoryTemplates
        val oversizedBlocks =
            definitions
                .groupBy { it.family to it.metric }
                .filterValues { it.size > MAX_LEVELS_PER_BLOCK }

        assertTrue("Trophy blocks with more than three levels: $oversizedBlocks", oversizedBlocks.isEmpty())
    }

    private companion object {
        const val MAX_LEVELS_PER_BLOCK = 3
    }
}
