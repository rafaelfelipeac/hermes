package com.rafaelfelipeac.hermes.features.categories.domain.command

sealed interface CategoryCommandResult {
    data object Changed : CategoryCommandResult

    data object NoChange : CategoryCommandResult
}
