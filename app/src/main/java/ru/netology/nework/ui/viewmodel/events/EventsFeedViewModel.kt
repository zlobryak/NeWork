package ru.netology.nework.ui.viewmodel.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.repository.events.EventRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

data class EventsFeedUiState(
    val isRefreshing: Boolean = false, // Для свайпа вниз (SwipeRefreshLayout)
    val isInitialLoading: Boolean = true // Для показа первичного прогресс-бара
)

@HiltViewModel
class EventsFeedViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val appAuth: AppAuth
) : ViewModel() {

    // Поток данных для RecyclerView (Paging 3)
    // cachedIn обязателен, чтобы данные сохранялись при повороте экрана
    val eventsData: Flow<PagingData<EventItem>> = eventRepository.getAllEventsData
        .cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(EventsFeedUiState())
    val uiState: StateFlow<EventsFeedUiState> = _uiState.asStateFlow()

    val showErrorEvent = SingleLiveEvent<AppError>()

    val isAuthorized: StateFlow<Boolean> = appAuth.authStateFlow.map { authState ->
        authState.token != null || authState.id != 0L
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)



    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }

        // Реальный рефреш Paging делает adapter.refresh() во Fragment/Activity.
        // Здесь мы просто сбрасываем флаг после небольшой задержки или по сигналу от адаптера.
    }

    fun onRefreshFinished() {
        _uiState.update { it.copy(isRefreshing = false, isInitialLoading = false) }
    }

    fun onEventLiked(eventId: Int, currentlyLiked: Boolean) {
        viewModelScope.launch {
            try {
                // Вызываем репозиторий. Он обновит БД, и Paging автоматически
                // переиздаст обновленный список.
                eventRepository.likeEvent(eventId, currentlyLiked)
            } catch (e: Exception) {
                val appError = AppError.from(e)
                showErrorEvent.value = appError
            }
        }
    }

    fun onEventDeleted(eventId: Int) {
        viewModelScope.launch {
            try {
                eventRepository.removeById(eventId)
            } catch (e: Exception) {
                val appError = AppError.from(e)
                showErrorEvent.value = appError
            }
        }
    }

    fun participateEvent(id: Int, isPartByMe: Boolean) {

        viewModelScope.launch {
            try {
                eventRepository.participateEvent(id, isPartByMe)
            } catch (_: ApiError) {
                // TODO Обработка ошибок
            }
        }
    }
}