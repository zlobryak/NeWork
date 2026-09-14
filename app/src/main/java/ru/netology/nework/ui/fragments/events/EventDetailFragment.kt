package ru.netology.nework.ui.fragments.events

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.ui.adapters.eventDetailFragmment.EventUsersAdapter

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    private lateinit var speakersAdapter: EventUsersAdapter
    private lateinit var likersAdapter: EventUsersAdapter
    private lateinit var participantsAdapter: EventUsersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeData()
    }

    private fun setupRecyclerViews() {
        speakersAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть список */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.SPEAKERS) }
        )

        likersAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть список */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.LIKERS) }
        )

        participantsAdapter = EventUsersAdapter(
            onItemClick = { userId -> /* открыть список */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.PARTICIPANTS) }
        )

        view?.findViewById<RecyclerView>(R.id.speakers_recycler)?.adapter = speakersAdapter
        view?.findViewById<RecyclerView>(R.id.likers_recycler)?.adapter = likersAdapter
        view?.findViewById<RecyclerView>(R.id.participants_recycler)?.adapter = participantsAdapter
    }

    private fun observeData() {
        // Получение данных из ViewModel:
        // speakersAdapter.submitList(speakers.map { EventUserListItem.User(it.id, it.avatarUrl) })
        // likersAdapter.submitList(likers.map { EventUserListItem.User(it.id, it.avatarUrl) })
        // participantsAdapter.submitList(participants.map { EventUserListItem.User(it.id, it.avatarUrl) })
    }

    private fun openUsersListFragment(type: UserListType) {
        // Навигация к фрагменту со списком всех пользователей
        // val bundle = bundleOf("type" to type)
        // findNavController().navigate(R.id.action_eventDetailFragment_to_usersListFragment, bundle)
    }

    enum class UserListType { SPEAKERS, LIKERS, PARTICIPANTS }
}