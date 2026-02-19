package com.rafaelfelipeac.hermes.features.categories.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.theme.CategoryColorOption
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxl
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXxs
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.categoryColorOptions
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var editorCategory by remember { mutableStateOf<CategoryUi?>(null) }
    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRestoreDefaultsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var deletingCategory by remember { mutableStateOf<CategoryUi?>(null) }
    val listState = rememberLazyListState()

    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(bottom = SpacingXl),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingSm, vertical = SpacingSm),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.categories_back),
                    )
                }

                Text(
                    text = stringResource(R.string.categories_title),
                    style = typography.titleLarge,
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = SpacingXl)) {
                Button(onClick = { isAddDialogVisible = true }) {
                    Text(text = stringResource(R.string.categories_add))
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = SpacingXl)) {
                Surface(
                    shape = shapes.medium,
                    tonalElevation = ElevationSm,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = SpacingMd,
                                vertical = SpacingXxs,
                            ),
                    ) {
                        state.categories.forEachIndexed { index, category ->
                            CategoryRow(
                                category = category,
                                canMoveUp = index != 0,
                                canMoveDown = index != state.categories.lastIndex,
                                onMoveUp = { viewModel.moveCategoryUp(category.id) },
                                onMoveDown = { viewModel.moveCategoryDown(category.id) },
                                onToggleHidden = { isHidden ->
                                    viewModel.updateCategoryVisibility(category.id, isHidden)
                                },
                                onEdit = { editorCategory = category },
                                onDelete = { deletingCategory = category },
                                modifier = Modifier.padding(vertical = SpacingXxs),
                            )

                            if (index != state.categories.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = SpacingXxs),
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.padding(horizontal = SpacingXl)) {
                TextButton(
                    onClick = { isRestoreDefaultsDialogVisible = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary),
                ) {
                    Text(text = stringResource(R.string.categories_restore_defaults))
                }
            }
        }
    }

    if (isAddDialogVisible) {
        CategoryEditorDialog(
            title = stringResource(R.string.categories_add_title),
            confirmLabel = stringResource(R.string.categories_add_confirm),
            initialName = "",
            initialColorId = categoryColorOptions().first().id,
            onDismiss = { isAddDialogVisible = false },
            onConfirm = { name, colorId ->
                viewModel.addCategory(name, colorId)
                isAddDialogVisible = false
            },
        )
    }

    editorCategory?.let { category ->
        CategoryEditorDialog(
            title = stringResource(R.string.categories_edit_title),
            confirmLabel = stringResource(R.string.save_changes),
            initialName = category.name,
            initialColorId = category.colorId,
            onDismiss = { editorCategory = null },
            onConfirm = { name, colorId ->
                if (name != category.name) {
                    viewModel.renameCategory(category.id, name)
                }

                if (colorId != category.colorId) {
                    viewModel.updateCategoryColor(category.id, colorId)
                }

                editorCategory = null
            },
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text(text = stringResource(R.string.categories_delete_title, category.name)) },
            text = { Text(text = stringResource(R.string.categories_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id)
                        deletingCategory = null
                    },
                ) {
                    Text(text = stringResource(R.string.categories_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }

    if (isRestoreDefaultsDialogVisible) {
        AlertDialog(
            onDismissRequest = { isRestoreDefaultsDialogVisible = false },
            title = { Text(text = stringResource(R.string.categories_restore_defaults_title)) },
            text = { Text(text = stringResource(R.string.categories_restore_defaults_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreDefaultCategories()
                        isRestoreDefaultsDialogVisible = false
                    },
                ) {
                    Text(text = stringResource(R.string.categories_restore_defaults_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { isRestoreDefaultsDialogVisible = false }) {
                    Text(text = stringResource(R.string.add_workout_cancel))
                }
            },
        )
    }
}

@Composable
private fun CategoryRow(
    category: CategoryUi,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleHidden: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = categoryAccentColor(category.colorId)
    val isHiddenToggleEnabled = category.id != UNCATEGORIZED_ID

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = stringResource(R.string.categories_move_up),
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowDownward,
                    contentDescription = stringResource(R.string.categories_move_down),
                )
            }
        }

        Spacer(modifier = Modifier.width(SpacingMd))

        Box(modifier = Modifier.weight(1f)) {
            TitleChip(
                label = category.name,
                containerColor = accent,
                contentColor = Color.White,
                modifier = Modifier.wrapContentWidth(),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(SpacingXxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isHiddenToggleEnabled) {
                IconButton(
                    onClick = { onToggleHidden(!category.isHidden) },
                ) {
                    Icon(
                        imageVector =
                            if (category.isHidden) {
                                Icons.Outlined.VisibilityOff
                            } else {
                                Icons.Outlined.Visibility
                            },
                        contentDescription =
                            if (category.isHidden) {
                                stringResource(R.string.categories_show_action)
                            } else {
                                stringResource(R.string.categories_hide_action)
                            },
                    )
                }
            }

            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.categories_edit_action),
                )
            }

            if (category.id != UNCATEGORIZED_ID) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.categories_delete_action),
                    )
                }
            }
        }
    }
}


@Composable
private fun CategoryEditorDialog(
    title: String,
    confirmLabel: String,
    initialName: String,
    initialColorId: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var selectedColorId by rememberSaveable(initialColorId) { mutableStateOf(initialColorId) }
    val options = categoryColorOptions()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(R.string.categories_name_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.categories_color_label),
                    style = typography.labelMedium,
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                    modifier =
                        Modifier
                            .height(140.dp)
                            .fillMaxWidth(),
                ) {
                    items(options) { option ->
                        CategoryColorSwatch(
                            option = option,
                            isSelected = option.id == selectedColorId,
                            onClick = { selectedColorId = option.id },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim(), selectedColorId) },
                enabled = name.isNotBlank(),
            ) {
                Text(text = confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.add_workout_cancel))
            }
        },
    )
}

@Composable
private fun CategoryColorSwatch(
    option: CategoryColorOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant

    Box(
        modifier =
            Modifier
                .size(36.dp)
                .background(option.accent, CircleShape)
                .border(BorderStroke(BorderThin, borderColor), CircleShape)
                .clickable(onClick = onClick),
    )
}
