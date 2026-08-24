package com.rafaelfelipeac.hermes.features.pacecalculator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rafaelfelipeac.hermes.core.useraction.domain.UserActionLogger
import com.rafaelfelipeac.hermes.core.useraction.metadata.UserActionMetadataKeys.PACE_CALCULATOR_MODE
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionEntityType.APP
import com.rafaelfelipeac.hermes.core.useraction.model.UserActionType.USE_PACE_CALCULATOR
import com.rafaelfelipeac.hermes.features.pacecalculator.domain.PaceCalculatorMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaceCalculatorViewModel
    @Inject
    constructor(
        private val userActionLogger: UserActionLogger,
    ) : ViewModel() {
        fun logCalculation(mode: PaceCalculatorMode) {
            viewModelScope.launch {
                userActionLogger.log(
                    actionType = USE_PACE_CALCULATOR,
                    entityType = APP,
                    metadata = mapOf(PACE_CALCULATOR_MODE to mode.name),
                )
            }
        }
    }
