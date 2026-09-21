package ru.netology.nework.ui.fragments.events

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.databinding.FragmentEventsFeedBinding
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.DbError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.ui.adapters.eventFeed.EventsLoadStateAdapter
import ru.netology.nework.ui.adapters.eventFeed.EventsPagingAdapter
import ru.netology.nework.ui.viewmodel.events.EventsFeedViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventsFeedFragment : Fragment(R.layout.fragment_events_feed) {

    private val viewModel: EventsFeedViewModel by viewModels()
    private var _binding: FragmentEventsFeedBinding? = null
    private val binding get() = _binding!!

    private var currentUserId: Int = 0
    private lateinit var adapter: EventsPagingAdapter

    @Inject
    lateinit var auth: AppAuth

    // 1. ЕДИНЫЙ слушатель для всех кликов в адаптере
    private val interactionListener = object : EventsPagingAdapter.OnInteractionListener {
        override fun onLike(event: EventItem) {
            viewModel.onEventLiked(event.id, event.likedByMe)
        }

        override fun onRemove(event: EventItem) {
            viewModel.onEventDeleted(event.id)
        }

        override fun onParticipate(event: EventItem) {
            if (currentUserId == 0) {
                navigateToLogin()
            } else {
                viewModel.participateEvent(event.id, event.participatedByMe)
            }
        }

        override fun onAuthorClick(userId: Int) {
            val action = EventsFeedFragmentDirections.actionEventsFeedFragmentToUserFragment(userId)
            findNavController().navigate(action)
        }

        override fun onOpenDetails(event: EventItem) {
            // ВАЖНО: передаем именно event.id (Int), а не весь объект event
            val action = EventsFeedFragmentDirections.actionEventsFeedFragmentToEventDetailFragment(event.id)
            findNavController().navigate(action)
        }

        override fun onEdit(event: EventItem) {
            // Передаем событие для редактирования (убедитесь, что в nav_main.xml добавлен argument eventItemArg)
            val action = EventsFeedFragmentDirections.actionEventsFragmentToNewEventFragment(event)
            findNavController().navigate(action)
        }

        override fun onShare(event: EventItem) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, event.content ?: "")
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_event)))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEventsFeedBinding.bind(view)

        currentUserId = auth.authStateFlow.value.id.toInt()
        Log.d("EVENTS_DEBUG", "Текущий ID пользователя: $currentUserId")

        setupRecyclerView()
        observeViewModel()
        setupClickListeners() // 2. ДОБАВЛЕНО: инициализация кликов (включая FAB)
    }

    private fun setupRecyclerView() {
        // 3. ИСПРАВЛЕНО: используем единый interactionListener, а не создаем новый пустой объект
        adapter = EventsPagingAdapter(
            currentUserId = currentUserId,
            onInteractionListener = interactionListener
        )

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
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

            binding.swiperefresh.isRefreshing = isRefreshing
            binding.progressBar.visibility = if (isInitialLoading) View.VISIBLE else View.GONE
        }
    }

    // 4. ДОБАВЛЕНО: обработка кликов по кнопкам фрагмента
    private fun setupClickListeners() {
        // Клик по FAB (создание нового события)
        binding.addButton.setOnClickListener {
            if (currentUserId == 0) {
                navigateToLogin()
            } else {
                // Переход на экран создания (без аргументов)
                findNavController().navigate(R.id.action_eventsFragment_to_newEventFragment)
            }
        }

        // Клик по SwipeRefreshLayout
        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_eventsFeedFragment_to_loginFragment)
    }

    private fun observeViewModel() {
        viewModel.showErrorEvent.observe(viewLifecycleOwner) { error ->
            val message = when (error) {
                is NetworkError -> "Проверьте подключение к интернету"
                is ApiError -> "Ошибка сервера: ${error.status}"
                is DbError -> "Ошибка базы данных"
                else -> "Неизвестная ошибка: ${error?.message}"
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}