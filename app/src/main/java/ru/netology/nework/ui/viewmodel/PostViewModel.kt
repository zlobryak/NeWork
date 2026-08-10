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
import ru.netology.nework.data.repository.PostRepository
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
)

private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    auth: AppAuth,
) : ViewModel() {
//    private val cached: Flow<PagingData<FeedItem>> = repository
//        .data
//        .map { pagingData ->
//            pagingData.insertSeparators(
//                generator = { before, after ->
//                    if (before?.id?.rem(5) != 0L) null else
//                        Ad(
//                            Random.nextLong(),
//                            "https://netology.ru",
//                            "figma.jpg"
//                        )
//                }
//            )
//        }
//        .cachedIn(viewModelScope)

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

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
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
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    repository.save(
                        it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
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

    fun likeById(id: Long) {
        TODO()
    }

    fun removeById(id: Long) {
        TODO()
    }
}
