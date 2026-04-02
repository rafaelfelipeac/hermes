package com.rafaelfelipeac.hermes.features.trophies.domain.model

data class TrophyDefinition(
    val id: TrophyId,
    val family: TrophyFamily,
    val metric: TrophyMetric,
    val target: Int,
    val sortOrder: Int,
    val badgeRank: Int = 1,
)
