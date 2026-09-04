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
import ru.netology.nework.data.dto.job.JobItem
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
    // cachedIn: сохраняет данные в памяти при пересоздании ViewModel.
    val wallPagingData: Flow<PagingData<PostItem>> = _userId
        .filterNotNull()
        .flatMapLatest { id ->
            repository.getUserWallData(id)
        }
        .cachedIn(viewModelScope)

    // Грядет
    private val _jobsState = MutableStateFlow<Resource<JobItem>?>(null)
    val jobsState: Flow<Resource<JobItem>?> = _jobsState

    //TODO Нужно ли публичное в фрагменте?
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
        safeApiCall(
            action = { repository.getUser(userId) },
            onSuccess = { user ->
                _userState.value = Resource.Success(user)
                _uiState.value = UserUiState.Success(user)
            }
        )
    }

    private fun loadJobs(userId: Int) = viewModelScope.launch {
        safeApiCall(
            action = { repository.getJobs(userId) },
            onSuccess = { job ->
                _jobsState.value = Resource.Success(job)
            },
            onError = { errorMessage ->
                _jobsState.value = Resource.Error(errorMessage)
            })
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
        safeApiCall(
            action = { repository.likePost(post.id, post.likedByMe) },
            onSuccess = {
                // TODO обновить локальный стейт поста,
            }
        )
    }

    fun removePost(post: PostItem) = viewModelScope.launch {
        safeApiCall(
            action = { repository.removeById(post.id) },
            onSuccess = {
                // TODO инициировать обновление PagingData или локального списка.
            }
        )
    }

    private suspend fun <T> safeApiCall(
        action: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (String) -> Unit = { errorMessage ->
            _uiState.value = UserUiState.Error(errorMessage)
        }
    ) {
        try {
            val result = action()
            onSuccess(result)
        } catch (e: Exception) {
            onError(e.getErrorMessage())
        }
    }

    private fun Exception.getErrorMessage(): String = when (this) {
        is ApiError -> "Ошибка сервера: ${message}"
        is NetworkError -> "Проверьте подключение к интернету"
        is UnknownError -> "Произошла непредвиденная ошибка"
        else -> message ?: "Неизвестная ошибка"
    }

}