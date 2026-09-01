package ru.netology.nework.ui.fragments.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentUserBinding
import ru.netology.nework.ui.adapter.UserPagerAdapter
import ru.netology.nework.ui.viewmodel.UserUiState
import ru.netology.nework.ui.viewmodel.UserViewModel

@AndroidEntryPoint
class UserFragment : Fragment() {

    // Инжектим ViewModel через Hilt
    private val viewModel: UserViewModel by viewModels()

    // Получаем аргумент. Убедитесь, что в nav_graph.xml аргумент называется именно "userIdArg"
    private val args: UserFragmentArgs by navArgs()

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Запускаем загрузку данных при открытии фрагмента
        viewModel.loadUserData(args.userIdArg)

        // 2. Наблюдаем за состоянием UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UserUiState.Loading -> {
                    showLoading(true)
                }

                is UserUiState.Success -> {
                    showLoading(false)
                    setupUI(state.user) // Передаем загруженного пользователя в метод отрисовки
                }

                is UserUiState.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // Вынесли настройку UI в отдельный чистый метод
    private fun setupUI(user: ru.netology.nework.data.dto.user.UserItem) {
        // Настройка Action Bar
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = user.name
            setDisplayHomeAsUpEnabled(true)
        }

        // Загружаем аватар
        Glide.with(this)
            .load(user.avatar)
            .placeholder(R.drawable.ic_manufacturing_24px) // Хорошая практика: ставить заглушку
            .into(binding.userPhoto)

        // Настраиваем ViewPager2 и TabLayout
        setupViewPager(user.userId) // Передаем ID для загрузки постов/вакансий
    }

    private fun setupViewPager(userId: Int) {
        val adapter = UserPagerAdapter(this, userId)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.wall)
                1 -> getString(R.string.jobs)
                else -> throw IllegalStateException("Неверная позиция: $position")
            }
        }.attach()
    }

    private fun showLoading(isLoading: Boolean) {
        // TODO Прогресс бар в разметке
//        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        // binding.viewPager.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}