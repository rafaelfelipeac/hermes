package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import android.content.Context
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.HERMES_DATE_PICKER_DIALOG_TAG
import com.rafaelfelipeac.hermes.core.ui.components.formatWorkoutDate
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordComparisonRule.HIGHER_IS_BETTER
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordEntry
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordFamily
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordMetricType.DISTANCE
import com.rafaelfelipeac.hermes.features.personalrecords.domain.model.PersonalRecordUnit.KILOMETER
import com.rafaelfelipeac.hermes.features.settings.domain.model.DistanceUnit.KILOMETERS
import com.rafaelfelipeac.hermes.features.settings.domain.model.WeightUnit.KILOGRAMS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.Locale

class PersonalRecordsEntryEditorDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun dateField_opensPicker_andCancelKeepsExistingValue() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val family = sampleFamily()
        val entryDate = LocalDate.of(2024, 2, 1)
        val expectedDateLabel = formatWorkoutDate(entryDate, Locale.getDefault())

        composeRule.setContent {
            PersonalRecordEntryEditorDialog(
                families = listOf(family),
                entries = emptyList(),
                initialFamilyId = family.id,
                settingsDistanceUnit = KILOMETERS,
                settingsWeightUnit = KILOGRAMS,
                initialEntry = sampleEntry(familyId = family.id, recordDate = entryDate),
                isEdit = true,
                onDismiss = {},
                onSave = { _, _, _, _, _, _ -> },
            )
        }

        composeRule.onNodeWithText(expectedDateLabel).assertIsDisplayed()
        composeRule.onNodeWithTag(PERSONAL_RECORDS_ENTRY_DATE_FIELD_TAG).performClick()
        composeRule.onNodeWithText(context.getString(R.string.personal_records_confirm)).assertIsDisplayed()
        val datePickerBounds =
            composeRule
                .onNodeWithTag(HERMES_DATE_PICKER_DIALOG_TAG)
                .fetchSemanticsNode()
                .boundsInRoot
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        assertTrue(datePickerBounds.left > 0f)
        assertTrue(datePickerBounds.right < screenWidth)
        composeRule.onAllNodesWithText(context.getString(R.string.add_workout_cancel))[1].performClick()
        composeRule.onNodeWithText(expectedDateLabel).assertIsDisplayed()
    }

    @Test
    fun futureDateDisablesSaveAction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val futureDate = LocalDate.now().plusDays(1)

        composeRule.setContent {
            PersonalRecordEntryEditorDialog(
                families = listOf(sampleFamily()),
                entries = emptyList(),
                initialFamilyId = 1L,
                settingsDistanceUnit = KILOMETERS,
                settingsWeightUnit = KILOGRAMS,
                initialEntry = sampleEntry(familyId = 1L, recordDate = futureDate),
                isEdit = true,
                onDismiss = {},
                onSave = { _, _, _, _, _, _ -> },
            )
        }

        composeRule.onNodeWithText(context.getString(R.string.save_changes)).assertIsNotEnabled()
    }

    @Test
    fun editActions_placeDeleteLeftOfCancelAndSave() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val family = sampleFamily()

        composeRule.setContent {
            PersonalRecordEntryEditorDialog(
                families = listOf(family),
                entries = emptyList(),
                initialFamilyId = family.id,
                settingsDistanceUnit = KILOMETERS,
                settingsWeightUnit = KILOGRAMS,
                initialEntry = sampleEntry(familyId = family.id, recordDate = LocalDate.of(2024, 2, 1)),
                isEdit = true,
                onDismiss = {},
                onSave = { _, _, _, _, _, _ -> },
                onDeleteRequested = {},
            )
        }

        val deleteBounds =
            composeRule
                .onNodeWithContentDescription(
                    context.getString(R.string.personal_records_delete_result_confirm),
                ).fetchSemanticsNode()
                .boundsInRoot
        val cancelBounds =
            composeRule
                .onNodeWithText(context.getString(R.string.add_workout_cancel))
                .fetchSemanticsNode()
                .boundsInRoot
        val saveBounds =
            composeRule
                .onNodeWithText(context.getString(R.string.save_changes))
                .fetchSemanticsNode()
                .boundsInRoot

        assertTrue(deleteBounds.center.x < cancelBounds.center.x)
        assertTrue(cancelBounds.center.x < saveBounds.center.x)
    }

    @Test
    fun focusedValueField_keepsCancelAndSaveVisible() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val family = sampleFamily()

        composeRule.setContent {
            PersonalRecordEntryEditorDialog(
                families = listOf(family),
                entries = emptyList(),
                initialFamilyId = family.id,
                settingsDistanceUnit = KILOMETERS,
                settingsWeightUnit = KILOGRAMS,
                initialEntry = sampleEntry(familyId = family.id, recordDate = LocalDate.of(2024, 2, 1)),
                isEdit = true,
                onDismiss = {},
                onSave = { _, _, _, _, _, _ -> },
                onDeleteRequested = {},
            )
        }

        composeRule.onNodeWithTag(PERSONAL_RECORDS_ENTRY_VALUE_FIELD_TAG).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(context.getString(R.string.add_workout_cancel)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.save_changes)).assertIsDisplayed()
    }

    @Test
    fun newResult_defaultsToCurrentPersonalRecordInsteadOfLatestEntry() {
        val family = sampleFamily()
        val currentBest =
            sampleEntry(
                familyId = family.id,
                recordDate = LocalDate.of(2024, 1, 1),
                id = 10L,
                value = 10.0,
            )
        val newerButLower =
            sampleEntry(
                familyId = family.id,
                recordDate = LocalDate.of(2024, 2, 1),
                id = 11L,
                value = 5.0,
            )

        composeRule.setContent {
            PersonalRecordEntryEditorDialog(
                families = listOf(family),
                entries = listOf(currentBest, newerButLower),
                initialFamilyId = family.id,
                settingsDistanceUnit = KILOMETERS,
                settingsWeightUnit = KILOGRAMS,
                onDismiss = {},
                onSave = { _, _, _, _, _, _ -> },
            )
        }

        composeRule.waitForIdle()
        val editableValue =
            composeRule
                .onNodeWithTag(PERSONAL_RECORDS_ENTRY_VALUE_FIELD_TAG)
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text
        assertEquals("10", editableValue)
    }

    private fun sampleFamily() =
        PersonalRecordFamily(
            id = 1L,
            categoryId = null,
            title = "5K",
            metricType = DISTANCE,
            defaultUnit = KILOMETER,
            comparisonRule = HIGHER_IS_BETTER,
            manualCurrentEntryId = null,
            sortOrder = 0,
            createdAt = Instant.parse("2024-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
        )

    private fun sampleEntry(
        familyId: Long,
        recordDate: LocalDate,
        id: Long = 10L,
        value: Double = 5.0,
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
