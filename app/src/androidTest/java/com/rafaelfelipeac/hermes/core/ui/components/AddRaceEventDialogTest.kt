package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class AddRaceEventDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun titleAndDescriptionAreCapitalizedBeforeSave() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val savedValues = mutableListOf<String>()
        val eventDate = LocalDate.now().plusDays(5)

        composeRule.setContent {
            AddRaceEventDialog(
                onDismiss = {},
                onSave = { title, description, _, _ ->
                    savedValues += title
                    savedValues += description
                },
                onManageCategories = { _, _, _, _ -> },
                isEdit = false,
                categories =
                    listOf(
                        CategoryUi(
                            id = UNCATEGORIZED_ID,
                            name = "Uncategorized",
                            colorId = "uncategorized",
                            sortOrder = 0,
                            isHidden = false,
                            isSystem = true,
                        ),
                    ),
                selectedCategoryId = UNCATEGORIZED_ID,
                selectedDate = eventDate,
            )
        }

        composeRule.onNodeWithTag(RACE_EVENT_DIALOG_TITLE_FIELD_TAG).performTextInput("marathon")
        composeRule.onNodeWithTag(RACE_EVENT_DIALOG_DESCRIPTION_FIELD_TAG).performTextInput("city race")
        composeRule
            .onNodeWithText(context.getString(R.string.race_event_dialog_add_race_event_confirm))
            .performClick()

        assertEquals(listOf("Marathon", "City race"), savedValues)
    }
}
