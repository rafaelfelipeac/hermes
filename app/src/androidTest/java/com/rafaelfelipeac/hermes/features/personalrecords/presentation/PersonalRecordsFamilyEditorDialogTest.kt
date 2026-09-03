package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import org.junit.Rule
import org.junit.Test

class PersonalRecordsFamilyEditorDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun categoryMenuShowsUncategorizedOnce() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val categoryLabel = context.getString(R.string.personal_records_family_category)
        val categories =
            listOf(
                Category(
                    id = 1L,
                    name = "Run",
                    colorId = "red",
                    sortOrder = 0,
                    isHidden = false,
                    isSystem = false,
                ),
            )

        composeRule.setContent {
            HermesTheme {
                PersonalRecordFamilyEditorDialog(
                    categories = categories,
                    onDismiss = {},
                    onSave = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText(categoryLabel).performClick()
        composeRule.onAllNodesWithText(context.getString(R.string.category_uncategorized)).assertCountEquals(1)
    }
}
