package ru.netology.nework.ui.fragments.events

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.ui.viewmodel.events.EventCreateViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EventCreateFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private var binding: EventCreateFragment? = null

    private val viewModel: EventCreateViewModel by viewModels()
}