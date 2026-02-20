package com.rafaelfelipeac.hermes.core.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

const val DARK_THEME_LUMINANCE_THRESHOLD = 0.5f
const val CONTENT_LUMINANCE_THRESHOLD = 0.5f
const val LIGHTER_TONE_BLEND_DARK = 0.16f
const val LIGHTER_TONE_BLEND_LIGHT = 0.1f
const val INDICATOR_EXTRA_BLEND_DARK = 0.12f
const val INDICATOR_EXTRA_BLEND_LIGHT = 0.16f

fun isDarkBackground(background: Color): Boolean {
    return background.luminance() < DARK_THEME_LUMINANCE_THRESHOLD
}

fun contentColorForBackground(background: Color): Color {
    return if (background.luminance() > CONTENT_LUMINANCE_THRESHOLD) {
        Color.Black
    } else {
        Color.White
    }
}
