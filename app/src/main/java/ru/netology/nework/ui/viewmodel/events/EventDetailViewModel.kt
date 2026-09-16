package ru.netology.nework.ui.viewmodel.events

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.repository.events.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Int = savedStateHandle["eventId"] ?: error("eventId is required")

    val eventState: StateFlow<EventItem?> = eventRepository.getEventById(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun likeEvent() {
        val event = eventState.value ?: return
        viewModelScope.launch {
            eventRepository.likeEvent(event.id, event.likedByMe)
        }
    }
}