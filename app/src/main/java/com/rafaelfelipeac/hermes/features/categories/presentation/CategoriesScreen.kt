package com.rafaelfelipeac.hermes.features.categories.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.R
import com.rafaelfelipeac.hermes.core.AppConstants.EMPTY
import com.rafaelfelipeac.hermes.core.ui.components.DefaultTextFieldKeyboardOptions
import com.rafaelfelipeac.hermes.core.ui.components.KeyboardAwareDialogForm
import com.rafaelfelipeac.hermes.core.ui.components.TitleChip
import com.rafaelfelipeac.hermes.core.ui.components.capitalizedFirstCharacter
import com.rafaelfelipeac.hermes.core.ui.theme.CategoryColorOption
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.BorderThin
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CategoryActionIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CategoryColorGridHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CategoryColorSwatchSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CategoryMoveIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.CategoryRowMinHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.ElevationSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.FloatingActionContentBottomPadding
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconGlyphSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.HelpIconSize
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl
import com.rafaelfelipeac.hermes.core.ui.theme.categoryAccentColor
import com.rafaelfelipeac.hermes.core.ui.theme.categoryColorOptions
import com.rafaelfelipeac.hermes.core.ui.theme.contentColorForBackground
import com.rafaelfelipeac.hermes.features.categories.domain.CategoryDefaults.UNCATEGORIZED_ID
import com.rafaelfelipeac.hermes.features.categories.presentation.model.CategoryUi
import kotlin.math.roundToInt

