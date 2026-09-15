package com.rafaelfelipeac.hermes.features.challenges.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R

@Composable
internal fun ChallengeOverflowMenu(
    onEdit: (() -> Unit)?,
    onArchive: (() -> Unit)?,
    onReactivate: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hasActions = onEdit != null || onArchive != null || onReactivate != null || onDelete != null
    if (!hasActions) return

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(R.string.challenges_actions_menu),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_edit)) },
                    onClick = {
                        expanded = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                    },
                )
            }
            if (onArchive != null) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_archive)) },
                    onClick = {
                        expanded = false
                        onArchive()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Unarchive, contentDescription = null)
                    },
                )
            }
            if (onReactivate != null) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_reactivate)) },
                    onClick = {
                        expanded = false
                        onReactivate()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                    },
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.challenges_delete)) },
                    onClick = {
                        expanded = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                    },
                )
            }
        }
    }
}
