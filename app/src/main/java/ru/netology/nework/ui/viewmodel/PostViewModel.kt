package ru.netology.nework.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.MediaUpload
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.data.entity.PostEntity
import ru.netology.nework.data.repository.PostRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

private val empty = PostItem(
    id = 0,
    content = "",
    authorId = 0,
    author = "",
    authorAvatar = "",
    likedByMe = false,
    attachment = null,
    authorJob = null,
    coords = null,
    likeOwnerIds = null,
    link = null,
    mentionIds = null,
    mentionedMe = false,
    published = "",
    users = null,
    ownedByMe = false,
    isDeleting = false,
    isSynced = true,
    syncStatus = PostEntity.SyncStatus.SYNCED,
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
    val data: Flow<PagingData<PostItem>> = auth.authStateFlow
        .onEach { Log.d("AUTH", "authStateFlow emitted: $it") }
        .flatMapLatest { (myId, _) ->
            repository.data.map { pagingData ->
                pagingData.map { item ->
                    item.copy(ownedByMe = item.authorId == myId.toInt())
                }
            }
        }
        .cachedIn(viewModelScope)

    // Событие для навигации на экран авторизации
    private val _navigateToLoginEvent = SingleLiveEvent<Unit>()
    val navigateToLoginEvent: LiveData<Unit> = _navigateToLoginEvent

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState


    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private val edited = MutableLiveData(empty) //TODO Делаеме редактирование
    private val _postCreated = SingleLiveEvent<Unit>()

    private val _state = MutableLiveData(FeedModelState())
    private val _successEvent = SingleLiveEvent<String>()
    val successEvent: LiveData<String> = _successEvent

    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            // repository.stream.cachedIn(viewModelScope).
            _dataState.value = FeedModelState()
        } catch (_: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let { postItem ->
            viewModelScope.launch {
                try {
                    repository.save(
                        postItem, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )

                    _postCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: PostItem) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun likePost(post: PostItem) {
        Log.d("LikeDebug", "1. Вызван likePost для id: ${post.id}, isSynced: ${post.isSynced}")
        val currentPost = post.copy()

        viewModelScope.launch {
            try {
                if (post.isSynced) {
                    Log.d("LikeDebug", "2. Вызываем repository.likePost")
                    repository.likePost(post.id, post.likedByMe)
                    Log.d("LikeDebug", "3. repository.likePost успешно завершен")
                } else {

                    Log.w("LikeDebug", "Пост не синхронизирован, прерываем")
                    _errorEvent.value = "Post is not synchronised, try later"
                }
            } catch (e: Throwable) {
                // Проверяем, является ли ошибка ApiError с кодом 403
                if (e is ApiError && e.status == 403) {
                    // Токен недействителен или отсутствует, отправляем событие навигации
                    _navigateToLoginEvent.value = Unit
                } else {
                    // Все остальные ошибки (сеть, 500 и т.д.)
                    _errorEvent.value = "Ошибка при обработке лайка: ${e.message}"
                    repository.restorePost(currentPost)
                }
            }
        }

    }

    fun removePost(post: PostItem) {
        val currentPosts = post.copy()
        viewModelScope.launch {
            try {
                repository.removeById(post.id)
                _successEvent.value = "Post deleted"
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
                repository.restorePost(currentPosts)
            }
        }
    }
}
