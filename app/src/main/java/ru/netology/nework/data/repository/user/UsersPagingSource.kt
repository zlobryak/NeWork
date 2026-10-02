package ru.netology.nework.data.repository.user

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.data.dto.user.UserItem

/**
 * Пагинатор для списка пользователей.
 *
 * Используется как общим списком пользователей (вкладка "Люди" главного экрана),
 * так и локальными списками (лайкнувшие, упомянутые, спикеры, участники) —
 * для последних достаточно передать готовый список через [usersProvider].
 */
class UsersPagingSource(
    private val repository: UserRepository,
    private val usersProvider: suspend () -> List<UserItem> = { repository.loadUsersPage() },
) : PagingSource<Int, UserItem>() {

    override fun getRefreshKey(state: PagingState<Int, UserItem>): Int? =
        state.anchorPosition?.let { anchor ->
            // Смещение начала страницы, ближайшей к якорю
            state.closestPageToPosition(anchor)?.let {
                (it.prevKey?.plus(PAGE_SIZE)) ?: it.nextKey?.minus(PAGE_SIZE)
            }
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserItem> {
        val from = params.key ?: START_OFFSET
        return try {
            val data = usersProvider()
            LoadResult.Page(
                data = data,
                prevKey = if (from <= START_OFFSET) null else (from - params.loadSize).coerceAtLeast(
                    START_OFFSET
                ),
                nextKey = if (data.isEmpty()) null else from + params.loadSize,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    companion object {
        const val PAGE_SIZE = 25
        private const val START_OFFSET = 0
    }
}
