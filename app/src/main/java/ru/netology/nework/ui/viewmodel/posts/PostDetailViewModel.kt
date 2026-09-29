package ru.netology.nework.ui.viewmodel.posts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.data.repository.post.PostRepository
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: Int = savedStateHandle["postId"] ?: error("eventId is required")

    val postState: StateFlow<PostItem?> = postRepository.getPostById(postId)
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

    fun mentionedEvent() {
        TODO("Not yet implemented")
    }
}