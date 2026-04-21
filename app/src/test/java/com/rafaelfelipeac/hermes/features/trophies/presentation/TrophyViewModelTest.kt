package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_STRENGTH
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyDefinitions
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrophyViewModelTest {
    @Test
    fun buildTrophyPageState_buildsFamiliesInFixedOrder() {
        val state = buildTrophyPageState(emptyList())

        assertTrue(state.families.isEmpty())
    }

    @Test
    fun buildTrophyPageState_groupsCategoryTrophiesByCategory() {
        val state =
            buildTrophyPageState(
                listOf(
                    progress(
                        TrophyId.PODIUM_PLACE,
                        categoryId = 10L,
                        categoryName = "Run",
                        categoryColorId = COLOR_RUN,
                    ),
                    progress(
                        TrophyId.IN_ROTATION,
                        categoryId = 10L,
                        categoryName = "Run",
                        categoryColorId = COLOR_RUN,
                    ),
                    progress(
                        TrophyId.TRAINING_BLOCK,
                        categoryId = 20L,
                        categoryName = "Strength",
                        categoryColorId = COLOR_STRENGTH,
                    ),
                ),
            )

        val categoriesFamily = state.families.first { it.family == TrophyFamilyUi.CATEGORIES }

        assertEquals(listOf("Run", "Strength"), categoriesFamily.sections.mapNotNull { it.title })
        assertEquals(2, categoriesFamily.sections.size)
        assertEquals(
            listOf(TrophyId.PODIUM_PLACE, TrophyId.IN_ROTATION),
            categoriesFamily.sections.first().trophies.map { it.trophyId },
        )
    }

    @Test
    fun buildTrophyPageState_tracksFamilyCounts() {
        val state =
            buildTrophyPageState(
                listOf(
                    progress(TrophyId.FULL_TIME, currentValue = 1, unlockedAt = 10L),
                    progress(TrophyId.MATCH_FITNESS, currentValue = 9),
                    progress(TrophyId.IN_FORM, currentValue = 1),
                ),
            )

        val followThroughFamily = state.families.first { it.family == TrophyFamilyUi.FOLLOW_THROUGH }
        val consistencyFamily = state.families.first { it.family == TrophyFamilyUi.CONSISTENCY }

        assertEquals(1, followThroughFamily.unlockedCount)
        assertEquals(2, followThroughFamily.totalCount)
        assertEquals(0, consistencyFamily.unlockedCount)
        assertEquals(1, consistencyFamily.totalCount)
        assertEquals(
            listOf(
                TrophyFamilyUi.FOLLOW_THROUGH,
                TrophyFamilyUi.CONSISTENCY,
            ),
            state.families.map { it.family },
        )
    }

    @Test
    fun buildTrophyPageState_mapsSingleUnlockFieldsOnCards() {
        val state =
            buildTrophyPageState(
                listOf(
                    progress(
                        TrophyId.FULL_TIME,
                        currentValue = 5,
                        unlockedAt = 10L,
                    ),
                ),
            )

        val card = state.families.first().sections.first().trophies.first()

        assertEquals(4, card.target)
        assertTrue(card.isUnlocked)
        assertEquals(10L, card.unlockedAt)
    }

    @Test
    fun buildTrophyPageState_keepsLockedCardsVisible() {
        val state =
            buildTrophyPageState(
                listOf(
                    progress(
                        TrophyId.MATCH_FITNESS,
                        currentValue = 4,
                    ),
                ),
            )

        val card = state.families.first().sections.first().trophies.first()

        assertFalse(card.isUnlocked)
        assertEquals(25, card.target)
    }

    private fun progress(
        trophyId: TrophyId,
        currentValue: Int = 0,
        categoryId: Long? = null,
        categoryName: String? = null,
        categoryColorId: String? = null,
        unlockedAt: Long? = null,
    ): TrophyProgress {
        val definition =
            (TrophyDefinitions.supportedV1 + TrophyDefinitions.categoryTemplates)
                .first { it.id == trophyId }

        return TrophyProgress(
            definition = definition,
            currentValue = currentValue,
            unlockedAt = unlockedAt,
            categoryId = categoryId,
            categoryName = categoryName,
            categoryColorId = categoryColorId,
        )
    }
}
