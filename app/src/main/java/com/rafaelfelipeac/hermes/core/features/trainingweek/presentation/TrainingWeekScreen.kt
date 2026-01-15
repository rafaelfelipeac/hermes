package com.rafaelfelipeac.hermes.core.features.trainingweek.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TrainingWeekScreen(
    modifier: Modifier = Modifier,
    viewModel: TrainingWeekViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .background(Color.Red)
            .fillMaxSize()
    ) { }
}
