package com.rafaelfelipeac.hermes.features.activity.presentation

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.strings.StringProvider
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionRepository
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_TYPE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.NEW_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_CATEGORY_NAME
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_DAY_OF_WEEK
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.OLD_VALUE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.WEEK_START_DATE
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataSerializer
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataValues
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.SETTINGS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionRecord
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CHANGE_LANGUAGE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COMPLETE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.COPY_LAST_WEEK
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.CREATE_WORKOUT
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.MOVE_WORKOUT_BETWEEN_DAYS
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.UPDATE_WORKOUT
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.Locale.ENGLISH

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun moveWorkout_formatsWeekAndDayNamesWithNewLine() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val weekStart = LocalDate.of(2026, 2, 2)
            val metadata =
                metadataJson(
                    WEEK_START_DATE to weekStart.toString(),
                    OLD_DAY_OF_WEEK to "1",
                    NEW_DAY_OF_WEEK to "2",
                    NEW_TYPE to "Bike",
                )

            viewModel.updateLocale(ENGLISH)

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 1L,
                        actionType = MOVE_WORKOUT_BETWEEN_DAYS.name,
                        entityType = WORKOUT.name,
                        entityId = 10L,
                        metadata = metadata,
                        timestamp = weekStart.atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
                    ),
                ),
            )

            viewModel.state.test {
                val state = awaitNonEmptyState()
                val subtitle = state.sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("Week of") == true)
                assertTrue(subtitle?.contains("Monday") == true)
                assertTrue(subtitle?.contains("Tuesday") == true)
                assertTrue(subtitle?.contains("\n") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun completeWorkout_usesWorkoutTypeAndQuotes() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val metadata =
                metadataJson(
                    NEW_TYPE to "Bike",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 2L,
                        actionType = COMPLETE_WORKOUT.name,
                        entityType = WORKOUT.name,
                        entityId = 42L,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val title = awaitNonEmptyState().sections.first().items.first().title

                assertTrue(title.contains("\"Bike\""))

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun changeLanguage_formatsSystemAndLanguageNames() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val metadata =
                metadataJson(
                    OLD_VALUE to "system",
                    NEW_VALUE to "en",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 3L,
                        actionType = CHANGE_LANGUAGE.name,
                        entityType = SETTINGS.name,
                        entityId = null,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val subtitle = awaitNonEmptyState().sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("\"System\"") == true)
                assertTrue(subtitle?.contains("\"English\"") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun unplannedDay_usesToBeDefinedLabel() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val metadata =
                metadataJson(
                    OLD_DAY_OF_WEEK to UserActionMetadataValues.UNPLANNED,
                    NEW_DAY_OF_WEEK to "1",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 4L,
                        actionType = MOVE_WORKOUT_BETWEEN_DAYS.name,
                        entityType = WORKOUT.name,
                        entityId = 99L,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val subtitle = awaitNonEmptyState().sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("To be defined") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun copyLastWeek_usesWeekActionTitleAndWeekSubtitle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val weekStart = LocalDate.of(2026, 2, 2)
            val metadata = metadataJson(WEEK_START_DATE to weekStart.toString())

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 5L,
                        actionType = COPY_LAST_WEEK.name,
                        entityType = WEEK.name,
                        entityId = null,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val item = awaitNonEmptyState().sections.first().items.first()

                assertTrue(item.title.contains("copied last week"))
                assertTrue(item.subtitle?.contains("Week of") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun createWorkout_includesCategorySubtitle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val weekStart = LocalDate.of(2026, 2, 2)
            val metadata =
                metadataJson(
                    WEEK_START_DATE to weekStart.toString(),
                    CATEGORY_NAME to "Strength",
                    NEW_TYPE to "Bike",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 6L,
                        actionType = CREATE_WORKOUT.name,
                        entityType = WORKOUT.name,
                        entityId = 88L,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val subtitle = awaitNonEmptyState().sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("Category") == true)
                assertTrue(subtitle?.contains("Strength") == true)
                assertTrue(subtitle?.contains("\n") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateWorkoutCategory_includesCategoryChangeSubtitle() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val metadata =
                metadataJson(
                    OLD_CATEGORY_NAME to "Strength",
                    NEW_CATEGORY_NAME to "Cardio",
                    NEW_TYPE to "Bike",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 7L,
                        actionType = UPDATE_WORKOUT.name,
                        entityType = WORKOUT.name,
                        entityId = 77L,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val subtitle = awaitNonEmptyState().sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("Category") == true)
                assertTrue(subtitle?.contains("Strength") == true)
                assertTrue(subtitle?.contains("Cardio") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateWorkoutCategory_withWeekSubtitleSplitsLines() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserActionRepository()
            val viewModel = ActivityViewModel(repository, FakeStringProvider())
            val weekStart = LocalDate.of(2026, 2, 2)
            val metadata =
                metadataJson(
                    WEEK_START_DATE to weekStart.toString(),
                    OLD_CATEGORY_NAME to "Strength",
                    NEW_CATEGORY_NAME to "Cardio",
                    NEW_TYPE to "Bike",
                )

            repository.emit(
                listOf(
                    UserActionRecord(
                        id = 8L,
                        actionType = UPDATE_WORKOUT.name,
                        entityType = WORKOUT.name,
                        entityId = 21L,
                        metadata = metadata,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            )

            viewModel.state.test {
                val subtitle = awaitNonEmptyState().sections.first().items.first().subtitle

                assertTrue(subtitle?.contains("Week of") == true)
                assertTrue(subtitle?.contains("Category") == true)
                assertTrue(subtitle?.contains("\n") == true)

                cancelAndIgnoreRemainingEvents()
            }
        }

    private class FakeUserActionRepository : UserActionRepository {
        private val flow = MutableStateFlow<List<UserActionRecord>>(emptyList())

        override fun observeActions() = flow

        fun emit(records: List<UserActionRecord>) {
            flow.value = records
        }
    }

    private class FakeStringProvider : StringProvider {
        private val values =
            mapOf(
                R.string.activity_action_create_workout to "You created the workout %1\$s.",
                R.string.activity_action_update_workout to "You updated the workout %1\$s.",
                R.string.activity_action_delete_workout to "You deleted the workout %1\$s.",
                R.string.activity_action_complete_workout to "You completed the workout %1\$s.",
                R.string.activity_action_complete_rest_day to "You completed a rest day.",
                R.string.activity_action_incomplete_workout to "You marked the workout %1\$s as incomplete.",
                R.string.activity_action_incomplete_rest_day to "You marked a rest day as incomplete.",
                R.string.activity_action_reorder_workout to "You reordered the workout %1\$s.",
                R.string.activity_action_move_workout to "You moved the workout %1\$s.",
                R.string.activity_action_reorder_rest_day to "You reordered a rest day.",
                R.string.activity_action_move_rest_day to "You moved a rest day.",
                R.string.activity_action_create_rest_day to "You created a rest day.",
                R.string.activity_action_update_rest_day to "You updated a rest day.",
                R.string.activity_action_delete_rest_day to "You deleted a rest day.",
                R.string.activity_action_convert_workout_to_rest_day to
                    "You converted the workout %1\$s to a rest day.",
                R.string.activity_action_convert_rest_day_to_workout to "You converted a rest day to a workout.",
                R.string.activity_action_change_language to "You changed the language.",
                R.string.activity_action_change_theme to "You changed the theme.",
                R.string.activity_action_open_week to "You opened another week.",
                R.string.activity_action_copy_last_week to "You copied last week into this week.",
                R.string.activity_action_undo_copy_last_week to "You undid copying last week into this week.",
                R.string.activity_action_fallback to "You performed an action in the app.",
                R.string.activity_subtitle_change_value to "From \"%1\$s\" to \"%2\$s\".",
                R.string.activity_subtitle_move to "From \"%1\$s\" to \"%2\$s\".",
                R.string.activity_subtitle_workout_category to "Category %1\$s.",
                R.string.activity_subtitle_workout_category_change to "Category from %1\$s to %2\$s.",
                R.string.activity_subtitle_week to "Week of %1\$s.",
                R.string.activity_subtitle_separator to " • ",
                R.string.activity_time_pattern to "HH:mm",
                R.string.activity_week_date_pattern to "MMM d, yyyy",
                R.string.activity_value_system to "System",
                R.string.activity_value_unknown to "Unknown",
                R.string.activity_value_quoted to "\"%1\$s\"",
                R.string.activity_workout_fallback to "untitled",
                R.string.activity_day_unplanned to "To be defined",
                R.string.settings_language_english to "English",
                R.string.settings_language_portuguese_brazil to "Portuguese (Brazil)",
                R.string.settings_language_german to "German",
                R.string.settings_language_french to "French",
                R.string.settings_language_spanish to "Spanish",
                R.string.settings_language_italian to "Italian",
                R.string.settings_language_arabic to "Arabic",
                R.string.settings_language_hindi to "Hindi",
                R.string.settings_language_japanese to "Japanese",
                R.string.settings_theme_light to "Light",
                R.string.settings_theme_dark to "Dark",
                R.string.day_monday to "Monday",
                R.string.day_tuesday to "Tuesday",
                R.string.day_wednesday to "Wednesday",
                R.string.day_thursday to "Thursday",
                R.string.day_friday to "Friday",
                R.string.day_saturday to "Saturday",
                R.string.day_sunday to "Sunday",
            )

        override fun get(
            id: Int,
            vararg args: Any,
        ): String {
            val template = values[id] ?: "missing:$id"
            return String.format(ENGLISH, template, *args)
        }

        override fun getForLanguage(
            languageTag: String?,
            id: Int,
            vararg args: Any,
        ): String {
            return get(id, *args)
        }
    }

    private suspend fun ReceiveTurbine<ActivityState>.awaitNonEmptyState(): ActivityState {
        while (true) {
            val state = awaitItem()
            if (state.sections.isNotEmpty()) return state
        }
    }

    private fun metadataJson(vararg pairs: Pair<String, String>): String {
        return UserActionMetadataSerializer.toJson(pairs.toMap())
    }
}
