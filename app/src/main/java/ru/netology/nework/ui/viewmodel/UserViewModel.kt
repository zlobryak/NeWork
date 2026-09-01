package ru.netology.nework.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.api.WallApiService
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserViewModel @Inject constructor(
    private val wallApiService: WallApiService,
    private val repository: PostRepository,
) : ViewModel() {

    // Состояния для стены (постов)
    private val _wallState = MutableLiveData<FeedModelState>()
    val wallState: LiveData<FeedModelState> = _wallState

    // Состояния для работ (jobs)
    private val _jobsState = MutableLiveData<FeedModelState>()
    val jobsState: LiveData<FeedModelState> = _jobsState

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private val _userState = MutableLiveData<Resource<UserItem>>()
    val userState: LiveData<Resource<UserItem>> = _userState

    private val _uiState = MutableLiveData<UserUiState>()
    val uiState: LiveData<UserUiState> = _uiState

    fun loadUserData(userId: Int) {
        loadWall(userId)

        loadJobs(userId)

        _uiState.value = UserUiState.Loading

        viewModelScope.launch {
            try {
                // Вызываем метод из Repository, который может бросить ApiError
                val user = repository.getUser(userId)
                _uiState.value = UserUiState.Success(user)

            } catch (e: ApiError) {
                //TODO Обработать ошибки согласно спецификации API
                _uiState.value = UserUiState.Error("Ошибка сервера: ${e.message}")
            } catch (e: NetworkError) {
                _uiState.value = UserUiState.Error("Проверьте подключение к интернету")
            } catch (e: UnknownError) {
                _uiState.value = UserUiState.Error("Произошла непредвиденная ошибка")
            } catch (e: Exception) {
                _uiState.value = UserUiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun loadWall(userId: Int) = viewModelScope.launch {
        try {
            _wallState.value = FeedModelState(loading = true)
            val posts = repository.getPostsByUserId(userId)
//            _wallState.value = FeedModelState(data = posts )
        } catch (e: Exception) {
            _errorEvent.value = "Ошибка загрузки стены"
            _wallState.value = FeedModelState(error = true)
        }
    }

    private fun loadJobs(userId: Int) = viewModelScope.launch {
        try {
            _jobsState.value = FeedModelState(loading = true)
            val jobs = repository.getJobsByUserId(userId)
//            _jobsState.value = FeedModelState(data = /* jobs */)
        } catch (e: Exception) {
            _errorEvent.value = "Ошибка загрузки работ"
            _jobsState.value = FeedModelState(error = true)
        }
    }
}

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T> : Resource<T>()
}

// Описание всех возможных состояний экрана
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: UserItem) : UserUiState()
    data class Error(val message: String) : UserUiState()
}

//TODO Заголовок -> Имя и логин.
//TODO _wallState и _jobsState должны получать данные для feed.