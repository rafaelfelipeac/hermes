package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyDefinitions
import org.junit.Assert.assertTrue
import org.junit.Test

class TrophyIconTest {
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
