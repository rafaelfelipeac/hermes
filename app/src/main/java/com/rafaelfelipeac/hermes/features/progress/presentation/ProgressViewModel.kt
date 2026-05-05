package com.rafaelfelipeac.hermes.features.progress.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.features.activity.presentation.formatter.ActivityUiFormatter
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.categories.domain.repository.CategoryRepository
import com.rafaelfelipeac.hermes.features.categories.presentation.toUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.repository.SettingsRepository
import com.rafaelfelipeac.hermes.features.trophies.domain.TrophyEngine
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyCategoryContext
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyFamily
import com.rafaelfelipeac.hermes.features.trophies.domain.model.TrophyProgress
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCardUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyFamilyUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.Workout
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.repository.WeeklyTrainingRepository
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.weekStart
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel
    @Inject
    constructor(
        weeklyTrainingRepository: WeeklyTrainingRepository,
        categoryRepository: CategoryRepository,
        userActionRepository: UserActionRepository,
        settingsRepository: SettingsRepository,
        stringProvider: StringProvider,
    ) : ViewModel() {
        private val trophyEngine = TrophyEngine()
        private val activityFormatter = ActivityUiFormatter(stringProvider)
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
                language.toLocale()
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
            ) { workoutsAndCategories, actionsAndTrophies, settingsAndLocale ->
                val (workouts, categories) = workoutsAndCategories
                val (actions, trophyCategories) = actionsAndTrophies
                val (weekStartDay, locale) = settingsAndLocale
                val today = LocalDate.now()
                val currentWeekStart = weekStart(today, weekStartDay.dayOfWeek)
                val trophyCards =
                    trophyEngine.compute(
                        actions = actions,
                        categories = trophyCategories,
                    ).map(::toCardUi)

                buildProgressState(
                    workouts = workouts,
                    categories = categories,
                    trophyCards = trophyCards,
                    recentActivities = buildRecentActivities(actions, locale),
                    today = today,
                    currentWeekStart = currentWeekStart,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = ProgressState(),
            )

        private fun buildRecentActivities(
            actions: List<UserActionRecord>,
            locale: Locale,
        ): List<ActivityItemUi> {
            val zoneId = ZoneId.systemDefault()

            return actions
                .sortedByDescending { it.timestamp }
                .take(RECENT_ACTIVITY_LIMIT)
                .map { record ->
                    val metadata = activityFormatter.parseMetadata(record.metadata)
                    ActivityItemUi(
                        id = record.id,
                        title = activityFormatter.buildTitle(record, metadata),
                        subtitle = activityFormatter.buildSubtitle(record, metadata, locale),
                        time = activityFormatter.formatTime(record.timestamp, zoneId, locale),
                    )
                }
        }

        private fun toCardUi(progress: TrophyProgress): TrophyCardUi {
            return TrophyCardUi(
                stableId = buildTrophyStableId(progress),
                trophyId = progress.definition.id,
                family = progress.definition.family.toUi(),
                sortOrder = progress.sortOrder,
                badgeRank = progress.badgeRank,
                categoryId = progress.categoryId,
                categoryName = progress.categoryName,
                categoryColorId = progress.categoryColorId,
                currentValue = progress.currentValue,
                target = progress.definition.target,
                isUnlocked = progress.isUnlocked,
                unlockedAt = progress.unlockedAt,
            )
        }

        private fun buildTrophyStableId(progress: TrophyProgress): String {
            return buildString {
                append(progress.definition.id.name)
                progress.categoryId?.let {
                    append('_')
                    append(it)
                }
            }
        }

        private fun TrophyFamily.toUi(): TrophyFamilyUi {
            return when (this) {
                TrophyFamily.FOLLOW_THROUGH -> TrophyFamilyUi.FOLLOW_THROUGH
                TrophyFamily.CONSISTENCY -> TrophyFamilyUi.CONSISTENCY
                TrophyFamily.ADAPTABILITY -> TrophyFamilyUi.ADAPTABILITY
                TrophyFamily.MOMENTUM -> TrophyFamilyUi.MOMENTUM
                TrophyFamily.BUILDER -> TrophyFamilyUi.BUILDER
                TrophyFamily.RACE_EVENTS -> TrophyFamilyUi.RACE_EVENTS
                TrophyFamily.CATEGORIES -> TrophyFamilyUi.CATEGORIES
            }
        }

        private fun AppLanguage.toLocale(): Locale {
            return if (this == AppLanguage.SYSTEM) {
                Locale.getDefault()
            } else {
                Locale.forLanguageTag(tag)
            }
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
            const val RECENT_ACTIVITY_LIMIT = 5
        }
    }
