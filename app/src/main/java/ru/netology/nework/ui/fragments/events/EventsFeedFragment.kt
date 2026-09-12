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
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.databinding.FragmentEventsFeedBinding // 1. Импортируем Binding
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.DbError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.ui.viewmodel.EventsFeedViewModel
import ru.netology.nework.ui.adapters.eventFeed.EventsPagingAdapter
import javax.inject.Inject

@AndroidEntryPoint
class EventsFeedFragment : Fragment(R.layout.fragment_events_feed) {

    private val viewModel: EventsFeedViewModel by viewModels()
    private var _binding: FragmentEventsFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: EventsPagingAdapter

    @Inject
    lateinit var auth: AppAuth

    private val currentUserId: Int = (auth.authStateFlow.value.id ?: 0) as Int

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEventsFeedBinding.bind(view)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = EventsPagingAdapter(
            currentUserId = currentUserId,
            onInteractionListener = object : EventsPagingAdapter.OnInteractionListener {
                override fun onLike(event: EventItem) {
                    viewModel.onEventLiked(event.id, event.likedByMe)
                }

                override fun onRemove(event: EventItem) {
                    viewModel.onEventDeleted(event.id)
                }

                override fun onEdit(event: EventItem) {}
                override fun onShare(event: EventItem) {}
                override fun onAuthorClick(userId: Int) {}
            }
        )

        // Используем binding для доступа к View
        binding.recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
            header = EventsLoadStateAdapter { adapter.retry() },
            footer = EventsLoadStateAdapter { adapter.retry() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventsData.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            val isRefreshing = loadState.refresh is LoadState.Loading
            val isInitialLoading = loadState.source.refresh is LoadState.Loading

            if (!isRefreshing) {
                viewModel.onRefreshFinished()
            }

            // 6. Доступ к View через binding
            binding.swipeRefreshLayout.isRefreshing = isRefreshing
            binding.progressBar.visibility = if (isInitialLoading) View.VISIBLE else View.GONE
        }

        // Не забудь настроить SwipeRefreshLayout, если он есть в макете
        binding.swipeRefreshLayout.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun observeViewModel() {
        viewModel.showErrorEvent.observe(viewLifecycleOwner) { error ->
            val message = when (error) {
                is NetworkError -> "Проверьте подключение к интернету"
                is ApiError -> "Ошибка сервера: ${error.status}"
                is DbError -> "Ошибка базы данных"
                else -> "Неизвестная ошибка: ${error?.message}" // 7. Добавлена ветка else для исчерпывающего when
            }

            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // 8. Очищаем binding для предотвращения утечек памяти
    }
}