package com.rafaelfelipeac.hermes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.rafaelfelipeac.hermes.core.features.home.presentation.HomeScreen
import com.rafaelfelipeac.hermes.core.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.HOME
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.SETTINGS
import com.rafaelfelipeac.hermes.core.theme.HermesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HermesTheme {
                HermesApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun HermesApp() {
    var currentDestination by rememberSaveable { mutableStateOf(HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                HOME -> HomeScreen(modifier = Modifier.padding(innerPadding))
                SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
