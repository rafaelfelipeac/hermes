package com.rafaelfelipeac.hermes.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val Typography =
    Typography(
        bodyLarge =
            TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = TextSizes.BodyLargeFontSize,
                lineHeight = TextSizes.BodyLargeLineHeight,
                letterSpacing = TextSizes.BodyLargeLetterSpacing,
            ),
    )
