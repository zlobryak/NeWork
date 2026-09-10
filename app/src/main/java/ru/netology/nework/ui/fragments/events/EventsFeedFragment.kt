package ru.netology.nework.ui.fragments.events

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.DbError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.ui.viewmodel.EventsFeedViewModel
import ru.netology.nework.ui.adapters.eventFeed.EventsPagingAdapter



@AndroidEntryPoint
class EventsFeedFragment : Fragment(R.layout.fragment_events_feed) {

    private val viewModel: EventsFeedViewModel by viewModels()
    private lateinit var adapter: EventsPagingAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = EventsPagingAdapter(
            onLikeClick = { event -> viewModel.onEventLiked(event.id, event.likedByMe) },
            onDeleteClick = { event -> viewModel.onEventDeleted(event.id) }
        )

        recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = EventsLoadStateAdapter { adapter.retry() },
            footer = EventsLoadStateAdapter { adapter.retry() }
        )

        // Собираем поток PagingData
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventsData.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        // Отслеживаем состояние загрузки для SwipeRefreshLayout
        adapter.addLoadStateListener { loadState ->
            val isRefreshing = loadState.refresh is LoadState.Loading
            val isInitialLoading = loadState.source.refresh is LoadState.Loading

            // Уведомляем ViewModel о изменении состояния (опционально, можно управлять UI напрямую тут)
            if (!isRefreshing) {
                viewModel.onRefreshFinished()
            }

            swipeRefreshLayout.isRefreshing = isRefreshing
            progressBar.isVisible = isInitialLoading
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за SingleLiveEvent.
        // Только один observer на этот объект во всем Fragment
        viewModel.showErrorEvent.observe(viewLifecycleOwner) { error ->
            val message = when (error) {
                is NetworkError -> "Проверьте подключение к интернету"
                is ApiError -> "Ошибка сервера: ${error.status}"
                is DbError -> "Ошибка базы данных"
                is UnknownError -> "Неизвестная ошибка"
            }

            Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG).show()
        }
    }
}