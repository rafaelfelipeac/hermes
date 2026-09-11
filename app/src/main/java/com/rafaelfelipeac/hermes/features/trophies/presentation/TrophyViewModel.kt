package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.flow.stateInWhileSubscribed
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
            }.flowOn(Dispatchers.Default)
                .stateInWhileSubscribed(
                    scope = viewModelScope,
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
    }
