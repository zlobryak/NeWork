package ru.netology.nework.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.data.dto.AuthResponse
import java.io.File

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val auth: AuthResponse) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}


@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val apiService: ApiService
): ViewModel() {

    private val _registrationState = MutableLiveData<RegistrationState>(RegistrationState.Idle)
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun register(
        login: String,
        password: String,
        name: String,
        avatarUri: Uri? = null,
        context: Context
    ) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading

            try {
                //Формируем часть с аватаркой (если выбрана)
                val avatarPart = avatarUri?.let { uri ->
                    val file = getFileFromUri(uri, context)
                    val requestFile = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }

                // Отправляем запрос через AuthApi
                val response = apiService.register(
                    login = login.toRequestBody("text/plain".toMediaType()),
                    pass = password.toRequestBody("text/plain".toMediaType()),
                    name = name.toRequestBody("text/plain".toMediaType()),
                    avatar = avatarPart
                )

                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        _registrationState.value = RegistrationState.Success(auth)
                    } ?: run {
                        _registrationState.value = RegistrationState.Error("Пустой ответ от сервера")
                    }
                } else {
                    // Парсим ошибку, если сервер вернул тело
                    val errorMessage = response.errorBody()?.string() ?: "Ошибка ${response.code()}"
                    _registrationState.value = RegistrationState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    // Вспомогательный метод: Uri → File
    private fun getFileFromUri(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Не удалось открыть Uri: $uri")

        val tempFile = File.createTempFile("avatar_", ".jpg", context.cacheDir)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}


//TODO Рефаторинг авторизации под текущий API