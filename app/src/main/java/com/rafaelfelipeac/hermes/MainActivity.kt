package com.rafaelfelipeac.hermes

import android.app.Activity
import android.app.LocaleManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.TRAINING_WEEK
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.SETTINGS
import com.rafaelfelipeac.hermes.core.settings.AppLanguage
import com.rafaelfelipeac.hermes.core.settings.ThemeMode
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.TrainingWeekScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.activity.ComponentActivity

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            HermesAppRoot()
        }
    }
}

@PreviewScreenSizes
@Composable
private fun HermesAppPreview() {
    HermesTheme(darkTheme = false) {
        HermesAppContent()
    }
}

@Composable
private fun HermesAppRoot() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(settingsState.language) {
        val applied = applyAppLanguage(context, settingsState.language)
        if (applied && activity != null) {
            activity.recreate()
        }
    }

    val darkTheme = when (settingsState.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    HermesTheme(darkTheme = darkTheme) {
        HermesAppContent()
    }
}

private fun applyAppLanguage(
    context: android.content.Context,
    language: AppLanguage
): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val desired = if (language == AppLanguage.SYSTEM) {
            LocaleList.getEmptyLocaleList()
        } else {
            LocaleList.forLanguageTags(language.tag)
        }
        if (localeManager.applicationLocales != desired) {
            localeManager.applicationLocales = desired
            true
        } else {
            false
        }
    } else {
        val desired = if (language == AppLanguage.SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(language.tag)
        }
        if (AppCompatDelegate.getApplicationLocales() != desired) {
            AppCompatDelegate.setApplicationLocales(desired)
            true
        } else {
            false
        }
    }
}

@Composable
private fun HermesAppContent() {
    var currentDestination by rememberSaveable { mutableStateOf(TRAINING_WEEK) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            it.icon,
                            contentDescription = stringResource(it.labelRes)
                        )
                    },
                    label = { Text(stringResource(it.labelRes)) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                TRAINING_WEEK -> TrainingWeekScreen(modifier = Modifier.padding(innerPadding))
                SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}
