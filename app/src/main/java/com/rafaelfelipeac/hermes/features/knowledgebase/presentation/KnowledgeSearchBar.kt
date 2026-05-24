package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.R

internal const val KNOWLEDGE_BASE_SEARCH_TAG = "knowledge_base_search"

@Composable
internal fun KnowledgeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(R.string.knowledge_base_search_label)) },
        placeholder = { Text(stringResource(R.string.knowledge_base_search_placeholder)) },
        leadingIcon = { Icon(imageVector = Icons.Outlined.Search, contentDescription = null) },
        modifier = modifier.testTag(KNOWLEDGE_BASE_SEARCH_TAG),
        singleLine = true,
    )
}
