package com.rafaelfelipeac.hermes.core.features.trainingweek.presentation

import androidx.lifecycle.ViewModel
import com.rafaelfelipeac.hermes.core.features.trainingweek.domain.repository.TrainingWeekRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrainingWeekViewModel @Inject constructor(
    private val repository: TrainingWeekRepository
) : ViewModel() {

}