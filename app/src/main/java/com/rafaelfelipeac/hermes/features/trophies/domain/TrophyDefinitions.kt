package com.rafaelfelipeac.hermes.features.trophies.domain

import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyDefinition
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyId

object TrophyDefinitions {
    val supportedV1: List<TrophyDefinition> =
        listOf(
            definition(
                id = TrophyId.FULL_TIME,
                family = TrophyFamily.FOLLOW_THROUGH,
                target = 1,
            ),
            definition(
                id = TrophyId.MATCH_FITNESS,
                family = TrophyFamily.FOLLOW_THROUGH,
                target = 10,
            ),
            definition(
                id = TrophyId.IN_FORM,
                family = TrophyFamily.CONSISTENCY,
                target = 2,
            ),
            definition(
                id = TrophyId.COMEBACK_WEEK,
                family = TrophyFamily.ADAPTABILITY,
                target = 1,
            ),
            definition(
                id = TrophyId.GAME_PLAN,
                family = TrophyFamily.ADAPTABILITY,
                target = 10,
            ),
            definition(
                id = TrophyId.BACK_IN_FORMATION,
                family = TrophyFamily.MOMENTUM,
                target = 1,
            ),
            definition(
                id = TrophyId.HOLD_THE_LINE,
                family = TrophyFamily.MOMENTUM,
                target = 1,
            ),
            definition(
                id = TrophyId.TEAM_SHEET,
                family = TrophyFamily.BUILDER,
                target = 3,
            ),
            definition(
                id = TrophyId.KIT_BAG,
                family = TrophyFamily.BUILDER,
                target = 1,
            ),
        )

    val categoryTemplates: List<TrophyDefinition> =
        listOf(
            definition(
                id = TrophyId.PODIUM_PLACE,
                family = TrophyFamily.CATEGORIES,
                target = 5,
            ),
            definition(
                id = TrophyId.HOME_GROUND,
                family = TrophyFamily.CATEGORIES,
                target = 1,
            ),
            definition(
                id = TrophyId.TRAINING_BLOCK,
                family = TrophyFamily.CATEGORIES,
                target = 5,
            ),
        )

    private fun definition(
        id: TrophyId,
        family: TrophyFamily,
        target: Int,
    ): TrophyDefinition {
        return TrophyDefinition(
            id = id,
            family = family,
            target = target,
        )
    }
}
