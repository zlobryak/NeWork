package ru.netology.nework.ui.fragments.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.netology.nework.R
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.databinding.FragmentUsersListBinding
import ru.netology.nework.ui.adapters.usersFeed.UsersListAdapter

/**
 * Полноэкранный список пользователей, соответствующий одному из полей
 * просматриваемого объекта (поста или события):
 * лайкнувшие, упомянутые, спикеры, участники.
 *
 * Открывается по кнопке "+" (шестой элемент) в адаптерах аватарок
 * детальных экранов PostDetailFragment и EventDetailFragment.
 *
 * Полный список передаётся во фрагмент аргументом usersListArg
 * (Safe Args, ArrayList<UserItem>, UserItem — Parcelable).
 */
class UsersListFragment : Fragment() {

    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!

    private val args: UsersListFragmentArgs by navArgs()

    private lateinit var adapter: UsersListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        submitUsers(args.usersListArg.toList())
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        adapter = UsersListAdapter(onUserClick = ::openUserProfile)
        binding.list.adapter = adapter
    }

    private fun submitUsers(users: List<UserItem>) {
        adapter.submitList(users)
        binding.empty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
    }

    /**
     * Клик по карточке пользователя ведёт в его профиль.
     * Переход описан только у usersFeedFragment, поэтому для остальных
     * экранов поднимаемся к общему родителю (nav_main), где объявлен userFragment.
     */
    private fun openUserProfile(user: UserItem) {
        val bundle = Bundle().apply { putInt("userIdArg", user.id) }
        findNavController().navigate(
            R.id.userFragment,
            bundle,
        )
    }
}
