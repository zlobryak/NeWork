package ru.netology.nework.data.repository.user

import androidx.paging.PagingSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.netology.nework.api.UserApiService
import ru.netology.nework.data.dao.userDao.UserDao
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.entity.userEntity.UserEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val userDao: UserDao,
) : UserRepository {

    /**
     * Получение одного пользователя: сначала из локальной копии всего списка,
     * при отсутствии — одиночным запросом с сохранением в БД.
     */
    override suspend fun getUser(userId: Int): UserItem {
        userDao.getById(userId)?.let { return it.toDto() }
        try {
            val response = userApiService.getUser(userId)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val user = response.body() ?: throw ApiError(response.code(), response.message())
            userDao.insertAll(listOf(UserEntity.fromDto(user)))
            return user
        } catch (e: AppError) {
            throw e
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun loadUsersPage(): List<UserItem> {
        try {
            val response = userApiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: AppError) {
            throw e
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    /** Локальный источник страниц (таблица со всеми пользователями). */
    override fun pagingSource(): PagingSource<Int, UserEntity> = userDao.pagingSource()

    /** Принудительная полная перезагрузка списка. */
    override suspend fun refreshUsers() = withContext(Dispatchers.IO) {
        userDao.removeAll()
        downloadAllUsers()
    }

    /** Синхронизация при первом открытии экрана: скачать всех, если БД пуста. */
    override suspend fun syncIfNeeded() {
        if (userDao.count() > 0) return
        downloadAllUsers()
    }

    /**
     * Скачивает ВСЕХ пользователей одним запросом GET /users
     * и сохраняет полный список в БД.
     */
    private suspend fun downloadAllUsers() {
        try {
            val response = userApiService.getAllUsers()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val users = response.body() ?: throw ApiError(response.code(), response.message())
            userDao.insertAll(users.map(UserEntity::fromDto))
        } catch (e: AppError) {
            throw e
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }
}