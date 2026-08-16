package ru.netology.nework.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentRegistrationBinding
import ru.netology.nework.ui.viewmodel.AuthViewModel
import ru.netology.nework.ui.viewmodel.RegistrationState
import ru.netology.nework.ui.viewmodel.RegistrationViewModel

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistrationViewModel by viewModels()

    private val appViewModel: AuthViewModel by activityViewModels()

    // Picker для выбора аватарки
    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedAvatarUri = uri
        uri?.let {
            // Показываем превью с помощью Glide
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_camera_24dp)
                .into(binding.avatarPreview)
        }
    }

    private var selectedAvatarUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //  Обработчик кнопки выбора аватарки
        binding.selectAvatarButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        binding.registerButton.setOnClickListener {
            val name = binding.name.text.toString()
            val login = binding.login.text.toString()
            val password = binding.password.text.toString()
            val confirm = binding.confirmPassword.text.toString()

            if (validateInput(name, login, password, confirm)) {
                viewModel.register(
                    login = login,
                    password = password,
                    name = name,
                    avatarUri = selectedAvatarUri,
                    context = requireContext()
                )
            }
        }

        viewModel.registrationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegistrationState.Loading -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }

                is RegistrationState.Success -> {
                    binding.loading.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    // Сохраняем авторизацию в AppAuth
                    appViewModel.setAuth(state.auth.id, state.auth.token)
                    // Возвращаемся назад
                    findNavController().popBackStack()
                }

                is RegistrationState.Error -> {
                    binding.loading.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is RegistrationState.Idle -> {}
            }
        }

        val appBarConfiguration = AppBarConfiguration.Builder().build()
        val navController = findNavController()

        (requireActivity() as? AppCompatActivity)?.let { activity ->
            NavigationUI.setupActionBarWithNavController(
                activity,
                navController,
                appBarConfiguration
            )
        }

// Используем MenuProvider для управления меню
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Очищаем меню — убираем все пункты (три точки)
                menu.clear()
            }

            override fun onMenuItemSelected(item: MenuItem) = false
        }

// Регистрируем провайдер меню с привязкой к жизненному циклу
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    private fun validateInput(
        name: String,
        login: String,
        password: String,
        confirm: String
    ): Boolean {
        var valid = true

        if (name.isBlank()) {
            binding.name.error = "Введите имя"
            valid = false
        } else {
            binding.name.error = null
        }

        if (login.isBlank()) {
            binding.login.error = "Введите логин"
            valid = false
        } else {
            binding.login.error = null
        }

        if (password.length < 6) {
            binding.password.error = "Пароль должен содержать минимум 6 символов"
            valid = false
        } else {
            binding.password.error = null
        }

        if (password != confirm) {
            binding.confirmPassword.error = "Пароли не совпадают"
            valid = false
        } else {
            binding.confirmPassword.error = null
        }

        return valid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}