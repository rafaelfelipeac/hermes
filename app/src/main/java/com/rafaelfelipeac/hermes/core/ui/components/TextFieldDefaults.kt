package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization

internal val DefaultTextFieldKeyboardOptions =
    KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)

internal fun String.capitalizedFirstCharacter(): String {
    if (isEmpty()) return this
    return first().uppercaseChar() + substring(1)
}
