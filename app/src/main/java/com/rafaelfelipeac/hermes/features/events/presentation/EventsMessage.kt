package com.rafaelfelipeac.hermes.features.events.presentation

sealed class EventsMessage {
    data class Created(val title: String) : EventsMessage()

    data class Updated(val title: String) : EventsMessage()
}
