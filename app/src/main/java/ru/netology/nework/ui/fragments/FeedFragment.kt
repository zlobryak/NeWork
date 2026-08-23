package ru.netology.nework.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.data.repository.PostRepository
import ru.netology.nework.R
import ru.netology.nework.ui.adapter.FeedAdapter
import ru.netology.nework.ui.adapter.PagingLoadStateAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.ui.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {
    @Inject
    lateinit var repository: PostRepository

    @Inject
    lateinit var auth: AppAuth
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.post {
            Log.e("RECYCLER_SIZE", "Ширина: ${binding.list.width}, Высота: ${binding.list.height}")
        }

        val adapter = FeedAdapter(object : FeedAdapter.OnInteractionListener {
            override fun onEdit(post: PostItem) {
                viewModel.edit(post)
            }

            override fun onLike(post: PostItem) {
                // 2. Второй лог. Если он есть, а первого нет -> странно. Если его нет -> проблема в передаче слушателя
                Log.d(
                    "LIKE_DEBUG",
                    "Fragment получил onLike для Post ID: ${post.id}, isSynced: ${post.isSynced}"
                )
                viewModel.likePost(post)
            }

            override fun onRemove(post: PostItem) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: PostItem) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.START or ItemTouchHelper.END
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                println("DO SOMETHING")
            }
        }).attachToRecyclerView(binding.list)

        // Устаревший вариант
        /*
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }
         */

        // Актуальный вариант
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.data.collectLatest { pagingData ->
                    Log.d("FRAGMENT", "collectLatest received PagingData")
                    adapter.submitData(pagingData)
                }
            }
        }

        // Устаревший вариант
        /*
        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                    state.prepend is LoadState.Loading ||
                    state.append is LoadState.Loading
            }
        }
         */

        // Актуальный вариант
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                adapter.loadStateFlow.collectLatest { state ->
                    binding.swiperefresh.isRefreshing =
                        state.refresh is LoadState.Loading
                                //В коде из лекции индикатор загрузки отображается во всех трех состояниях.
                                //Для выполнения задания номер 1 оставляем индикатор только для REFRESH
                                || state.prepend is LoadState.Loading ||
                                state.append is LoadState.Loading
                }
            }
        }

// Наблюдаем за одноразовыми событиями ошибки
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Log.e("LIKE_DEBUG", "ПОЙМАНА ОШИБКА: $errorMessage")
            // Для наглядности можно вывести Toast, чтобы точно увидеть ошибку на экране
            android.widget.Toast.makeText(
                requireContext(),
                errorMessage,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        binding.swiperefresh.setOnRefreshListener(adapter::refresh)

        binding.addButton.setOnClickListener {
            //Если пользователь не авторизован, переходим на страницу логина
            if (auth.authStateFlow.value.token != null) {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
            //TODO Проверить, как должно выглядеть предложение залогиниться для создания нового поста
        }

        // Наблюдение за событием перехода на экран логина при нажатии на кнопку лайк(если сервер вернул код 403)
        viewModel.navigateToLoginEvent.observe(viewLifecycleOwner) {
            Log.d("NAV_DEBUG", "Получен код 403, переходим на экран авторизации")

            // Очищаем состояние авторизации для фоновых процессов.
            auth.removeAuth()

            findNavController().navigate(R.id.action_feedFragment_to_loginFragment)
        }

        return binding.root
    }
}
