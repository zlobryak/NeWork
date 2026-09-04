package ru.netology.nework.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentLoginBinding
import ru.netology.nework.ui.viewmodel.AuthFragmentViewModel
import ru.netology.nework.ui.viewmodel.LoginState

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val viewModel: AuthFragmentViewModel by activityViewModels()
    private var _binding: FragmentLoginBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(
            inflater,
            container,
            false
        )

        binding.username.requestFocus()

        binding.loginButton.setOnClickListener {
            val username = binding.username.text.toString()
            val password = binding.password.text.toString()

            // Логика авторизации
            Toast.makeText(requireContext(), "Вход...", Toast.LENGTH_SHORT).show()
            viewModel.login(username, password)
        }

        // Если пользователь нажимает на предложение зарегистрироваться, переходим на фрагмент регистрации
        binding.registerPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
        }

        // Наблюдаем за состоянием авторизации
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.loading.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }

                is LoginState.Success -> {
                    binding.loading.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    viewLifecycleOwner.lifecycleScope.launch {
                        viewModel.refresh()
                    }
                    // Переход к следующему экрану
                    findNavController().navigate(
                        R.id.action_loginFragment_to_feedFragment
                    )
                }

                is LoginState.Error -> {
                    binding.loading.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is LoginState.Idle -> {
                    // Сброс UI при необходимости
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//TODO Добавить обработку ввода неправильного пароля и логина
