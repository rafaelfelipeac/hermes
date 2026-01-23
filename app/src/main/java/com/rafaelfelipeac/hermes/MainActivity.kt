package com.rafaelfelipeac.hermes

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.SETTINGS
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.TRAINING_WEEK
import com.rafaelfelipeac.hermes.core.ui.theme.HermesTheme
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel
import com.rafaelfelipeac.hermes.features.trainingweek.presentation.TrainingWeekScreen
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewData
import com.rafaelfelipeac.hermes.core.ui.preview.HermesAppPreviewProvider
import com.rafaelfelipeac.hermes.features.settings.domain.model.AppLanguage.SYSTEM
import com.rafaelfelipeac.hermes.features.settings.domain.model.ThemeMode.*
import dagger.hilt.android.AndroidEntryPoint

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

    val darkTheme =
        when (settingsState.themeMode) {
            DARK -> true
            LIGHT -> false
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }

    HermesTheme(darkTheme = darkTheme) {
        HermesAppContent()
    }
}

private fun applyAppLanguage(
    context: Context,
    language: AppLanguage,
): Boolean {
    return if (SDK_INT >= TIRAMISU) {
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val desired =
            if (language == SYSTEM) {
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
        val desired =
            if (language == SYSTEM) {
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
                TRAINING_WEEK -> TrainingWeekScreen(modifier = Modifier.padding(innerPadding))
                SETTINGS -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@PreviewScreenSizes
@Composable
private fun HermesAppPreview(
    @PreviewParameter(HermesAppPreviewProvider::class)
    preview: HermesAppPreviewData,
) {
    HermesTheme(darkTheme = preview.darkTheme) {
        HermesAppContent()
    }
}
