package com.rafaelfelipeac.hermes.features.knowledgebase.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun KnowledgeBaseRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onOpenWorkout: (Long) -> Unit,
    onOpenEvent: (Long) -> Unit,
) {
    KnowledgeBaseScreen(
        modifier = modifier,
        onBack = onBack,
        onOpenWorkout = onOpenWorkout,
        onOpenEvent = onOpenEvent,
    )
}
