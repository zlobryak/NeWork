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
import ru.netology.nework.data.dto.post.AuthResponse
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
) : ViewModel() {

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
                // Формируем часть для аватара
                val avatarPart = if (avatarUri != null) {
                    val file = getFileFromUri(avatarUri, context)
                    val mimeType = context.contentResolver.getType(avatarUri) ?: "image/jpeg"
                    val requestFile = file.asRequestBody(mimeType.toMediaType())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                } else {
                    //Создаем часть без имени файла (null) и без типа контента (null).

                    val emptyBody = "".toRequestBody(null)
                    MultipartBody.Part.createFormData("file", null, emptyBody)
                }

                // Отправляем запрос
                val response = apiService.register(
                    login = login,
                    pass = password,
                    name = name,
                    avatar = avatarPart
                )

                if (response.isSuccessful) {
                    response.body()?.let { auth ->
                        _registrationState.value = RegistrationState.Success(auth)
                    } ?: run {
                        _registrationState.value =
                            RegistrationState.Error("Пустой ответ от сервера")
                    }
                } else {
                    // Формируем понятное сообщение для пользователя (для Toast)
                    val userErrorMessage = when (response.code()) {
                        400 -> "Пользователь с таким логином уже зарегистрирован"
                        415 -> "Неподдерживаемый формат файла. Используйте JPEG или PNG"
                        else -> "Ошибка сервера: ${response.code()}"
                    }
                    // Отправляем состояние ошибки во Fragment (там сработает короткий Toast)
                    _registrationState.value = RegistrationState.Error(userErrorMessage)
                }
            } catch (e: Exception) {
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

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