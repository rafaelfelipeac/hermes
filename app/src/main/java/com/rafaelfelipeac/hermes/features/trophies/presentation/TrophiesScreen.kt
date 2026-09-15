package com.rafaelfelipeac.hermes.features.trophies.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingLg
import com.rafaelfelipeac.hermes.core.ui.theme.Dimens.SpacingXl

@Composable
fun TrophiesScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    requestedTrophyStableId: String? = null,
    onRequestedTrophyConsumed: () -> Unit = {},
    onOpenActivities: () -> Unit = {},
    viewModel: TrophyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var selectedFamilyName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTrophyId by rememberSaveable { mutableStateOf<String?>(null) }
    var overviewFirstVisibleItemIndex by rememberSaveable { mutableIntStateOf(0) }
    var overviewFirstVisibleItemScrollOffset by rememberSaveable { mutableIntStateOf(0) }
    val familyFirstVisibleItemIndex = remember { mutableStateMapOf<String, Int>() }
    val familyFirstVisibleItemScrollOffset = remember { mutableStateMapOf<String, Int>() }
    val selectedFamily = state.families.firstOrNull { it.family.name == selectedFamilyName }
    val selectedTrophy =
        state.families
            .flatMap { it.sections }
            .flatMap { it.trophies }
            .firstOrNull { it.stableId == selectedTrophyId }
    val requestedTrophy =
        requestedTrophyStableId?.let { requestedId ->
            state.families
                .flatMap { it.sections }
                .flatMap { it.trophies }
                .firstOrNull { it.stableId == requestedId }
        }

    BackHandler(enabled = selectedFamily != null || onBack != null) {
        if (selectedFamily != null) {
            selectedFamilyName = null
        } else {
            onBack?.invoke()
        }
    }

    LaunchedEffect(requestedTrophy?.stableId) {
        if (requestedTrophy != null) {
            selectedFamilyName = requestedTrophy.family.name
            selectedTrophyId = requestedTrophy.stableId
            onRequestedTrophyConsumed()
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SpacingLg),
    ) {
        TrophiesHeader(
            familySection = selectedFamily,
            onBack = { selectedFamilyName = null },
            onBrowseBack = onBack,
            onOpenActivities = onOpenActivities,
        )

        TrophiesContent(
            state = state,
            selectedFamilyName = selectedFamilyName,
            selectedTrophyId = selectedTrophyId,
            onFamilySelected = { family -> selectedFamilyName = family.name },
            onBackFromFamily = { selectedFamilyName = null },
            onTrophySelected = { selectedTrophyId = it.stableId },
            overviewFirstVisibleItemIndex = overviewFirstVisibleItemIndex,
            overviewFirstVisibleItemScrollOffset = overviewFirstVisibleItemScrollOffset,
            onOverviewScrollChanged = { index, offset ->
                overviewFirstVisibleItemIndex = index
                overviewFirstVisibleItemScrollOffset = offset
            },
            familyFirstVisibleItemIndex = familyFirstVisibleItemIndex,
            familyFirstVisibleItemScrollOffset = familyFirstVisibleItemScrollOffset,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = SpacingXl)
                    .padding(bottom = SpacingXl),
        )
    }

    selectedTrophy?.let { trophy ->
        val selectedTrophyName = stringResource(trophyNameRes(trophy.trophyId))
        TrophyDetailDialog(
            trophy = trophy,
            onShare = { viewModel.logShareTrophy(trophy, selectedTrophyName) },
            onDismiss = { selectedTrophyId = null },
        )
    }
}
