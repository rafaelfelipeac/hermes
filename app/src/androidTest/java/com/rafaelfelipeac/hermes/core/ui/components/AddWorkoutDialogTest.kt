package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import org.junit.Rule
import org.junit.Test

class AddWorkoutDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

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
                onSave = { _, _, _ -> },
                onManageCategories = { _, _, _ -> },
                isEdit = false,
                categories = categories,
                selectedCategoryId = 1L,
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
