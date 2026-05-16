package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeekStartDay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class AddWorkoutDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun typeAndDescriptionAreCapitalizedBeforeSave() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val savedValues = mutableListOf<String?>()

        composeRule.setContent {
            AddWorkoutDialog(
                onDismiss = {},
                onSave = { type, description, _, workoutDate ->
                    savedValues += type
                    savedValues += description
                    savedValues += workoutDate?.toString()
                },
                onManageCategories = { _, _, _, _ -> },
                isEdit = false,
                categories = emptyList(),
                selectedCategoryId = null,
                weekStartDay = WeekStartDay.MONDAY,
            )
        }

        composeRule
            .onNodeWithTag(ADD_WORKOUT_DIALOG_TITLE_FIELD_TAG)
            .performTextInput("tempo run")
        composeRule
            .onNodeWithTag(ADD_WORKOUT_DIALOG_DESCRIPTION_FIELD_TAG)
            .performTextInput("controlled effort")
        composeRule
            .onNodeWithText(context.getString(R.string.workout_dialog_add_workout_confirm))
            .performClick()

        assertEquals(listOf("Tempo run", "Controlled effort", null), savedValues)
    }

    @Test
    fun dateField_isEmptyByDefaultWhenCreatingWorkout() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            AddWorkoutDialog(
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onManageCategories = { _, _, _, _ -> },
                isEdit = false,
                categories = emptyList(),
                selectedCategoryId = null,
                weekStartDay = WeekStartDay.MONDAY,
            )
        }

        composeRule
            .onNodeWithText(context.getString(R.string.race_event_dialog_date))
            .assertIsDisplayed()
    }

    @Test
    fun dateField_canShowProvidedDateWhenCreatingWorkout() {
        val selectedDate = LocalDate.of(2026, 4, 12)
        val expectedDateLabel =
            selectedDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
            )

        composeRule.setContent {
            AddWorkoutDialog(
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onManageCategories = { _, _, _, _ -> },
                isEdit = false,
                categories = emptyList(),
                selectedCategoryId = null,
                weekStartDay = WeekStartDay.MONDAY,
                selectedDate = selectedDate,
            )
        }

        composeRule
            .onNodeWithText(expectedDateLabel)
            .assertIsDisplayed()
    }

    @Test
    fun dateField_isVisibleInEditMode() {
        val selectedDate = LocalDate.of(2026, 4, 12)
        val expectedDateLabel =
            selectedDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
            )
        val savedDate = mutableListOf<LocalDate?>()

        composeRule.setContent {
            AddWorkoutDialog(
                onDismiss = {},
                onSave = { _, _, _, workoutDate ->
                    savedDate += workoutDate
                },
                onManageCategories = { _, _, _, _ -> },
                isEdit = true,
                categories = emptyList(),
                selectedCategoryId = null,
                weekStartDay = WeekStartDay.MONDAY,
                selectedDate = selectedDate,
                initialType = "Run",
                initialDescription = "Easy",
            )
        }

        composeRule
            .onNodeWithText(expectedDateLabel)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.save_changes))
            .performClick()

        assertEquals(listOf(selectedDate), savedDate)
    }

    @Test
    fun categoryPicker_showsCategoriesAndManageAction() {
        val categories =
            listOf(
                CategoryUi(
                    id = 1L,
                    name = "Uncategorized",
                    colorId = "uncategorized",
                    sortOrder = 0,
                    isHidden = false,
                    isSystem = true,
                ),
                CategoryUi(
                    id = 2L,
                    name = "Run",
                    colorId = "run",
                    sortOrder = 1,
                    isHidden = false,
                    isSystem = false,
                ),
            )

        composeRule.setContent {
            AddWorkoutDialog(
                onDismiss = {},
                onSave = { _, _, _, _ -> },
                onManageCategories = { _, _, _, _ -> },
                isEdit = false,
                categories = categories,
                selectedCategoryId = 1L,
                weekStartDay = WeekStartDay.MONDAY,
                initialType = "Run",
                initialDescription = "Easy",
            )
        }

        composeRule.onNodeWithText("Uncategorized").performClick()

        composeRule.onAllNodesWithText("Uncategorized").assertCountEquals(2)
        composeRule.onAllNodesWithText("Run").assertCountEquals(2)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeRule
            .onNodeWithText(context.getString(R.string.workout_dialog_manage_categories))
            .assertIsDisplayed()
    }
}
