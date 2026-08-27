package com.rafaelfelipeac.hermes.features.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.BROWSE
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.EVENTS
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.HOME
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.PROGRESS
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingSm
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.TrophySnackbarFabClearance
import com.rafaelfelipeac.hermes.features.activity.presentation.model.ActivityItemUi
import com.rafaelfelipeac.hermes.features.browse.presentation.BrowseDestination
import com.rafaelfelipeac.hermes.features.browse.presentation.BrowseScreen
import com.rafaelfelipeac.hermes.features.events.presentation.EventsScreen
import com.rafaelfelipeac.hermes.features.events.presentation.model.EventDialogDraft
import com.rafaelfelipeac.hermes.features.progress.presentation.ProgressNextFocusUi
import com.rafaelfelipeac.hermes.features.progress.presentation.ProgressScreen
import com.rafaelfelipeac.hermes.features.progress.presentation.ProgressUpcomingEventUi
import com.rafaelfelipeac.hermes.features.settings.presentation.SettingsViewModel
import com.rafaelfelipeac.hermes.features.trophies.presentation.FeaturedTrophyUi
import com.rafaelfelipeac.hermes.features.trophies.presentation.TrophyCelebrationViewModel
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.WeeklyTrainingScreen
import com.rafaelfelipeac.hermes.features.weeklytraining.presentation.model.WorkoutDialogDraft
import java.time.LocalDate

