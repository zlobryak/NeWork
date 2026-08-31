package ru.netology.nework.ui.fragments.userfragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.data.repository.PostRepository
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.ui.adapter.FeedAdapter
import ru.netology.nework.ui.fragments.FeedFragmentDirections.Companion.actionFeedFragmentToNewPostFragment
import ru.netology.nework.ui.viewmodel.PostViewModel // Или UserViewModel, если решите объединить
import javax.inject.Inject

@AndroidEntryPoint
class UserWallFragment : Fragment() {

    @Inject
    lateinit var repository: PostRepository

    private val viewModel: PostViewModel by viewModels()

    companion object {
        private const val ARG_USER_ID = "user_id_arg"

        fun newInstance(userId: Int?): UserWallFragment {
            return UserWallFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, userId!!)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)
        binding.list.layoutManager = LinearLayoutManager(requireContext())

        val adapter = FeedAdapter(object : FeedAdapter.OnInteractionListener {
            override fun onEdit(post: PostItem) {
                val action = actionFeedFragmentToNewPostFragment(post)
                findNavController().navigate(action)
            }

            override fun onLike(post: PostItem) {

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
        })

        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  Извлекаем userId из аргументов
        val userId = requireArguments().getInt(ARG_USER_ID)

        // Теперь мы знаем, чью стену грузить!
        // TODO: Вызвать метод ViewModel для загрузки постов этого пользователя
        viewModel.loadUserPosts(userId)
    }
}