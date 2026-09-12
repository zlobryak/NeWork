package ru.netology.nework.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.repository.events.EventRepository
import ru.netology.nework.error.AppError
import ru.netology.nework.error.AppError.Companion.from
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

data class EventsFeedUiState(
    val isRefreshing: Boolean = false, // Для свайпа вниз (SwipeRefreshLayout)
    val isInitialLoading: Boolean = true // Для показа первичного прогресс-бара
)

@HiltViewModel
class EventsFeedViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    // Поток данных для RecyclerView (Paging 3)
    // cachedIn обязателен, чтобы данные сохранялись при повороте экрана
    val eventsData: Flow<PagingData<EventItem>> = eventRepository.getAllEventsData
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(EventsFeedUiState())
    val uiState: StateFlow<EventsFeedUiState> = _uiState.asStateFlow()

    val showErrorEvent = SingleLiveEvent<AppError>()


    /**
     * Принудительное обновление списка
     */
    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }

        // Реальный рефреш Paging делает adapter.refresh() во Fragment/Activity.
        // Здесь мы просто сбрасываем флаг после небольшой задержки или по сигналу от адаптера.
    }

    fun onRefreshFinished() {
        _uiState.update { it.copy(isRefreshing = false, isInitialLoading = false) }
    }

    /**
     * Обработка лайка/дизлайка события
     */
    fun onEventLiked(eventId: Int, currentlyLiked: Boolean) {
        viewModelScope.launch {
            try {
                // Вызываем репозиторий. Он обновит БД, и Paging автоматически
                // переиздаст обновленный список.
                eventRepository.likeEvent(eventId, currentlyLiked)
            } catch (e: Exception) {
                val appError = from(e)
                showErrorEvent.value = appError
            }
        }
    }

    /**
     * Обработка удаления события
     */
    fun onEventDeleted(eventId: Int) {
        viewModelScope.launch {
            try {
                eventRepository.removeById(eventId)
            } catch (e: Exception) {
                val appError = from(e)
                showErrorEvent.value = appError
            }
        }
    }
}