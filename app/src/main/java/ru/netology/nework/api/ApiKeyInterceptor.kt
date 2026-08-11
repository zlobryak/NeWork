package ru.netology.nework.api

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiKeyInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // Берем оригинальный запрос
        val originalRequest = chain.request()

        // Создаем новый запрос, добавляя нужный заголовок
        val newRequest = originalRequest.newBuilder()
            .header("Api-Key", "c1378193-bc0e-42c8-a502-b8d66d189617")
            .build()

        // Отправляем измененный запрос дальше в сеть
        return chain.proceed(newRequest)
    }
}