package com.rafaelfelipeac.hermes.features.browse.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BrowseScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun browseHomeContent_clickingKnowledgeBaseCardInvokesNavigation() {
        var navigatedTo: BrowseDestination? = null

        composeRule.setContent {
            BrowseHomeContent(
                onNavigateTo = { navigatedTo = it },
            )
        }

        composeRule.onNodeWithTag(BROWSE_KNOWLEDGE_BASE_CARD_TAG).performClick()

        composeRule.runOnIdle {
            assertEquals(BrowseDestination.KNOWLEDGE_BASE, navigatedTo)
        }
    }
}
