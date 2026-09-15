@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)
@file:Suppress("TooManyFunctions")

package com.rafaelfelipeac.hermes.features.personalrecords.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillHorizontalPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.AddActionPillMinWidth
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_DARK
import com.rafaelfelipeac.hermes.core.ui.theme.INDICATOR_EXTRA_BLEND_LIGHT
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.model.Category

@Composable
internal fun AddActionPill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color? = null,
    contentColor: Color? = null,
) {
    Surface(
        onClick = onClick,
        shape = shapes.extraLarge,
        tonalElevation = ElevationSm,
        shadowElevation = ElevationSm,
        color = containerColor ?: colorScheme.surface,
        contentColor = contentColor ?: colorScheme.onSurface,
        modifier =
            Modifier
                .fillMaxWidth()
                .defaultMinSize(minWidth = AddActionPillMinWidth),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = AddActionPillHorizontalPadding,
                    vertical = SpacingLg,
                ),
            horizontalArrangement = Arrangement.spacedBy(SpacingLg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = label, style = typography.titleSmall)
        }
    }
}

internal data class PersonalRecordRelatedColors(
    val container: Color,
    val content: Color,
)

@Composable
internal fun personalRecordRelatedColors(category: Category?): PersonalRecordRelatedColors {
    val container =
        category?.colorId?.let(::categoryAccentColor)?.let { categoryColor ->
            val blend =
                if (isSystemInDarkTheme()) {
                    INDICATOR_EXTRA_BLEND_DARK
                } else {
                    INDICATOR_EXTRA_BLEND_LIGHT
                }
            lerp(categoryColor, colorScheme.surface, blend)
        } ?: colorScheme.primaryContainer
    val content =
        if (category == null) {
            colorScheme.onPrimaryContainer
        } else {
            contentColorForBackground(container)
        }
    return PersonalRecordRelatedColors(container = container, content = content)
}

@Composable
internal fun PersonalRecordCategoryChip(category: Category?) {
    val accent = category?.colorId?.let(::categoryAccentColor) ?: colorScheme.surfaceVariant
    val contentColor =
        if (category == null) {
            colorScheme.onSurfaceVariant
        } else {
            contentColorForBackground(accent)
        }

    TitleChip(
        label = category?.name ?: stringResource(R.string.category_uncategorized),
        containerColor = accent,
        contentColor = contentColor,
    )
}
