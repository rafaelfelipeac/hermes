package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HermesSnackbarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun paletteUsesNeutralThemeColorsInLightTheme() {
        assertPaletteUsesNeutralThemeColors(darkTheme = false)
    }

    @Test
    fun paletteUsesNeutralThemeColorsInDarkTheme() {
        assertPaletteUsesNeutralThemeColors(darkTheme = true)
    }

    private fun assertPaletteUsesNeutralThemeColors(darkTheme: Boolean) {
        var expectedContainerColor = Color.Unspecified
        var expectedContentColor = Color.Unspecified
        var expectedActionColor = Color.Unspecified
        var actualPalette: Triple<Color, Color, Color>? = null

        composeRule.setContent {
            HermesTheme(darkTheme = darkTheme, dynamicColor = false) {
                expectedContainerColor = colorScheme.surfaceVariant
                expectedContentColor = colorScheme.onSurfaceVariant
                expectedActionColor = colorScheme.primary
                actualPalette = hermesSnackbarPalette(colorScheme)
            }
        }

        composeRule.runOnIdle {
            val palette = requireNotNull(actualPalette)

            assertEquals(expectedContainerColor, palette.first)
            assertEquals(expectedContentColor, palette.second)
            assertEquals(expectedActionColor, palette.third)
        }
    }
}
