package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.flow.stateInWhileSubscribed
import com.rafaelfelipeac.hermes.core.strings.LocaleProvider
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.time.CurrentDateProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.features.app.toLocale
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyEngine
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.weekStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class ProgressViewModel
    @Inject
    constructor(
        weeklyTrainingRepository: WeeklyTrainingRepository,
        categoryRepository: CategoryRepository,
        userActionRepository: UserActionRepository,
        settingsRepository: SettingsRepository,
        stringProvider: StringProvider,
        private val localeProvider: LocaleProvider,
        currentDateProvider: CurrentDateProvider,
    ) : ViewModel() {
        private val trophyEngine = TrophyEngine()
        private val categoriesFlow =
            categoryRepository.observeCategories().map { categories ->
                categories.map { it.toUi() }
            }
        private val trophyCategoriesFlow =
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
        private val localeFlow =
            settingsRepository.language.map { language ->
                language.toLocale(localeProvider)
            }
        private val workoutsAndCategories =
            combine(
                weeklyTrainingRepository.observeAllWorkouts(),
                categoriesFlow,
            ) { workouts, categories ->
                workouts to categories
            }
        private val actionsAndTrophies =
            combine(
                userActionRepository.observeActions(),
                trophyCategoriesFlow,
            ) { actions, trophyCategories ->
                actions to trophyCategories
            }
        private val settingsAndLocale =
            combine(
                settingsRepository.weekStartDay,
                localeFlow,
            ) { weekStartDay, locale ->
                weekStartDay to locale
            }

        val state: StateFlow<ProgressState> =
            combine(
                workoutsAndCategories,
                actionsAndTrophies,
                settingsAndLocale,
                currentDateProvider.observeToday(),
            ) { workoutsAndCategories, actionsAndTrophies, settingsAndLocale, today ->
                val (workouts, categories) = workoutsAndCategories
                val (actions, trophyCategories) = actionsAndTrophies
                val (weekStartDay, locale) = settingsAndLocale
                val currentWeekStart = weekStart(today, weekStartDay.dayOfWeek)
                val trophyCards =
                    trophyEngine.compute(
                        actions = actions,
                        categories = trophyCategories,
                    ).map(TrophyProgress::toCardUi)

                buildProgressState(
                    workouts = workouts,
                    categories = categories,
                    trophyCards = trophyCards,
                    recentActivities = buildRecentActivities(actions, locale, stringProvider),
                    today = today,
                    currentWeekStart = currentWeekStart,
                )
            }.stateInWhileSubscribed(
                scope = viewModelScope,
                initialValue = ProgressState(),
            )
    }
