package ru.netology.nework.ui.viewmodel

data class FeedModelState(
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val isRefreshing: Boolean = false,
)