@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var editorCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isAddDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isRestoreDefaultsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var deletingCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var isHelpDialogVisible by rememberSaveable { mutableStateOf(false) }
    var draggedCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dragStartIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var dragTargetIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var dragOffsetY by rememberSaveable { mutableStateOf(0f) }
    var categoryRowHeightPx by rememberSaveable { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val actionIconTint = colorScheme.onSurfaceVariant
    val displayedCategories by remember(state.categories, draggedCategoryId, dragTargetIndex) {
        derivedStateOf {
            val draggedId = draggedCategoryId
            val targetIndex = dragTargetIndex
            if (draggedId == null || targetIndex == null) {
                state.categories
            } else {
                state.categories.previewMovedCategory(draggedId, targetIndex)
            }
        }
    }

    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogVisible = true },
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = SpacingXl),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.categories_add),
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SpacingSm,
                            end = SpacingXl,
                            top = SpacingSm,
                            bottom = SpacingSm,
                        ),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = stringResource(R.string.categories_back),
                    )
                }

                Text(
                    text = stringResource(R.string.categories_title),
                    style = typography.titleLarge,
                )

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = { isHelpDialogVisible = true },
                    shape = CircleShape,
                    color = colorScheme.surfaceVariant,
                    tonalElevation = ElevationSm,
                    shadowElevation = ElevationSm,
                    modifier = Modifier.size(HelpIconSize),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.categories_help_icon),
                            tint = actionIconTint,
                            modifier = Modifier.size(HelpIconGlyphSize),
                        )
                    }
                }
            }

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = FloatingActionContentBottomPadding),
                verticalArrangement = Arrangement.spacedBy(SpacingLg),
            ) {
                item {
                    TextButton(
                        onClick = { isRestoreDefaultsDialogVisible = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary),
                        modifier = Modifier.padding(horizontal = SpacingXl),
                    ) {
                        Text(text = stringResource(R.string.categories_restore_defaults))
                    }
                }

                itemsIndexed(
                    items = displayedCategories,
                    key = { _, category -> category.id },
                ) { index, category ->
                    val isDragging = category.id == draggedCategoryId

                    CategoryRow(
                        category = category,
                        canMoveUp = index != 0,
                        canMoveDown = index != displayedCategories.lastIndex,
                        isDragging = isDragging,
                        onMoveUp = { viewModel.moveCategoryUp(category.id) },
                        onMoveDown = { viewModel.moveCategoryDown(category.id) },
                        onToggleHidden = { isHidden ->
                            viewModel.updateCategoryVisibility(category.id, isHidden)
                        },
                        onEdit = { editorCategoryId = category.id },
                        onDelete = { deletingCategoryId = category.id },
                        onDragStart = {
                            draggedCategoryId = category.id
                            dragStartIndex = state.categories.indexOfFirst { it.id == category.id }
                            dragTargetIndex = dragStartIndex
                            dragOffsetY = 0f
                        },
                        onDrag = { deltaY ->
                            val startIndex = dragStartIndex ?: return@CategoryRow
                            if (categoryRowHeightPx <= 0) return@CategoryRow
                            dragOffsetY += deltaY
                            dragTargetIndex =
                                (startIndex + (dragOffsetY / categoryRowHeightPx).roundToInt())
                                    .coerceIn(state.categories.indices)
                        },
                        onDragEnd = {
                            val startIndex = dragStartIndex
                            val targetIndex = dragTargetIndex
                            if (startIndex != null && targetIndex != null && startIndex != targetIndex) {
                                viewModel.moveCategoryToPosition(category.id, targetIndex)
                            }
                            draggedCategoryId = null
                            dragStartIndex = null
                            dragTargetIndex = null
                            dragOffsetY = 0f
                        },
                        onMeasured = { height -> categoryRowHeightPx = height },
                        modifier =
                            Modifier
                                .padding(horizontal = SpacingXl)
                                .offset {
                                    if (isDragging) {
                                        IntOffset(x = 0, y = dragOffsetY.roundToInt())
                                    } else {
                                        IntOffset.Zero
                                    }
                                }
                                .zIndex(if (isDragging) CATEGORY_DRAG_Z_INDEX else CATEGORY_ROW_Z_INDEX),
                    )
                }
            }
        }
    }

    if (isAddDialogVisible) {
        CategoryEditorDialog(
            title = stringResource(R.string.categories_add_title),
            confirmLabel = stringResource(R.string.categories_add_confirm),
            initialName = EMPTY,
            initialColorId = categoryColorOptions().first().id,
            onDismiss = { isAddDialogVisible = false },
            onConfirm = { name, colorId ->
                viewModel.addCategory(name, colorId)
                isAddDialogVisible = false
            },
        )
    }

    val editorCategory =
        editorCategoryId?.let { id ->
            state.categories.firstOrNull { it.id == id }
        }

    editorCategory?.let { category ->
        CategoryEditorDialog(
            title = stringResource(R.string.categories_edit_title),
            confirmLabel = stringResource(R.string.save_changes),
            initialName = category.name,
            initialColorId = category.colorId,
            onDismiss = { editorCategoryId = null },
            onConfirm = { name, colorId ->
                if (name != category.name) {
                    viewModel.renameCategory(category.id, name)
                }

                if (colorId != category.colorId) {
                    viewModel.updateCategoryColor(category.id, colorId)
                }

                editorCategoryId = null
            },
        )
    }

    val deletingCategory =
        deletingCategoryId?.let { id ->
            state.categories.firstOrNull { it.id == id }
        }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategoryId = null },
            title = { Text(text = stringResource(R.string.categories_delete_title, category.name)) },
            text = { Text(text = stringResource(R.string.categories_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category.id)
                        deletingCategoryId = null
                    },
                ) {
                    Text(text = stringResource(R.string.categories_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategoryId = null }) {
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

    if (isHelpDialogVisible) {
        AlertDialog(
            onDismissRequest = { isHelpDialogVisible = false },
            title = { Text(text = stringResource(R.string.categories_help_title)) },
            text = { Text(text = stringResource(R.string.categories_help_message)) },
            confirmButton = {
                TextButton(onClick = { isHelpDialogVisible = false }) {
                    Text(text = stringResource(R.string.categories_help_confirm))
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
    isDragging: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleHidden: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onMeasured: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = categoryAccentColor(category.colorId)
    val isHiddenToggleEnabled = category.id != UNCATEGORIZED_ID
    val contentAlpha = if (isDragging) DRAGGING_ROW_ALPHA else ENABLED_ROW_ALPHA

    Surface(
        shape = shapes.medium,
        tonalElevation = ElevationSm,
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(contentAlpha)
                .onGloballyPositioned { coordinates -> onMeasured(coordinates.size.height) },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .heightIn(min = CategoryRowMinHeight)
                    .fillMaxWidth(),
        ) {
            IconButton(
                onClick = { },
                modifier =
                    Modifier
                        .size(CategoryMoveIconSize)
                        .pointerInput(category.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDrag = { _, dragAmount ->
                                    onDrag(dragAmount.y)
                                },
                                onDragEnd = onDragEnd,
                                onDragCancel = onDragEnd,
                            )
                        },
            ) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = stringResource(R.string.categories_drag_action),
                    tint = colorScheme.onSurfaceVariant,
                )
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(vertical = SpacingMd),
            ) {
                TitleChip(
                    label = category.name,
                    containerColor = accent,
                    contentColor = contentColorForBackground(accent),
                )
                if (category.isHidden) {
                    Text(
                        text = stringResource(R.string.categories_hidden_status),
                        style = typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }

            CategoryRowMenu(
                category = category,
                canMoveUp = canMoveUp,
                canMoveDown = canMoveDown,
                canToggleHidden = isHiddenToggleEnabled,
                onMoveUp = onMoveUp,
                onMoveDown = onMoveDown,
                onToggleHidden = onToggleHidden,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun CategoryRowMenu(
    category: CategoryUi,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canToggleHidden: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleHidden: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(category.id) { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.size(CategoryActionIconSize),
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.categories_more_actions),
                tint = colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            CategoryMenuItem(
                label = stringResource(R.string.categories_move_up),
                icon = { Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = null) },
                enabled = canMoveUp,
                onClick = {
                    expanded = false
                    onMoveUp()
                },
            )
            CategoryMenuItem(
                label = stringResource(R.string.categories_move_down),
                icon = { Icon(imageVector = Icons.Outlined.ArrowDownward, contentDescription = null) },
                enabled = canMoveDown,
                onClick = {
                    expanded = false
                    onMoveDown()
                },
            )
            if (canToggleHidden) {
                CategoryMenuItem(
                    label =
                        if (category.isHidden) {
                            stringResource(R.string.categories_show_action)
                        } else {
                            stringResource(R.string.categories_hide_action)
                        },
                    icon = {
                        Icon(
                            imageVector =
                                if (category.isHidden) {
                                    Icons.Outlined.VisibilityOff
                                } else {
                                    Icons.Outlined.Visibility
                                },
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        expanded = false
                        onToggleHidden(!category.isHidden)
                    },
                )
            }
            CategoryMenuItem(
                label = stringResource(R.string.categories_edit_action),
                icon = { Icon(imageVector = Icons.Outlined.Edit, contentDescription = null) },
                onClick = {
                    expanded = false
                    onEdit()
                },
            )
            if (category.id != UNCATEGORIZED_ID) {
                CategoryMenuItem(
                    label = stringResource(R.string.categories_delete_action),
                    icon = { Icon(imageVector = Icons.Outlined.Delete, contentDescription = null) },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    DropdownMenuItem(
        text = { Text(text = label) },
        leadingIcon = icon,
        enabled = enabled,
        onClick = onClick,
    )
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
    var name by rememberSaveable(initialName) { mutableStateOf(initialName.capitalizedFirstCharacter()) }
    var selectedColorId by rememberSaveable(initialColorId) { mutableStateOf(initialColorId) }
    val options = categoryColorOptions()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            KeyboardAwareDialogForm(
                verticalArrangement = Arrangement.spacedBy(SpacingMd),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.capitalizedFirstCharacter() },
                    label = { Text(text = stringResource(R.string.categories_name_label)) },
                    keyboardOptions = DefaultTextFieldKeyboardOptions,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.categories_color_label),
                    style = typography.labelMedium,
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(CATEGORY_COLOR_GRID_COLUMNS),
                    horizontalArrangement = Arrangement.spacedBy(SpacingSm),
                    verticalArrangement = Arrangement.spacedBy(SpacingSm),
                    modifier =
                        Modifier
                            .height(CategoryColorGridHeight)
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

private const val CATEGORY_COLOR_GRID_COLUMNS = 4
private const val CATEGORY_DRAG_Z_INDEX = 1f
private const val CATEGORY_ROW_Z_INDEX = 0f
private const val DRAGGING_ROW_ALPHA = 0.82f
private const val ENABLED_ROW_ALPHA = 1f

private fun List<CategoryUi>.previewMovedCategory(
    categoryId: Long,
    targetIndex: Int,
): List<CategoryUi> {
    val currentIndex = indexOfFirst { it.id == categoryId }
    if (currentIndex == -1 || targetIndex !in indices || currentIndex == targetIndex) return this

    return toMutableList().apply {
        val category = removeAt(currentIndex)
        add(targetIndex, category)
    }
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
                .size(CategoryColorSwatchSize)
                .background(option.accent, CircleShape)
                .border(BorderStroke(BorderThin, borderColor), CircleShape)
                .clickable(onClick = onClick),
    )
}
