package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import com.rafaelfelipeac.hermes.core.useraction.domain.UserAction
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PACE_CALCULATOR_MODE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.USE_PACE_CALCULATOR
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode.TIME
import com.rafaelfelipeac.hermes.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaceCalculatorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun logCalculation_recordsModeForActivitiesAndTrophies() =
        runTest {
            val logger = RecordingUserActionLogger()
            val viewModel = PaceCalculatorViewModel(logger)

            viewModel.logCalculation(TIME)
            advanceUntilIdle()

            val action = logger.actions.single()
            assertEquals(USE_PACE_CALCULATOR, action.actionType)
            assertEquals(APP, action.entityType)
            assertEquals(TIME.name, action.metadata?.get(PACE_CALCULATOR_MODE))
        }

    private class RecordingUserActionLogger : UserActionLogger {
        val actions = mutableListOf<UserAction>()

        override suspend fun log(action: UserAction) {
            actions += action
        }
    }
}
