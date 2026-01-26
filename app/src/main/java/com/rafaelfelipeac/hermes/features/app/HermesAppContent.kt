package com.rafaelfelipeac.hermes.features.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.SETTINGS
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.WEEKLY_TRAINING
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.WeeklyTrainingScreen

@Composable
fun HermesAppContent() {
    var currentDestination by rememberSaveable { mutableStateOf(WEEKLY_TRAINING) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.labelRes),
                        )
                    },
                    label = { Text(stringResource(it.labelRes)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it },
                )
            }
        },
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                WEEKLY_TRAINING -> WeeklyTrainingScreen(modifier = Modifier.padding(innerPadding))
                SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
