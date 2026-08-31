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
import ru.netology.nework.ui.viewmodel.UserViewModel

@AndroidEntryPoint
class UserFragment : Fragment() {
    private val viewModel: UserViewModel by viewModels()
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

        // Ранний выход: если пользователя нет, просто выходим
        val user = args.userItemArg ?: return

         (requireActivity() as AppCompatActivity).supportActionBar?.apply {
             title = user.name
             setDisplayHomeAsUpEnabled(true)
         }

        //  Загружаем аватар
        Glide.with(this)
            .load(user.avatar)
            .into(binding.userPhoto)

        // Запускаем загрузку данных для этого пользователя
        viewModel.loadUserData(user.id)

        // Настраиваем ViewPager2 и TabLayout
        setupViewPager(user.id)

        // Обрабатываем ошибки
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupViewPager(userId: Int?) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Защита от утечек памяти
    }
}