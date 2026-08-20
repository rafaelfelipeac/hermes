@file:OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.rafaelfelipeac.hermes.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.window.DialogProperties
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.DatePickerDialogMaxHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.KeyboardVisibleDialogContentMaxHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.PlannedItemDialogContentMaxHeight
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingMd
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl

internal const val HERMES_DATE_PICKER_DIALOG_TAG = "hermes_date_picker_dialog"

@Composable
internal fun KeyboardAwareDialogForm(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val contentMaxHeight =
        if (WindowInsets.isImeVisible) {
            KeyboardVisibleDialogContentMaxHeight
        } else {
            PlannedItemDialogContentMaxHeight
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(max = contentMaxHeight)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = verticalArrangement,
        content = content,
    )
}

@Composable
internal fun HermesDatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = DatePickerDefaults.colors()

    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingXl),
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = DatePickerDialogMaxHeight)
                    .testTag(HERMES_DATE_PICKER_DIALOG_TAG),
            shape = DatePickerDefaults.shape,
            color = colors.containerColor,
            tonalElevation = DatePickerDefaults.TonalElevation,
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Box(modifier = Modifier.weight(1f, fill = false)) {
                    content()
                }

                Row(
                    modifier =
                        Modifier
                            .align(Alignment.End)
                            .padding(end = SpacingSm, bottom = SpacingMd),
                    horizontalArrangement = Arrangement.spacedBy(SpacingMd),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}
