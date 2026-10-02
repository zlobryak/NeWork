package ru.netology.nework.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.databinding.FragmentUsersFeedBinding
import ru.netology.nework.ui.adapters.usersFeed.UsersLoadStateAdapter
import ru.netology.nework.ui.adapters.usersFeed.UsersPagingAdapter
import ru.netology.nework.ui.viewmodel.users.UsersViewModel

/**
 * Третий цветовой экран главного экрана — список пользователей (вкладка "Люди").
 *
 * API отдаёт всех пользователей только одним запросом GET /users (без пачек),
 * поэтому полный список предварительно сохраняется в Room, а список листается
 * локально через Paging (см. UsersViewModel / UserRepository).
 *
 * Клик по карточке пользователя ведёт к детальному виду UserFragment.
 */
@AndroidEntryPoint
class UsersFeedFragment : Fragment(R.layout.fragment_users_feed) {

    private val viewModel: UsersViewModel by viewModels()
    private var _binding: FragmentUsersFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UsersPagingAdapter

    // Единый слушатель кликов по карточкам
    private val interactionListener = object : UsersPagingAdapter.OnInteractionListener {
        override fun onUserClick(user: UserItem) {
            val action = UsersFeedFragmentDirections
                .actionUsersFeedFragmentToUserFragment(user.id)
            findNavController().navigate(action)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUsersFeedBinding.bind(view)

        setupRecyclerView()
        setupSwipeRefresh()

        // При первом открытии скачиваем всех пользователей в БД (если она пуста)
        viewModel.syncIfNeeded()
    }

    private fun setupRecyclerView() {
        adapter = UsersPagingAdapter(interactionListener)

        binding.list.adapter = adapter.withLoadStateFooter(
            footer = UsersLoadStateAdapter { adapter.retry() }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pagingData.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }

        adapter.addLoadStateListener { loadState ->
            // Индикатор pull-to-refresh
            binding.swiperefresh.isRefreshing =
                loadState.refresh is LoadState.Loading

            // Прогресс первой загрузки, когда данных ещё нет
            val isFirstLoading =
                loadState.source.refresh is LoadState.Loading && adapter.itemCount == 0
            binding.progressBar.visibility =
                if (isFirstLoading) View.VISIBLE else View.GONE

            // Пустой список
            val isEmpty = loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
            binding.empty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun setupSwipeRefresh() {
        binding.swiperefresh.setOnRefreshListener {
            // Принудительно перезагружаем весь список с сервера и обновляем Pager
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.refresh()
                adapter.refresh()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
