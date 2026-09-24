package ru.netology.nework.ui.viewmodel.events

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.post.MediaUpload
import ru.netology.nework.data.repository.events.EventRepository
import ru.netology.nework.ui.viewmodel.PhotoModel
import ru.netology.nework.utils.SingleLiveEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import androidx.core.net.toFile

private val noPhoto = PhotoModel()

@HiltViewModel
class NewEventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val auth: AppAuth,
) : ViewModel() {

    // Состояние редактируемого события (аналогично _edited в PostViewModel)
    private val _edited = MutableLiveData<EventItem>()
    val edited: LiveData<EventItem>
        get() = _edited

    // Локально выбранное фото (аналогично photo в PostViewModel)
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String>
        get() = _errorEvent

    // Событие успешного создания -> навигация назад
    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    fun changeContent(content: String) {
        val text = content.trim()
        if (_edited.value?.content == text) {
            return
        }
        _edited.value = (_edited.value ?: emptyEvent()).copy(content = text)
    }

    fun changePhoto(uri: Uri?) {
        _photo.value = PhotoModel(uri)
    }

    // Сохранение — точная копия логики PostViewModel.save():
    // если URI локальный (content:// или file://) — сначала загружаем изображение,
    // затем отправляем событие с вложением
    fun save() {
        _edited.value?.let { eventItem ->
            viewModelScope.launch {
                try {
                    val localFile = _photo.value?.uri?.takeIf { uri ->
                        uri.scheme == "content" || uri.scheme == "file"
                    }?.toFile()

                    val mediaUpload = localFile?.let { MediaUpload(it) }

                    repository.save(eventItem, mediaUpload)

                    _eventCreated.value = Unit
                } catch (e: Exception) {
                    e.printStackTrace()
                    _errorEvent.value = e.message ?: "Не удалось сохранить событие"
                }
            }
        }
    }

    private fun emptyEvent(): EventItem = EventItem(
        authorId = auth.authStateFlow.value.id.toInt(),
        attachment = null,
        author = "",
        authorAvatar = "",
        content = "",
        datetime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
            .format(Date()),
        id = 0,
        likeOwnerIds = emptyList(),
        likedByMe = false,
        participantsIds = emptyList(),
        participatedByMe = false,
        published = "",
        speakerIds = emptyList(),
    )
}
