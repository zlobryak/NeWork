package ru.netology.nework.ui.fragments.userfragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.ui.adapters.userFragment.UserWallPostPagingAdapter
import ru.netology.nework.ui.adapters.postFeed.PostLoadStateAdapter
import ru.netology.nework.ui.fragments.FeedFragmentDirections.Companion.actionFeedFragmentToNewPostFragment
import ru.netology.nework.ui.viewmodel.UserViewModel

@AndroidEntryPoint
class UserWallFragment : Fragment() {

    // requireParentFragment() заставляет Hilt вернуть
    // экземпляр UserViewModel, созданный для UserFragment
    private val viewModel: UserViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: UserWallPostPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView и PagingDataAdapter
        binding.list.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserWallPostPagingAdapter(
            object : UserWallPostPagingAdapter.OnInteractionListener {
                override fun onEdit(post: PostItem) {
                    Log.d("NAVIGATION_DEBUG", "Попытка редактирования поста с ID: ${post.id}")
                    val action = actionFeedFragmentToNewPostFragment(post)
                    findNavController().navigate(action)
                }

                override fun onLike(post: PostItem) {
                    Log.d(
                        "LIKE_DEBUG",
                        "Fragment получил onLike для Post ID: ${post.id}, isSynced: ${post.isSynced}"
                    )
                    viewModel.likePost(post)
                }

                override fun onRemove(post: PostItem) {
                    viewModel.removePost(post)
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
                //TODO Добавить сюда открытие поста в отдельном фрагменте, когда оно будет реализовано

                //Тут не должно быть навигации в фрагмент пользователя со стеной.
            })

//  Добавляем  индикатор загрузки внизу списка при подгрузке новых страниц
        adapter.withLoadStateFooter(
            footer = PostLoadStateAdapter
            { adapter.retry() }
        )

        binding.list.adapter = adapter

// Собираем Flow с данными.
// Как только UserViewModel загрузит данные, они автоматически попадут сюда.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.wallPagingData.collect { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
        }
    }

    private fun sharePost(post: PostItem) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, post.content)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, getString(R.string.chooser_share_post)))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}