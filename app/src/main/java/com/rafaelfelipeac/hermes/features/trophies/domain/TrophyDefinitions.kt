package com.rafaelfelipeac.hermes.features.trophies.domain

import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyDefinition
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyMetric

object TrophyDefinitions {
    val supportedV1: List<TrophyDefinition> =
        listOf(
            definition(
                id = TrophyId.FULL_TIME,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.COMPLETED_WEEKS,
                target = 3,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.SEASON_BUILDER,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.COMPLETED_WEEKS,
                target = 12,
                sortOrder = 20,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.SEASON_ANCHOR,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.COMPLETED_WEEKS,
                target = 52,
                sortOrder = 30,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.MATCH_FITNESS,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.WORKOUT_COMPLETIONS,
                target = 25,
                sortOrder = 40,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.ENGINE_ROOM,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.WORKOUT_COMPLETIONS,
                target = 100,
                sortOrder = 50,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.WORKHORSE,
                family = TrophyFamily.FOLLOW_THROUGH,
                metric = TrophyMetric.WORKOUT_COMPLETIONS,
                target = 250,
                sortOrder = 60,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.IN_FORM,
                family = TrophyFamily.CONSISTENCY,
                metric = TrophyMetric.CONSECUTIVE_COMPLETED_WEEKS,
                target = 3,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.LOCKED_IN,
                family = TrophyFamily.CONSISTENCY,
                metric = TrophyMetric.CONSECUTIVE_COMPLETED_WEEKS,
                target = 8,
                sortOrder = 20,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.STEADY_RHYTHM,
                family = TrophyFamily.CONSISTENCY,
                metric = TrophyMetric.CONSECUTIVE_COMPLETED_WEEKS,
                target = 16,
                sortOrder = 30,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.COMEBACK_WEEK,
                family = TrophyFamily.ADAPTABILITY,
                metric = TrophyMetric.COMEBACK_WEEKS,
                target = 2,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.GAME_PLAN,
                family = TrophyFamily.ADAPTABILITY,
                metric = TrophyMetric.PLANNING_ADJUSTMENTS,
                target = 50,
                sortOrder = 20,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.TACTICAL_BOARD,
                family = TrophyFamily.ADAPTABILITY,
                metric = TrophyMetric.PLANNING_ADJUSTMENTS,
                target = 200,
                sortOrder = 30,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.FIELD_MARSHAL,
                family = TrophyFamily.ADAPTABILITY,
                metric = TrophyMetric.PLANNING_ADJUSTMENTS,
                target = 500,
                sortOrder = 40,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.BACK_IN_FORMATION,
                family = TrophyFamily.MOMENTUM,
                metric = TrophyMetric.COPIED_WEEKS,
                target = 3,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.HOLD_THE_LINE,
                family = TrophyFamily.MOMENTUM,
                metric = TrophyMetric.COPIED_AND_COMPLETED_WEEKS,
                target = 2,
                sortOrder = 20,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.TEAM_SHEET,
                family = TrophyFamily.BUILDER,
                metric = TrophyMetric.CATEGORY_ACTIONS,
                target = 10,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.KIT_BAG,
                family = TrophyFamily.BUILDER,
                metric = TrophyMetric.BACKUP_SUCCESSES,
                target = 3,
                sortOrder = 20,
                badgeRank = 1,
            ),
        )

    val categoryTemplates: List<TrophyDefinition> =
        listOf(
            definition(
                id = TrophyId.PODIUM_PLACE,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_COMPLETIONS,
                target = 10,
                sortOrder = 10,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.IN_ROTATION,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_COMPLETIONS,
                target = 25,
                sortOrder = 20,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.MAINSTAY,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_COMPLETIONS,
                target = 75,
                sortOrder = 30,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.HOME_GROUND,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_PRESENCE_WEEKS,
                target = 4,
                sortOrder = 40,
                badgeRank = 1,
            ),
            definition(
                id = TrophyId.LOCAL_FAVORITE,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_PRESENCE_WEEKS,
                target = 10,
                sortOrder = 50,
                badgeRank = 2,
            ),
            definition(
                id = TrophyId.TERRITORY,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_PRESENCE_WEEKS,
                target = 20,
                sortOrder = 60,
                badgeRank = 3,
            ),
            definition(
                id = TrophyId.TRAINING_BLOCK,
                family = TrophyFamily.CATEGORIES,
                metric = TrophyMetric.CATEGORY_PLANNING_ACTIONS,
                target = 15,
                sortOrder = 70,
                badgeRank = 1,
            ),
        )

    private fun definition(
        id: TrophyId,
        family: TrophyFamily,
        metric: TrophyMetric,
        target: Int,
        sortOrder: Int,
        badgeRank: Int,
    ): TrophyDefinition {
        return TrophyDefinition(
            id = id,
            family = family,
            metric = metric,
            target = target,
            sortOrder = sortOrder,
            badgeRank = badgeRank,
        )
    }
}
