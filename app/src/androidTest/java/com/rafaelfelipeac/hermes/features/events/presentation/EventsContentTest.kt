package com.rafaelfelipeac.hermes.features.events.presentation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventsTestViewportHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.EventsTestViewportWidth
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.canonicalStorageWeekStart
import com.rafaelfelipeac.hermes.features.weeklytraining.domain.model.EventType.RACE_EVENT
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutUi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class EventsContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun eventsContent_placesEventsInRowMajorChronologicalOrder() {
        composeRule.setContent {
            EventsContent(
                state =
                    EventsUiState(
                        events =
                            listOf(
                                event(id = 1L, title = "10 km", date = LocalDate.now()),
                                event(id = 2L, title = "15 km", date = LocalDate.now().plusDays(7)),
                                event(id = 3L, title = "21,1 km", date = LocalDate.now().plusDays(14)),
                                event(id = 4L, title = "100 km", date = LocalDate.now().plusDays(21)),
                            ),
                        categories = listOf(category()),
                    ),
                modifier =
                    Modifier
                        .width(EventsTestViewportWidth)
                        .height(EventsTestViewportHeight),
                onEditEvent = {},
                onToggleCompleted = { _, _ -> },
                onDeleteEvent = {},
            )
        }

        composeRule.onNodeWithText("10 km").assertExists()
        composeRule.onNodeWithText("15 km").assertExists()
        composeRule.onNodeWithText("21,1 km").assertExists()
        composeRule.onNodeWithText("100 km").assertExists()

        val first = composeRule.onNodeWithTag(eventCardTag(1L)).getBoundsInRoot()
        val second = composeRule.onNodeWithTag(eventCardTag(2L)).getBoundsInRoot()
        val third = composeRule.onNodeWithTag(eventCardTag(3L)).getBoundsInRoot()
        val fourth = composeRule.onNodeWithTag(eventCardTag(4L)).getBoundsInRoot()

        assertEquals(first.top, second.top)
        assertTrue(first.left < second.left)
        assertTrue(first.bottom <= third.top)
        assertEquals(third.top, fourth.top)
        assertTrue(third.left < fourth.left)
    }

    @Test
    fun eventsContent_keepsCardsEqualHeightWithAndWithoutDescription() {
        val longDescription = "Maratona de São Paulo"
        val longDescriptionDate = LocalDate.now().plusDays(28)
        val longDescriptionDateLabel =
            longDescriptionDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()))

        composeRule.setContent {
            EventsContent(
                state =
                    EventsUiState(
                        events =
                            listOf(
                                event(
                                    id = 1L,
                                    title = "42,195 km",
                                    description = longDescription,
                                    date = longDescriptionDate,
                                ),
                                event(id = 2L, title = "15 km", date = LocalDate.now().plusDays(7)),
                            ),
                        categories = listOf(category()),
                    ),
                modifier =
                    Modifier
                        .width(EventsTestViewportWidth)
                        .height(EventsTestViewportHeight),
                onEditEvent = {},
                onToggleCompleted = { _, _ -> },
                onDeleteEvent = {},
            )
        }

        val withDescription = composeRule.onNodeWithTag(eventCardTag(1L)).getBoundsInRoot()
        val withoutDescription = composeRule.onNodeWithTag(eventCardTag(2L)).getBoundsInRoot()

        composeRule.onNodeWithText(longDescription).assertIsDisplayed()
        composeRule.onNodeWithText("42,195 km").assertIsDisplayed()
        composeRule.onNodeWithText(longDescriptionDateLabel).assertIsDisplayed()

        assertEquals(
            withDescription.bottom - withDescription.top,
            withoutDescription.bottom - withoutDescription.top,
        )
    }

    private fun eventCardTag(id: Long): String = EVENT_CARD_TAG_PREFIX + id

    private fun category(): CategoryUi {
        return CategoryUi(
            id = 1L,
            name = "Run",
            colorId = "run",
            sortOrder = 0,
            isHidden = false,
            isSystem = true,
        )
    }

    private fun event(
        id: Long,
        title: String,
        description: String = "",
        date: LocalDate,
    ): WorkoutUi {
        return WorkoutUi(
            id = id,
            weekStartDate = canonicalStorageWeekStart(date),
            dayOfWeek = date.dayOfWeek,
            type = title,
            description = description,
            isCompleted = false,
            isRestDay = false,
            categoryId = 1L,
            categoryColorId = "run",
            categoryName = "Run",
            order = 0,
            eventType = RACE_EVENT,
        )
    }
}