@Composable
fun HermesAppContent() {
    val trophyCelebrationViewModel: TrophyCelebrationViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentDestination by rememberSaveable { mutableStateOf(HOME) }
    var currentBrowseDestination by rememberSaveable { mutableStateOf(BrowseDestination.ROOT) }
    var browseOriginTab by rememberSaveable { mutableStateOf<AppDestinations?>(null) }
    var browseParentDestination by rememberSaveable { mutableStateOf(BrowseDestination.ROOT) }
    var pendingWorkoutDraft by rememberSaveable(stateSaver = WorkoutDialogDraft.Saver) {
        mutableStateOf<WorkoutDialogDraft?>(null)
    }
    var pendingEventDraft by rememberSaveable(stateSaver = EventDialogDraft.Saver) {
        mutableStateOf<EventDialogDraft?>(null)
    }
    var pendingRequestedWorkoutId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingRequestedWorkoutDate by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingRequestedWorkoutRequestKey by rememberSaveable { mutableStateOf(0L) }
    var pendingRequestedEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingRequestedActivityId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingRequestedTrophyStableId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCelebrationTrophyStableId by rememberSaveable { mutableStateOf<String?>(null) }
    val visibleDestinations = listOf(HOME, PROGRESS, EVENTS, BROWSE)
    val showBottomNavigation = currentDestination != BROWSE || currentBrowseDestination == BrowseDestination.ROOT
    val trophySnackbarNeedsFabClearance =
        trophySnackbarNeedsFabClearance(
            currentDestination = currentDestination,
            currentBrowseDestination = currentBrowseDestination,
        )
    val trophyViewActionLabel = stringResource(com.rafaelfelipeac.hermes.R.string.trophies_view_action)

    fun resetBrowseNavigation() {
        currentBrowseDestination = BrowseDestination.ROOT
        browseOriginTab = null
        browseParentDestination = BrowseDestination.ROOT
    }

    fun openBrowseDestination(
        destination: BrowseDestination,
        originTab: AppDestinations?,
        parentDestination: BrowseDestination = BrowseDestination.ROOT,
    ) {
        currentDestination = BROWSE
        currentBrowseDestination = destination
        browseOriginTab = originTab
        browseParentDestination = parentDestination
    }

    fun navigateToBrowse(destination: BrowseDestination) {
        val originTab = if (currentDestination == BROWSE) null else currentDestination
        val parentDestination =
            if (currentDestination == BROWSE) {
                currentBrowseDestination
            } else {
                BrowseDestination.ROOT
            }

        openBrowseDestination(destination, originTab = originTab, parentDestination = parentDestination)
    }
    val openProgressEvent: (ProgressUpcomingEventUi) -> Unit = { event ->
        pendingWorkoutDraft = null
        pendingRequestedWorkoutId = null
        pendingRequestedWorkoutDate = null
        pendingRequestedTrophyStableId = null
        pendingEventDraft = null
        pendingRequestedEventId = event.id
        pendingRequestedActivityId = null
        currentDestination = EVENTS
    }
    val openProgressWorkout: (ProgressNextFocusUi) -> Unit = { workout ->
        pendingWorkoutDraft = null
        pendingRequestedWorkoutId = workout.id
        pendingRequestedWorkoutDate = workout.date?.toString()
        pendingRequestedWorkoutRequestKey += 1L
        pendingRequestedTrophyStableId = null
        pendingRequestedEventId = null
        pendingRequestedActivityId = null
        currentDestination = HOME
    }
    val openProgressActivityItem: (ActivityItemUi) -> Unit = { item ->
        pendingRequestedWorkoutId = null
        pendingRequestedWorkoutDate = null
        pendingRequestedActivityId = item.id
        pendingRequestedEventId = null
        pendingRequestedTrophyStableId = null
        navigateToBrowse(BrowseDestination.ACTIVITIES)
    }
    val openProgressTrophy: (FeaturedTrophyUi) -> Unit = { trophy ->
        pendingRequestedWorkoutId = null
        pendingRequestedWorkoutDate = null
        pendingCelebrationTrophyStableId = null
        pendingRequestedTrophyStableId = trophy.trophy.stableId
        pendingRequestedEventId = null
        pendingRequestedActivityId = null
        navigateToBrowse(BrowseDestination.TROPHIES)
    }
    val openCategoriesBrowse: (WorkoutDialogDraft) -> Unit = { draft ->
        pendingEventDraft = null
        pendingWorkoutDraft = draft
        pendingRequestedWorkoutId = null
        pendingRequestedWorkoutDate = null
        pendingRequestedTrophyStableId = null
        pendingRequestedEventId = null
        pendingRequestedActivityId = null
        navigateToBrowse(BrowseDestination.CATEGORIES)
    }
    val openEventCategories: (EventDialogDraft) -> Unit = { draft ->
        pendingWorkoutDraft = null
        pendingEventDraft = draft
        pendingRequestedWorkoutId = null
        pendingRequestedWorkoutDate = null
        pendingRequestedTrophyStableId = null
        pendingRequestedEventId = null
        pendingRequestedActivityId = null
        navigateToBrowse(BrowseDestination.CATEGORIES)
    }
    val onBrowseBack = {
        when (currentBrowseDestination) {
            BrowseDestination.ACTIVITIES ->
                if (browseParentDestination != BrowseDestination.ROOT) {
                    currentBrowseDestination = browseParentDestination
                    browseParentDestination = BrowseDestination.ROOT
                } else if (browseOriginTab != null) {
                    currentDestination = browseOriginTab!!
                    resetBrowseNavigation()
                } else {
                    currentBrowseDestination = BrowseDestination.ROOT
                }

            else ->
                if (browseOriginTab != null) {
                    currentDestination = browseOriginTab!!
                    resetBrowseNavigation()
                } else {
                    currentBrowseDestination = BrowseDestination.ROOT
                }
        }
    }

    BackHandler(
        enabled = currentDestination == BROWSE && currentBrowseDestination != BrowseDestination.ROOT,
    ) {
        onBrowseBack()
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
                navigateToBrowse(BrowseDestination.TROPHIES)
            }
        }
    }

    @Composable
    fun ShellContent(contentPadding: PaddingValues) {
        Box(modifier = Modifier.fillMaxSize().padding(contentPadding)) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                when (currentDestination) {
                    HOME ->
                        WeeklyTrainingScreen(
                            modifier = Modifier.padding(innerPadding),
                            onManageCategories = openCategoriesBrowse,
                            pendingWorkoutDraft = pendingWorkoutDraft,
                            onWorkoutDraftConsumed = { pendingWorkoutDraft = null },
                            requestedWorkoutId = pendingRequestedWorkoutId,
                            requestedWorkoutDate = pendingRequestedWorkoutDate?.let(LocalDate::parse),
                            requestedWorkoutRequestKey = pendingRequestedWorkoutRequestKey,
                            onRequestedWorkoutDateConsumed = { pendingRequestedWorkoutDate = null },
                            onRequestedWorkoutConsumed = { pendingRequestedWorkoutId = null },
                        )
                    PROGRESS ->
                        ProgressScreen(
                            modifier = Modifier.padding(innerPadding),
                            onOpenActivity = { navigateToBrowse(BrowseDestination.ACTIVITIES) },
                            onOpenActivityItem = openProgressActivityItem,
                            onOpenWorkout = openProgressWorkout,
                            onOpenEvent = openProgressEvent,
                            onOpenTrophy = openProgressTrophy,
                        )
                    EVENTS ->
                        EventsScreen(
                            modifier = Modifier.padding(innerPadding),
                            requestedEventId = pendingRequestedEventId,
                            onRequestedEventConsumed = { pendingRequestedEventId = null },
                            onManageCategories = openEventCategories,
                            pendingEventDraft = pendingEventDraft,
                            onEventDraftConsumed = { pendingEventDraft = null },
                            weekStartDay = settingsState.weekStartDay,
                        )
                    BROWSE ->
                        BrowseScreen(
                            modifier = Modifier.padding(innerPadding),
                            route = currentBrowseDestination,
                            settingsState = settingsState,
                            settingsViewModel = settingsViewModel,
                            requestedActivityId = pendingRequestedActivityId,
                            onRequestedActivityConsumed = { pendingRequestedActivityId = null },
                            requestedTrophyStableId =
                                pendingRequestedTrophyStableId ?: pendingCelebrationTrophyStableId,
                            onRequestedTrophyConsumed = {
                                pendingRequestedTrophyStableId = null
                                pendingCelebrationTrophyStableId = null
                            },
                            onNavigateTo = { destination ->
                                when (destination) {
                                    BrowseDestination.ROOT -> {
                                        currentBrowseDestination = BrowseDestination.ROOT
                                        browseOriginTab = null
                                        browseParentDestination = BrowseDestination.ROOT
                                    }
                                    BrowseDestination.ACTIVITIES ->
                                        navigateToBrowse(BrowseDestination.ACTIVITIES)
                                    else -> navigateToBrowse(destination)
                                }
                            },
                            onBack = onBrowseBack,
                        )
                }
            }

            SnackbarHost(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .imePadding()
                        .then(
                            if (showBottomNavigation) {
                                Modifier
                            } else {
                                Modifier.navigationBarsPadding()
                            },
                        )
                        .padding(
                            bottom =
                                if (trophySnackbarNeedsFabClearance) {
                                    TrophySnackbarFabClearance
                                } else {
                                    SpacingSm
                                },
                        ),
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

    if (showBottomNavigation) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                visibleDestinations.forEach {
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
                            if (it == BROWSE) {
                                resetBrowseNavigation()
                                currentDestination = BROWSE
                            } else if (
                                currentDestination == BROWSE &&
                                currentBrowseDestination != BrowseDestination.ROOT
                            ) {
                                pendingWorkoutDraft = null
                                pendingEventDraft = null
                                resetBrowseNavigation()
                                currentDestination = it
                            } else {
                                resetBrowseNavigation()
                                currentDestination = it
                            }
                        },
                    )
                }
            },
        ) {
            ShellContent(PaddingValues())
        }
    } else {
        ShellContent(PaddingValues())
    }
}

internal fun trophySnackbarNeedsFabClearance(
    currentDestination: AppDestinations,
    currentBrowseDestination: BrowseDestination,
): Boolean =
    currentDestination == HOME ||
        currentDestination == EVENTS ||
        (
            currentDestination == BROWSE &&
                (
                    currentBrowseDestination == BrowseDestination.CATEGORIES ||
                        currentBrowseDestination == BrowseDestination.PERSONAL_RECORDS ||
                        currentBrowseDestination == BrowseDestination.CHALLENGES
                )
        )
