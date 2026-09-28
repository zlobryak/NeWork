package ru.netology.nework.ui.viewmodel.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.error.ApiError
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: Int = savedStateHandle["eventId"] ?: error("eventId is required")

    val postState: StateFlow<EventItem?> = postRepository.getEventById(eventId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun likeEvent() {
        val post = postState.value ?: return
        viewModelScope.launch {
            postRepository.likePost(post.id, post.likedByMe)
        }
    }
}