package com.rafaelfelipeac.hermes.features.trophies.presentation

import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress

internal val trophyFamilyOrder =
    listOf(
        TrophyFamilyUi.FOLLOW_THROUGH,
        TrophyFamilyUi.CONSISTENCY,
        TrophyFamilyUi.ADAPTABILITY,
        TrophyFamilyUi.MOMENTUM,
        TrophyFamilyUi.BUILDER,
        TrophyFamilyUi.RACE_EVENTS,
        TrophyFamilyUi.CHALLENGES,
        TrophyFamilyUi.PERSONAL_RECORDS,
        TrophyFamilyUi.CATEGORIES,
    )

internal fun buildTrophyPageState(progress: List<TrophyProgress>): TrophyPageState {
    val cards = progress.map(::toCardUi)
    val families =
        trophyFamilyOrder.mapNotNull { family ->
            val familyCards = cards.filter { it.family == family }
            if (familyCards.isEmpty()) return@mapNotNull null

            TrophyFamilySectionUi(
                family = family,
                unlockedCount = familyCards.count { it.isUnlocked },
                totalCount = familyCards.size,
                sections =
                    if (family == TrophyFamilyUi.CATEGORIES) {
                        familyCards
                            .groupBy { it.categoryId }
                            .values
                            .sortedBy { it.firstOrNull()?.categoryName.orEmpty() }
                            .mapNotNull { categoryCards ->
                                categoryCards.firstOrNull()?.categoryName?.let { categoryName ->
                                    TrophySectionUi(
                                        stableId = categoryCards.first().stableId.substringAfterLast('_'),
                                        title = categoryName,
                                        accentColorId = categoryCards.first().categoryColorId,
                                        trophies = categoryCards.sortedBy(TrophyCardUi::sortOrder),
                                    )
                                }
                            }
                    } else {
                        familyCards.sortedBy(TrophyCardUi::sortOrder).takeIf { it.isNotEmpty() }?.let { list ->
                            listOf(
                                TrophySectionUi(
                                    stableId = family.name,
                                    trophies = list,
                                ),
                            )
                        }.orEmpty()
                    },
            )
        }

    return TrophyPageState(families = families)
}

private fun toCardUi(progress: TrophyProgress): TrophyCardUi {
    return TrophyCardUi(
        stableId =
            buildString {
                append(progress.definition.id.name)
                progress.categoryId?.let {
                    append('_')
                    append(it)
                }
            },
        trophyId = progress.definition.id,
        family = progress.definition.family.toUi(),
        sortOrder = progress.sortOrder,
        badgeRank = progress.badgeRank,
        categoryId = progress.categoryId,
        categoryName = progress.categoryName,
        categoryColorId = progress.categoryColorId,
        currentValue = progress.currentValue,
        target = progress.target,
        isUnlocked = progress.isUnlocked,
        unlockedAt = progress.unlockedAt,
    )
}
