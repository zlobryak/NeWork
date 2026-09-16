package ru.netology.nework.ui.fragments.events

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.data.dto.event.EventItem
import ru.netology.nework.data.entity.CoordsEmbeddable
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUserListItem
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUsersAdapter
import ru.netology.nework.ui.viewmodel.events.EventDetailViewModel

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    private val viewModel: EventDetailViewModel by viewModels()

    private lateinit var speakersAdapter: EventUsersAdapter
    private lateinit var likersAdapter: EventUsersAdapter
    private lateinit var participantsAdapter: EventUsersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews(view)
        observeData(view)
    }

    private fun setupRecyclerViews(view: View) {
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

        view.findViewById<RecyclerView>(R.id.speakers_recycler).adapter = speakersAdapter
        view.findViewById<RecyclerView>(R.id.likers_recycler).adapter = likersAdapter
        view.findViewById<RecyclerView>(R.id.participants_recycler).adapter = participantsAdapter

        // Обработчик кнопки Like
        view.findViewById<MaterialButton>(R.id.like).setOnClickListener {
            viewModel.likeEvent()
            //TODO Кнопка частвовать/не участвовать
        }
    }

    private fun observeData(view: View) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventState.collect { event ->
                    event?.let { bindEvent(view, it) }
                }
            }
        }
    }

    private fun bindEvent(view: View, event: EventItem) {
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

        view.findViewById<TextView>(R.id.author_name).text = event.author
        view.findViewById<TextView>(R.id.author_job).text =
            event.authorJob ?: getString(R.string.job_searching)
        view.findViewById<TextView>(R.id.event_type).text = event.type ?: ""
        view.findViewById<TextView>(R.id.event_datetime).text = event.datetime
        view.findViewById<TextView>(R.id.event_description).text = event.content
        view.findViewById<MaterialButton>(R.id.like).text =
            event.likeOwnerIds.size.toString()
        view.findViewById<MaterialButton>(R.id.like).isChecked = event.likedByMe
    }

    private fun setupMap(mapView: MapView, coords: CoordsEmbeddable) {
        // Инициализация Yandex Map и установка маркера
        // Здесь нужно реализовать логику с MapKit
    }

    private fun openUsersListFragment(type: UserListType) {
        // Навигация
    }

    enum class UserListType { SPEAKERS, LIKERS, PARTICIPANTS }
}