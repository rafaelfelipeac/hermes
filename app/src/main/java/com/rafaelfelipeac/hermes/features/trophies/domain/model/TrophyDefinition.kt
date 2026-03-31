package com.rafaelfelipeac.hermes.features.trophies.domain.model

data class TrophyDefinition(
    val id: TrophyId,
    val family: TrophyFamily,
    val target: Int,
)
