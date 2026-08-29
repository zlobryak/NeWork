package ru.netology.nework.ui.fragments.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.repository.PostRepository
import ru.netology.nework.ui.viewmodel.PostViewModel // Или UserViewModel, если решите объединить
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {

    @Inject
    lateinit var repository: PostRepository

    private val viewModel: PostViewModel by viewModels()

    companion object {
        private const val ARG_USER_ID = "user_id_arg"

        fun newInstance(userId: Int): UserWallFragment {
            return UserWallFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, userId)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Извлекаем userId из аргументов
        val userId = requireArguments().getInt(ARG_USER_ID)

        // Теперь мы знаем, чью стену грузить!
        // TODO: Вызвать метод ViewModel для загрузки постов этого пользователя
        viewModel.loadUserPosts(userId)
    }
}