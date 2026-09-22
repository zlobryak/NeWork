package ru.netology.nework.ui.fragments.events

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentNewEventBinding
import javax.inject.Inject
import kotlin.getValue
import ru.netology.nework.ui.viewmodel.events.EventNewViewModel
import java.util.Date

@AndroidEntryPoint
class EventNewFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    private var _binding: FragmentNewEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventNewViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("NEW_EVENT_DEBUG", "NewEventFragment создан, view: $view")
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Обработка клика по кнопке "+"
        binding.choseDateButton.setOnClickListener {
            showEventOptionsBottomSheet()
        }
    }

    private fun showEventOptionsBottomSheet() {
        val bottomSheet = EventOptionsBottomSheetFragment.newInstance()

        bottomSheet.setListener(object : EventOptionsBottomSheetFragment.EventOptionsListener {
            override fun onEventOptionsConfirmed(dateTime: Date, isOnline: Boolean) {
                // Передаем данные в ViewModel
                viewModel.changeEventOptions(dateTime, isOnline)
            }
        })

        bottomSheet.show(childFragmentManager, EventOptionsBottomSheetFragment.TAG)
    }

    private fun saveEvent() { //TODO ПОсмотреть что надо передавать для сохранение
        viewModel.save()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}