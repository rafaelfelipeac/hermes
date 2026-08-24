package com.rafaelfelipeac.hermes.features.app

import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.BROWSE
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.EVENTS
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.HOME
import com.rafaelfelipeac.hermes.core.navigation.AppDestinations.PROGRESS
import com.rafaelfelipeac.hermes.features.browse.presentation.BrowseDestination
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HermesAppContentTest {
    @Test
    fun trophySnackbar_clearsScreensWithFloatingActions() {
        assertTrue(trophySnackbarNeedsFabClearance(HOME, BrowseDestination.ROOT))
        assertTrue(trophySnackbarNeedsFabClearance(EVENTS, BrowseDestination.ROOT))
        assertTrue(trophySnackbarNeedsFabClearance(BROWSE, BrowseDestination.PERSONAL_RECORDS))
    }

    @Test
    fun trophySnackbar_usesStandardBottomPlacementWithoutFloatingAction() {
        assertFalse(trophySnackbarNeedsFabClearance(PROGRESS, BrowseDestination.ROOT))
        assertFalse(trophySnackbarNeedsFabClearance(BROWSE, BrowseDestination.PACE_CALCULATOR))
        assertFalse(trophySnackbarNeedsFabClearance(BROWSE, BrowseDestination.TROPHIES))
    }
}
