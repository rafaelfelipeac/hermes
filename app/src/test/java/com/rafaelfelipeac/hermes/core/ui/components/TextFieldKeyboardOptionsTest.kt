package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.ui.text.input.KeyboardCapitalization
import org.junit.Assert.assertEquals
import org.junit.Test

class TextFieldKeyboardOptionsTest {
    @Test
    fun defaultTextFieldKeyboardOptions_capitalizesSentences() {
        assertEquals(
            KeyboardCapitalization.Sentences,
            DefaultTextFieldKeyboardOptions.capitalization,
        )
    }
}
