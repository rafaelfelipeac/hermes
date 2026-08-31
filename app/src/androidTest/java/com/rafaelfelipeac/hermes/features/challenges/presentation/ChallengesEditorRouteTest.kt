package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeEditorState
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeLifecycle
import com.rafaelfelipeac.hermes.features.challenges.domain.model.ChallengeTargetType
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ChallengesEditorRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun tappingStartDate_opensDatePicker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val startDateLabel = context.getString(R.string.challenges_field_start_date)
        val saveLabel = context.getString(R.string.save_changes)

        composeRule.setContent {
            ChallengesEditorDialog(
                editorState =
                    ChallengeEditorState(
                        title = "August distance",
                        targetType = ChallengeTargetType.DAILY,
                        targetQuantityText = "10",
                        startDate = LocalDate.of(2026, 8, 1),
                        endDate = LocalDate.of(2026, 8, 31),
                        lifecycle = ChallengeLifecycle.ACTIVE,
                    ),
                categories = emptyList(),
                validationMessage = null,
                onTitleChange = {},
                onDescriptionChange = {},
                onTargetTypeChange = {},
                onTargetQuantityChange = {},
                onStartDateChange = {},
                onEndDateChange = {},
                onCategoryChange = {},
                onManageCategories = {},
                onSave = {},
                onCancel = {},
            )
        }

        composeRule.onNodeWithText(startDateLabel).performClick()

        composeRule.onAllNodesWithText(saveLabel).assertCountEquals(2)
    }
}
