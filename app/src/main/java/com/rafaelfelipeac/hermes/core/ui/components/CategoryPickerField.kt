package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID

internal data class CategoryPickerOption(
    val id: Long,
    val name: String,
    val colorId: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategoryPickerField(
    label: String,
    categories: List<CategoryPickerOption>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    onManageCategories: (() -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedCategory = categories.firstOrNull { it.id == selectedCategoryId }
    val fallbackLabel = stringResource(R.string.category_uncategorized)
    val selectedLabel = selectedCategory?.name ?: fallbackLabel
    val selectedAccent =
        selectedCategory?.takeIf { it.id != UNCATEGORIZED_ID }?.let { categoryAccentColor(it.colorId) }
    val selectedContainerColor = selectedAccent ?: colorScheme.surfaceVariant
    val selectedContentColor =
        selectedAccent?.let { contentColorForBackground(it) } ?: colorScheme.onSurfaceVariant

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            readOnly = true,
            value = selectedLabel,
            onValueChange = {},
            label = { Text(text = label) },
            textStyle = TextStyle(color = Color.Transparent),
            prefix = {
                TitleChip(
                    label = selectedLabel,
                    containerColor = selectedContainerColor,
                    contentColor = selectedContentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            categories.forEachIndexed { index, category ->
                val accent =
                    if (category.id == UNCATEGORIZED_ID) {
                        null
                    } else {
                        categoryAccentColor(category.colorId)
                    }
                DropdownMenuItem(
                    text = {
                        TitleChip(
                            label = category.name,
                            containerColor = accent ?: colorScheme.surfaceVariant,
                            contentColor = accent?.let { contentColorForBackground(it) } ?: colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onCategorySelected(if (category.id == UNCATEGORIZED_ID) null else category.id)
                        expanded = false
                    },
                )
                if (index != categories.lastIndex || onManageCategories != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = SpacingSm))
                }
            }

            onManageCategories?.let { manageCategories ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(R.string.workout_dialog_manage_categories),
                            color = colorScheme.primary,
                        )
                    },
                    onClick = {
                        expanded = false
                        manageCategories()
                    },
                )
            }
        }
    }
}
