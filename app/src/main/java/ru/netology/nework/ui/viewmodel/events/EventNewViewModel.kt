package ru.netology.nework.ui.viewmodel.events

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.event.IsOnline
import ru.netology.nework.data.dto.post.MediaUpload
import ru.netology.nework.data.repository.events.EventRepository
import ru.netology.nework.utils.SingleLiveEvent
import ru.netology.nework.ui.viewmodel.PhotoModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Базовое пустое состояние для нового события
private val emptyEvent = EventItem(
    id = 0,
    content = "",
    authorId = 0,
    attachment = null,
    author = "",
    authorAvatar = "",
    authorJob = "",
    coords = null,
    datetime = "",
    likeOwnerIds = emptyList(),
    likedByMe = false,
    link = null,
    participantsIds = emptyList(),
    participatedByMe = true,
    published = "",
    speakerIds = emptyList(),
    type = IsOnline.ONLINE,
    users = null,
)

private val noPhoto = PhotoModel()

@HiltViewModel
class EventNewViewModel @Inject constructor(
    private val repository: EventRepository,
    auth: AppAuth,
) : ViewModel() {

    // Событие успешного создания для навигации назад
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit> get() = _eventCreated

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> get() = _errorEvent

    // Состояние редактируемого события
    private val _edited = MutableLiveData(emptyEvent)
    val edited: LiveData<EventItem> get() = _edited

    // Состояние выбранного фото
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel> get() = _photo

    // --- Методы изменения состояния ---

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) return
        _edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    fun removePhoto() {
        _photo.value = noPhoto
        _edited.value = edited.value?.copy(attachment = null)
    }

    // Специфичные для события методы:
    fun changeDate(datetime: String) {
        _edited.value = edited.value?.copy(datetime = datetime)
    }

    // Обновление даты и времени события
    fun changeDateTime(dateTime: Date) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        _edited.value = edited.value?.copy(datetime = dateFormat.format(dateTime))
    }

    fun changeType(type: IsOnline) {
        _edited.value = edited.value?.copy(type = type)
    }

    fun changeIsOnline(type: IsOnline) {
        _edited.value = edited.value?.copy(type = type)
    }

    fun changePlace(coords: Coords?) {
        _edited.value = edited.value?.copy(coords = coords)
    }

    fun changeEventOptions(dateTime: Date, isOnline: Boolean) {
        changeDateTime(dateTime)
        changeType(if (isOnline) IsOnline.ONLINE else IsOnline.OFFLINE)
    }

    // --- Логика сохранения ---
    fun save() {
        edited.value?.let { eventItem ->
            viewModelScope.launch {
                try {
                    // Проверяем, является ли URI локальным файлом
                    val localFile = _photo.value?.uri?.takeIf { uri ->
                        uri.scheme == "content" || uri.scheme == "file"
                    }?.toFile()

                    val mediaUpload = localFile?.let { MediaUpload(it) }

                    // Вызываем метод репозитория для сохранения события
                    repository.save(eventItem, mediaUpload)

                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _errorEvent.value = "Ошибка при создании события: ${e.message}"
                }
            }
        }
        // Сбрасываем состояние после сохранения
        _edited.value = emptyEvent
        _photo.value = noPhoto
    }

    // Для редактирования
    fun edit(event: EventItem) {
        _edited.value = event
        if (event.attachment != null) {
            changePhoto(event.attachment.url.toUri())
        }
    }
}