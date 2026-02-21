package com.rafaelfelipeac.hermes.core.ui.theme

import androidx.compose.ui.graphics.Color
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_AMBER
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_CYCLING
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_INDIGO
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_MOBILITY
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_OLIVE
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_ORANGE
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_OTHER
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_PINK
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_RUN
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_STRENGTH
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_SWIM
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.COLOR_UNCATEGORIZED

private val CategoryRed = Color(0xFFE24A4A)
private val CategoryOrange = Color(0xFFE3822B)
private val CategoryAmber = Color(0xFFD7A027)
private val CategoryTeal = Color(0xFF2EA8A1)
private val CategoryBlue = Color(0xFF3F7AC6)
private val CategoryIndigo = Color(0xFF4F63D8)
private val CategoryPurple = Color(0xFFA057D5)
private val CategoryPink = Color(0xFFE0549E)
private val CategoryLime = Color(0xFF7BBB2F)
private val CategoryOlive = Color(0xFF8DA337)
private val CategorySlate = Color(0xFF5F6F7F)

private val categoryPalette =
    listOf(
        CategoryColorOption(COLOR_UNCATEGORIZED, CategorySlate),
        CategoryColorOption(COLOR_RUN, CategoryBlue),
        CategoryColorOption(COLOR_CYCLING, CategoryRed),
        CategoryColorOption(COLOR_STRENGTH, CategoryPurple),
        CategoryColorOption(COLOR_SWIM, CategoryTeal),
        CategoryColorOption(COLOR_MOBILITY, CategoryLime),
        CategoryColorOption(COLOR_OTHER, CategoryAmber),
        CategoryColorOption(COLOR_AMBER, CategoryAmber),
        CategoryColorOption(COLOR_ORANGE, CategoryOrange),
        CategoryColorOption(COLOR_INDIGO, CategoryIndigo),
        CategoryColorOption(COLOR_PINK, CategoryPink),
        CategoryColorOption(COLOR_OLIVE, CategoryOlive),
    )

fun categoryAccentColor(colorId: String): Color {
    return categoryPalette.firstOrNull { it.id == colorId }?.accent ?: CategorySlate
}

fun categoryColorOptions(): List<CategoryColorOption> = categoryPalette

data class CategoryColorOption(
    val id: String,
    val accent: Color,
)
