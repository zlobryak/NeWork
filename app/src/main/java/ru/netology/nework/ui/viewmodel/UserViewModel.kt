package ru.netology.nework.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.PostRepository
import ru.netology.nework.utils.SingleLiveEvent
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class UserViewModel @Inject constructor(
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

    fun loadUserData(userId: Int) {
        loadWall(userId)
        loadJobs(userId)
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

//TODO Заголовок -> Имя и логин.
//TODO _wallState и _jobsState должны получать данные для feed.