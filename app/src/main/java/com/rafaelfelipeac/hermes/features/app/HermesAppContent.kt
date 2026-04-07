package com.rafaelfelipeac.hermes.features.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.ACTIVITY
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.SETTINGS
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.TROPHIES
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.WEEKLY_TRAINING
import com.rafaelfelipeac.hermes.features.activity.presentation.ActivityScreen
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsRoute
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsRoute.CATEGORIES
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsRoute.MAIN
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsScreen
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCelebrationViewModel
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophiesScreen
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.WeeklyTrainingScreen
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft

@Composable
fun HermesAppContent() {
    val trophyCelebrationViewModel: TrophyCelebrationViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentDestination by rememberSaveable { mutableStateOf(WEEKLY_TRAINING) }
    var pendingSettingsRoute by rememberSaveable { mutableStateOf<SettingsRoute?>(null) }
    var pendingWorkoutDraft by remember { mutableStateOf<WorkoutDialogDraft?>(null) }
    var pendingCelebrationTrophyStableId by rememberSaveable { mutableStateOf<String?>(null) }
    val trophyViewActionLabel = stringResource(com.rafaelfelipeac.hermes.R.string.trophies_view_action)
    val openCategoriesSettings: (WorkoutDialogDraft) -> Unit = { draft ->
        pendingWorkoutDraft = draft
        pendingSettingsRoute = CATEGORIES
        currentDestination = SETTINGS
    }
    val handleCategoriesExit = {
        if (pendingWorkoutDraft != null) {
            currentDestination = WEEKLY_TRAINING
        }
    }

    LaunchedEffect(trophyCelebrationViewModel) {
        trophyCelebrationViewModel.events.collect { celebration ->
            trophyCelebrationViewModel.markCelebrationSeen(celebration.token)
            snackbarHostState.currentSnackbarData?.dismiss()
            val result =
                snackbarHostState.showSnackbar(
                message = celebration.message,
                actionLabel = trophyViewActionLabel,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                pendingCelebrationTrophyStableId = celebration.trophyStableId
                currentDestination = TROPHIES
            }
        }
    }

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
                    onClick = {
                        if (it == SETTINGS) {
                            pendingSettingsRoute = MAIN
                        }
                        currentDestination = it
                    },
                )
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (currentDestination) {
                    WEEKLY_TRAINING ->
                        WeeklyTrainingScreen(
                            modifier = Modifier.padding(innerPadding),
                            onManageCategories = openCategoriesSettings,
                            pendingWorkoutDraft = pendingWorkoutDraft,
                            onWorkoutDraftConsumed = { pendingWorkoutDraft = null },
                        )
                    ACTIVITY -> ActivityScreen(modifier = Modifier.padding(innerPadding))
                    TROPHIES ->
                        TrophiesScreen(
                            modifier = Modifier.padding(innerPadding),
                            requestedTrophyStableId = pendingCelebrationTrophyStableId,
                            onRequestedTrophyConsumed = { pendingCelebrationTrophyStableId = null },
                        )
                    SETTINGS ->
                        SettingsScreen(
                            modifier = Modifier.padding(innerPadding),
                            initialRoute = pendingSettingsRoute,
                            onRouteConsumed = { pendingSettingsRoute = null },
                            onExitCategories = handleCategoriesExit,
                        )
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = colorScheme.surfaceVariant,
                    contentColor = colorScheme.onSurfaceVariant,
                    actionColor = colorScheme.primary,
                )
            }
        }
    }
}
