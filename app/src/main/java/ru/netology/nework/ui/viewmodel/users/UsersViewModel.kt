package ru.netology.nework.ui.viewmodel.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.userEntity.UserEntity
import ru.netology.nework.data.repository.user.UserRepository
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    /**
     * Лента пользователей.
     * Пагинация — локальная (по Room-таблице со всеми пользователями),
     * синхронизация с сервером — полная выгрузка списка одним запросом
     * GET /users при первой загрузке и принудительная перезагрузка
     * по pull-to-refresh ([refresh]).
     * cachedIn — данные переживают конфигурационные изменения экрана.
     */
    val pagingData: Flow<PagingData<UserItem>> = Pager(
        config = PagingConfig(
            pageSize = PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { userRepository.pagingSource() },
    ).flow
        .map { pagingData -> pagingData.map { it.toDto() } }
        // На время MVP: при ошибке сети показываем то, что уже есть в БД.
        .catch { emit(PagingData.empty()) }
        .cachedIn(viewModelScope)

    /** Синхронизация при первом открытии экрана (если БД пуста). */
    fun syncIfNeeded() {
        viewModelScope.launch {
            runCatching { userRepository.syncIfNeeded() }
        }
    }

    /** Принудительная полная перезагрузка списка пользователей. */
    suspend fun refresh() {
        runCatching { userRepository.refreshUsers() }
    }

    companion object {
        const val PAGE_SIZE = 20
    }
}

private fun UserEntity.toDto(): UserItem = UserItem(
    id = id,
    avatar = avatar,
    login = login,
    name = name,
)
