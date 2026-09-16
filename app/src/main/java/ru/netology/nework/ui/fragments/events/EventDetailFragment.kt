package ru.netology.nework.ui.fragments.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.auth.AuthState
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.dto.event.isLikedBy
import ru.netology.nework.data.dto.event.isParticipating
import ru.netology.nework.databinding.FragmentEventBinding
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUserListItem
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUsersAdapter
import ru.netology.nework.ui.viewmodel.events.EventDetailViewModel
import ru.netology.nework.view.loadAttachment
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    // Nullable backing property + геттер
    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventDetailViewModel by viewModels()

    private lateinit var speakersAdapter: EventUsersAdapter
    private lateinit var likersAdapter: EventUsersAdapter
    private lateinit var participantsAdapter: EventUsersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()   // адаптеры создаются один раз
        setupListeners()       // слушатели создаются один раз
        observeData()          // подписка на данные
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------- Настройка списков аватарок (вызывается один раз) ----------

    private fun setupRecyclerViews() {
        speakersAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть профиль */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.SPEAKERS) }
        )

        likersAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть профиль */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.LIKERS) }
        )

        participantsAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть профиль */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.PARTICIPANTS) }
        )

        binding.speakersRecycler.adapter = speakersAdapter
        binding.likersRecycler.adapter = likersAdapter
        binding.participantsRecycler.adapter = participantsAdapter
    }

    private fun setupListeners() {
        binding.like.setOnClickListener {
            if (appAuth.authStateFlow.value.id == 0L) {
                navigateToLogin()
            } else {
                viewModel.likeEvent()
            }
        }

        binding.participateButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id == 0L) {
                navigateToLogin()
            } else {
                viewModel.participateEvent()
            }
        }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.loginFragment)

    }

    // ---------- Наблюдение за данными ----------

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.eventState,
                    appAuth.authStateFlow
                ) { event, authState ->
                    event to authState
                }.collect { (event, authState) ->
                    event?.let { bindEvent(it, authState) }
                }
            }
        }
    }

    // ---------- Только привязка данных, без слушателей ----------

    private fun bindEvent(event: EventItem, authState: AuthState) {
        val speakers = event.getSpeakers().map {
            EventUserListItem.User(it.id, it.avatar)
        }
        val likers = event.getLikers().map {
            EventUserListItem.User(it.id, it.avatar)
        }
        val participants = event.getParticipants().map {
            EventUserListItem.User(it.id, it.avatar)
        }

        speakersAdapter.submitList(speakers)
        likersAdapter.submitList(likers)
        participantsAdapter.submitList(participants)

        with(binding) {
            val currentUserId = authState.id
            val isAuthorized = currentUserId != 0L

            authorName.text = event.author
            authorJob.text = event.authorJob ?: getString(R.string.job_searching)
            eventType.text = event.type ?: ""
            eventDatetime.text = event.datetime
            eventDescription.text = event.content

            like.text = event.likeOwnerIds.size.toString()
            like.isChecked = event.isLikedBy(currentUserId)
            like.isEnabled = isAuthorized

            participateButton.text = event.participantsIds.size.toString()
            participateButton.isChecked = event.isParticipating(currentUserId)
            participateButton.isEnabled = isAuthorized

            // Обложка
            val attachmentUrl = event.attachment?.url
            if (!attachmentUrl.isNullOrBlank()) {
                attachment.loadAttachment(attachmentUrl)
                attachment.visibility = View.VISIBLE
            } else {
                attachment.visibility = View.GONE
            }

            // Карта
            event.coords?.let { coords ->
                mapView.visibility = View.VISIBLE
                setupMap(mapView, coords)
            } ?: run {
                mapView.visibility = View.GONE
            }
        }
    }

    private fun setupMap(mapView: MapView, coords: Coords) {
        // Инициализация Yandex Map и установка маркера
    }

    private fun openUsersListFragment(type: UserListType) {
        // Навигация
    }

    enum class UserListType { SPEAKERS, LIKERS, PARTICIPANTS }
}