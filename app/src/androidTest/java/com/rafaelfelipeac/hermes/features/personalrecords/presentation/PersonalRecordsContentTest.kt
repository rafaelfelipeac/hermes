package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.MANUAL
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class PersonalRecordsContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shelfHidesEmptyCategoriesAndOpensFamilyDetail() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var backPressed = false
        val runCategory =
            Category(
                id = 1L,
                name = "Run",
                colorId = "run",
                sortOrder = 0,
                isHidden = false,
                isSystem = true,
            )
        val cyclingCategory =
            Category(
                id = 2L,
                name = "Cycling",
                colorId = "cycling",
                sortOrder = 1,
                isHidden = false,
                isSystem = true,
            )
        val family =
            PersonalRecordFamily(
                id = 10L,
                categoryId = runCategory.id,
                title = "5K",
                metricType = DISTANCE,
                defaultUnit = KILOMETER,
                comparisonRule = HIGHER_IS_BETTER,
                manualCurrentEntryId = null,
                sortOrder = 0,
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
            )
        val entries =
            listOf(
                personalRecordEntry(
                    id = 100L,
                    familyId = family.id,
                    value = 5.0,
                    recordDate = LocalDate.parse("2024-01-01"),
                ),
                personalRecordEntry(
                    id = 101L,
                    familyId = family.id,
                    value = 10.0,
                    recordDate = LocalDate.parse("2024-02-01"),
                ),
            )

        composeRule.setContent {
            var selectedFamilyId by remember { mutableStateOf<Long?>(null) }
            PersonalRecordsContent(
                state =
                    PersonalRecordsState(
                        categories = listOf(runCategory, cyclingCategory),
                        families = listOf(family),
                        entries = entries,
                    ),
                selectedFamilyId = selectedFamilyId,
                onFamilySelected = { selectedFamilyId = it },
                onBack = { backPressed = true },
                onBackToShelf = { selectedFamilyId = null },
                onCreateFamily = {},
                onAddResult = {},
                onEditFamily = {},
                onDeleteFamily = {},
                onEditEntry = {},
                onSetManualCurrentEntry = { _, _ -> },
            )
        }

        composeRule.onNodeWithTag(PERSONAL_RECORDS_BACK_BUTTON_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(PERSONAL_RECORDS_BACK_BUTTON_TAG).performClick()
        composeRule.runOnIdle {
            assertTrue(backPressed)
        }
        composeRule.onNodeWithTag(PERSONAL_RECORDS_ACTION_FAB_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(PERSONAL_RECORDS_ACTION_FAB_TAG).performClick()
        composeRule.onNodeWithTag(PERSONAL_RECORDS_ACTION_MENU_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.personal_records_new_family)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.personal_records_add_result)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.personal_records_summary_title)).assertCountEquals(0)
        composeRule.onNodeWithText("Run").assertIsDisplayed()
        composeRule.onAllNodesWithText("Cycling").assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.category_uncategorized)).assertCountEquals(0)
        composeRule.onNodeWithTag(PERSONAL_RECORDS_ACTION_FAB_TAG).performClick()

        composeRule.onNodeWithTag(PERSONAL_RECORDS_FAMILY_CARD_TAG_PREFIX + family.id).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(PERSONAL_RECORDS_ACTION_FAB_TAG).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.getString(R.string.personal_records_add_result)).assertCountEquals(0)
    }

    @Test
    fun manualSeries_allowsSelectingCurrentEntry() {
        val family =
            PersonalRecordFamily(
                id = 10L,
                categoryId = null,
                title = "Push-ups",
                metricType = DISTANCE,
                defaultUnit = KILOMETER,
                comparisonRule = MANUAL,
                manualCurrentEntryId = null,
                sortOrder = 0,
                createdAt = Instant.parse("2024-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
            )
        val olderEntry =
            personalRecordEntry(
                id = 100L,
                familyId = family.id,
                value = 5.0,
                recordDate = LocalDate.parse("2024-01-01"),
            )
        val newerEntry =
            personalRecordEntry(
                id = 101L,
                familyId = family.id,
                value = 10.0,
                recordDate = LocalDate.parse("2024-02-01"),
            )
        var selectedEntryId: Long? = null

        composeRule.setContent {
            PersonalRecordsContent(
                state =
                    PersonalRecordsState(
                        families = listOf(family),
                        entries = listOf(olderEntry, newerEntry),
                    ),
                selectedFamilyId = family.id,
                onFamilySelected = {},
                onBack = {},
                onBackToShelf = {},
                onCreateFamily = {},
                onAddResult = {},
                onEditFamily = {},
                onDeleteFamily = {},
                onEditEntry = {},
                onSetManualCurrentEntry = { _, entryId -> selectedEntryId = entryId },
            )
        }

        composeRule.onNodeWithTag(PERSONAL_RECORDS_SET_CURRENT_TAG_PREFIX + olderEntry.id).performClick()

        composeRule.runOnIdle {
            assertEquals(olderEntry.id, selectedEntryId)
        }
    }

    private fun personalRecordEntry(
        id: Long,
        familyId: Long,
        value: Double,
        recordDate: LocalDate,
    ) = PersonalRecordEntry(
        id = id,
        familyId = familyId,
        value = value,
        unit = KILOMETER,
        customUnitLabel = null,
        recordDate = recordDate,
        note = null,
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    )
}
