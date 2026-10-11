package ru.netology.nework.ui.fragments.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.dto.user.UserItem
import ru.netology.nework.data.repository.user.UserRepository
import ru.netology.nework.databinding.FragmentUsersListBinding
import ru.netology.nework.ui.adapters.usersFeed.UsersListAdapter
import javax.inject.Inject

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
 *
 * Режим выбора (selectionMode=true): тот же универсальный список вызывается
 * при создании поста (кнопка «упомянуть пользователя») и события
 * (кнопка «пригласить спикера»). В этом режиме usersListArg не передаётся —
 * подгружается общий список всех пользователей (синхронизация с сервером),
 * клик по карточке отмечает пользователя чекбоксом, а кнопка «Готово»
 * возвращает выбранных через setFragmentResult (REQUEST_KEY_SELECTED_USERS).
 */
@AndroidEntryPoint
class UsersListFragment : Fragment() {

    companion object {
        const val ARG_SELECTION_MODE = "selectionMode"
        const val ARG_INITIAL_SELECTED = "initialSelected"

        /** Ключ для setFragmentResult / setFragmentResultListener. */
        const val REQUEST_KEY_SELECTED_USERS = "selected-users"

        /** IntArray id выбранных пользователей в бандле результата. */
        const val ARG_SELECTED_IDS = "selectedIds"

        /** Флаг отмены выбора (кнопка «Отмена» / back). */
        const val ARG_CANCELED = "canceled"

        /**
         * Создание аргументов для режима выбора:
         * общий список пользователей + изначально отмеченные id.
         */
        fun newSelectionInstance(initialSelected: IntArray): Bundle = bundleOf(
            ARG_SELECTION_MODE to true,
            ARG_INITIAL_SELECTED to initialSelected,
        )
    }

    private var _binding: FragmentUsersListBinding? = null
    private val binding get() = _binding!!

    private val args: UsersListFragmentArgs by navArgs()

    private lateinit var adapter: UsersListAdapter

    // ViewModel нужна только в режиме выбора; в обычном режиме просмотр
    // работает без неё (аргумент usersListArg обязателен для Safe Args,
    // но в режиме выбора заполняется дефолтным пустым массивом).
    private val viewModel: UsersSelectionViewModel by viewModels()

    private val selectionMode: Boolean
        get() = arguments?.getBoolean(ARG_SELECTION_MODE) == true

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

        if (selectionMode) {
            setupSelectionUi()
            loadSelectableUsers()
        } else {
            setupRecyclerView(selected = null)
            submitUsers(args.usersListArg.toList())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView(selected: MutableSet<Int>?) {
        adapter = UsersListAdapter(
            selectionMode = selected != null,
            initiallySelected = selected ?: emptySet(),
            onSelectionChanged = ::onSelectionChanged,
            onUserClick = ::openUserProfile,
        )
        binding.list.adapter = adapter
    }

    private fun submitUsers(users: List<UserItem>) {
        adapter.submitList(users)
        binding.empty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
    }

    // --- Режим выбора (упомянуть пользователя / пригласить спикера) ---

    private fun setupSelectionUi() {
        val initial = arguments?.getIntArray(ARG_INITIAL_SELECTED)?.toList()
            ?.toCollection(linkedSetOf()) ?: linkedSetOf()

        // При пересоздании view выбор уже живёт в ViewModel —
        // аргументы применяем только при первом запуске
        if (viewModel.selectedIds.value.isNullOrEmpty()) {
            viewModel.setSelected(initial)
        }

        setupRecyclerView(selected = requireNotNull(viewModel.selectedIds.value))

        binding.selectionButtons.visibility = View.VISIBLE

        binding.doneButton.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY_SELECTED_USERS,
                bundleOf(
                    ARG_SELECTED_IDS to (viewModel.selectedIds.value ?: emptySet()).toIntArray(),
                    ARG_CANCELED to false,
                )
            )
            findNavController().navigateUp()
        }
        binding.cancelButton.setOnClickListener {
            setFragmentResult(
                REQUEST_KEY_SELECTED_USERS,
                bundleOf(
                    ARG_SELECTED_IDS to intArrayOf(),
                    ARG_CANCELED to true,
                )
            )
            findNavController().navigateUp()
        }
    }

    private fun loadSelectableUsers() {
        binding.progressBar.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch {
            val users = viewModel.loadAllUsers()
            // Экран мог быть закрыт до окончания загрузки
            _binding?.let {
                it.progressBar.visibility = View.GONE
                submitUsers(users)
            }
        }
    }

    private fun onSelectionChanged(ids: Set<Int>) {
        viewModel.setSelected(ids)
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

/**
 * Состояние режима выбора. ViewModel живёт до уровня фрагмента,
 * поэтому отмеченные пользователи переживают пересоздание view.
 */
@HiltViewModel
class UsersSelectionViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _selectedIds = MutableLiveData<MutableSet<Int>>(linkedSetOf())
    val selectedIds: LiveData<MutableSet<Int>> = _selectedIds

    fun setSelected(ids: Set<Int>) {
        _selectedIds.value = ids.toCollection(linkedSetOf())
    }

    /**
     * Общий список всех пользователей: сначала синхронизация с сервером
     * (если локальная копия пуста), затем загрузка полного списка.
     */
    suspend fun loadAllUsers(): List<UserItem> {
        runCatching { userRepository.syncIfNeeded() }
        return runCatching { userRepository.loadUsersPage() }.getOrDefault(emptyList())
    }
}
