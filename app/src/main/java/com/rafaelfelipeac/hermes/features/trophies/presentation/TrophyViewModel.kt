package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.TROPHY_ID
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.TROPHY_NAME
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyEngine
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrophyViewModel
    @Inject
    constructor(
        userActionRepository: UserActionRepository,
        categoryRepository: CategoryRepository,
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        private val engine = TrophyEngine()
        private val categoriesFlow =
            categoryRepository.observeCategories().map { categories ->
                categories
                    .filter { !it.isHidden }
                    .sortedBy { it.sortOrder }
                    .map { category ->
                        TrophyCategoryContext(
                            id = category.id,
                            name = category.name,
                            colorId = category.colorId,
                        )
                    }
            }

        val state: StateFlow<TrophyPageState> =
            combine(userActionRepository.observeActions(), categoriesFlow) { actions, categories ->
                buildTrophyPageState(
                    progress =
                        engine.compute(
                            actions = actions,
                            categories = categories,
                        ),
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = TrophyPageState(),
            )

        fun logShareTrophy(
            trophy: TrophyCardUi,
            trophyName: String,
        ) {
            viewModelScope.launch {
                userActionLogger.log(
                    actionType = UserActionType.SHARE_TROPHY,
                    entityType = UserActionEntityType.TROPHY,
                    metadata =
                        buildMap {
                            put(TROPHY_ID, trophy.trophyId.name)
                            put(TROPHY_NAME, trophyName)
                            trophy.categoryId?.let { put(CATEGORY_ID, it.toString()) }
                            trophy.categoryName?.let { put(CATEGORY_NAME, it) }
                        },
                )
            }
        }

        companion object {
            private const val STATE_SHARING_TIMEOUT_MS = 5_000L

            internal val familyOrder =
                listOf(
                    TrophyFamilyUi.FOLLOW_THROUGH,
                    TrophyFamilyUi.CONSISTENCY,
                    TrophyFamilyUi.ADAPTABILITY,
                    TrophyFamilyUi.MOMENTUM,
                    TrophyFamilyUi.BUILDER,
                    TrophyFamilyUi.CATEGORIES,
                )
        }
    }

internal fun buildTrophyPageState(progress: List<TrophyProgress>): TrophyPageState {
    val cards = progress.map(::toCardUi)
    val families =
        TrophyViewModel.familyOrder.map { family ->
            val familyCards = cards.filter { it.family == family }
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
                                        stableId = categoryCards.first().stableId.substringAfter('_'),
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
