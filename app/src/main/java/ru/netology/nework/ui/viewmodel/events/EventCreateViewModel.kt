package ru.netology.nework.ui.viewmodel.events

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.netology.nework.data.repository.events.EventRepository
import javax.inject.Inject

@HiltViewModel
class EventCreateViewModel @Inject constructor(
    private val eventRepository: EventRepository
): ViewModel() {

}
