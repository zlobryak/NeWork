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
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.databinding.FragmentEventBinding
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUserListItem
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUsersAdapter
import ru.netology.nework.ui.viewmodel.events.EventDetailViewModel

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

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

    // ---------- Настройка (вызывается один раз) ----------

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
            viewModel.likeEvent()
        }

        // TODO: кнопка участвовать/не участвовать
        // binding.participate.setOnClickListener { viewModel.participateEvent() }
    }

    // ---------- Наблюдение за данными ----------

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventState.collect { event ->
                    event?.let { bindEvent(it) }
                }
            }
        }
    }

    // ---------- Только привязка данных, без слушателей ----------

    private fun bindEvent(event: EventItem) {
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
            authorName.text = event.author
            authorJob.text = event.authorJob ?: getString(R.string.job_searching)
            eventType.text = event.type ?: ""
            eventDatetime.text = event.datetime
            eventDescription.text = event.content
            like.text = event.likeOwnerIds.size.toString()
            like.isChecked = event.likedByMe
            participantsCount.text = event.participantsIds.size.toString()

            // Обложка
            event.attachment?.let { attachment ->
                eventCover.visibility = View.VISIBLE
                // Glide/Coil: load(attachment.url).into(eventCover)
            } ?: run {
                eventCover.visibility = View.GONE
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