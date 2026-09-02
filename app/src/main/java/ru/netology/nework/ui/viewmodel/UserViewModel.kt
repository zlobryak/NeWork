package ru.netology.nework.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {

    // Храним текущий userId в StateFlow.
    // Это позволяет реактивно перезапускать загрузку при смене пользователя.
    private val _userId = MutableStateFlow<Int?>(null)

    // Основной поток данных для стены (Paging 3)
    // flatMapLatest: если userId изменится, старый запрос отменится, и начнется новый.
    // cachedIn: сохраняет данные в памяти при пересоздании ViewModel (например, при повороте экрана).
    val wallPagingData: Flow<PagingData<PostItem>> = _userId
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getUserWallData(id)
        }
        .cachedIn(viewModelScope)

    // Грядет
    private val _jobsState = MutableStateFlow<FeedModelState>(FeedModelState())
    val jobsState: Flow<FeedModelState> = _jobsState

    private val _userState = MutableStateFlow<Resource<UserItem>?>(null)
    val userState: Flow<Resource<UserItem>?> = _userState

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: Flow<UserUiState> = _uiState

    // Метод инициализации. Теперь он просто задает ID
    fun loadUserData(userId: Int) {
        _userId.value = userId // Это автоматически запустит загрузку стены через flatMapLatest

        loadJobs(userId)
        loadUser(userId)
    }

    private fun loadUser(userId: Int) = viewModelScope.launch {
        _uiState.value = UserUiState.Loading
        try {
            val user = repository.getUser(userId)
            _userState.value = Resource.Success(user)
            _uiState.value = UserUiState.Success(user)
        } catch (e: ApiError) {
            _uiState.value = UserUiState.Error("Ошибка сервера: ${e.message}")
        } catch (e: NetworkError) {
            _uiState.value = UserUiState.Error("Проверьте подключение к интернету")
        } catch (e: UnknownError) {
            _uiState.value = UserUiState.Error("Произошла непредвиденная ошибка")
        } catch (e: Exception) {
            _uiState.value = UserUiState.Error(e.message ?: "Неизвестная ошибка")
        }
    }

    private fun loadJobs(userId: Int) = viewModelScope.launch {
//TODO
    }

    // Ваши существующие классы состояний (оставляем без изменений)
    sealed class Resource<T>(val data: T? = null, val message: String? = null) {
        class Success<T>(data: T) : Resource<T>(data)
        class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
        class Loading<T> : Resource<T>()
    }

    sealed class UserUiState {
        object Loading : UserUiState()
        data class Success(val user: UserItem) : UserUiState()
        data class Error(val message: String) : UserUiState()
    }

    fun likePost(post: PostItem) = viewModelScope.launch {
        try {
            repository.likePost(post.id, post.likedByMe)

        } catch (e: Exception) {
            //TODO Обработка ошибки like
        }
    }

    fun removePost(post: PostItem) = viewModelScope.launch {
        try {
            repository.removeById(post.id)

        } catch (e: Exception) {
            //TODO Обработка ошибки remove
        }
    }

}