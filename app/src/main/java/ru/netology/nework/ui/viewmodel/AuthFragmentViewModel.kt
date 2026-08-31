package ru.netology.nework.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.repository.post.PostRepository


@HiltViewModel
class AuthFragmentViewModel @Inject constructor(
    private val appAuth: AppAuth,
    private val apiService: ApiService,
    private val repository: PostRepository
) : ViewModel() {

    // Приватный MutableLiveData для изменения состояния внутри ViewModel
    private val _loginState = MutableLiveData<LoginState>(LoginState.Idle)
    // Публичный LiveData для наблюдения из Fragment
    val loginState: LiveData<LoginState> = _loginState


    /**
     * Метод авторизации
     * Вызывается из Fragment при нажатии на кнопку "Войти"
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            try {
                val response = apiService.authenticate(username, password)

                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    // Сохраняем токен и ID в хранилище
                    appAuth.setAuth(authData.id, authData.token)

                    _loginState.value = LoginState.Success
                } else {
                    // Обработка ошибок от сервера
                    _loginState.value = LoginState.Error(
                        response.message() ?: "Ошибка авторизации"
                    )
                }
            } catch (e: Exception) {
                //  Обработка ошибки
                _loginState.value = LoginState.Error(e.message ?: "Неизвестная ошибка")
                //TODO Добавить обработки ошибок из документации
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
    //Запрашивает новые посты при login
    suspend fun refresh(){
      // TODO  repository.getAllVisible()

    }
}

sealed class LoginState {
    object Idle : LoginState()              // Начальное состояние
    object Loading : LoginState()           // Идёт запрос
    object Success : LoginState()           // Успех
    data class Error(val message: String) : LoginState()  // Ошибка с сообщением
}


//TODO Рефаторинг авторизации под текущий API