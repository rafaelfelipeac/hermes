package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyEngine
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrophyCelebrationUi(
    val token: String,
    val message: String,
)

@HiltViewModel
class TrophyCelebrationViewModel
    @Inject
    constructor(
        userActionRepository: UserActionRepository,
        categoryRepository: CategoryRepository,
        private val settingsRepository: SettingsRepository,
        private val stringProvider: StringProvider,
    ) : ViewModel() {
        private val engine = TrophyEngine()
        private val eventsFlow = MutableSharedFlow<TrophyCelebrationUi>(extraBufferCapacity = 1)
        val events: SharedFlow<TrophyCelebrationUi> = eventsFlow.asSharedFlow()

        init {
            val categoriesFlow =
                categoryRepository.observeCategories().mapToContexts()

            viewModelScope.launch {
                combine(
                    userActionRepository.observeActions(),
                    categoriesFlow,
                    settingsRepository.lastSeenTrophyCelebrationToken,
                ) { actions, categories, lastSeenToken ->
                    val progress = engine.compute(actions = actions, categories = categories)
                    val featured =
                        selectFeaturedTrophy(progress.map(::toCardUi))
                            ?.takeIf { it.mode == FeaturedTrophyMode.RECENT_UNLOCK }
                            ?.trophy

                    featured?.takeUnless { celebrationToken(it) == lastSeenToken }
                }.collect { trophy ->
                    if (trophy != null) {
                        eventsFlow.emit(
                            TrophyCelebrationUi(
                                token = celebrationToken(trophy),
                                message =
                                    stringProvider.get(
                                        com.rafaelfelipeac.hermes.R.string.trophies_unlock_banner,
                                        stringProvider.get(trophyNameRes(trophy.trophyId)),
                                    ),
                            ),
                        )
                    }
                }
            }
        }

        fun markCelebrationSeen(token: String) {
            viewModelScope.launch {
                settingsRepository.setLastSeenTrophyCelebrationToken(token)
            }
        }

        private fun toCardUi(progress: com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress): TrophyCardUi {
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
    }

private fun kotlinx.coroutines.flow.Flow<List<Category>>.mapToContexts() =
    map { categories ->
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
