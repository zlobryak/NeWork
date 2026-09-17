package ru.netology.nework.ui.fragments.events

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
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
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import javax.inject.Inject

@AndroidEntryPoint
class EventDetailFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventDetailViewModel by viewModels()

    private lateinit var speakersAdapter: EventUsersAdapter
    private lateinit var likersAdapter: EventUsersAdapter
    private lateinit var participantsAdapter: EventUsersAdapter

    private var placemark: PlacemarkMapObject? = null

    private var currentCoords: Coords? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

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
        setupRecyclerViews()
        setupListeners()
        observeData()
    }

    override fun onStart() {
        super.onStart()
        _binding?.mapView?.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        _binding?.mapView?.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        // Очистка маркера перед уничтожением view
        placemark = null
        currentCoords = null
        _binding = null
        super.onDestroyView()
    }

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

    private fun bindEvent(event: EventItem, authState: AuthState) {
        val speakers = event.getSpeakers().map {
            EventUserListItem.User(it.id, it.avatar, it.name ?: "")
        }
        val likers = event.getLikers().map {
            EventUserListItem.User(it.id, it.avatar, it.name ?: "")
        }
        val participants = event.getParticipants().map {
            EventUserListItem.User(it.id, it.avatar, it.name ?: "")
        }

        speakersAdapter.submitList(speakers)
        likersAdapter.submitList(likers)
        participantsAdapter.submitList(participants)

        val currentUserId = authState.id
        val isAuthorized = currentUserId != 0L

        with(binding) {
            authorName.text = event.author
            authorJob.text = event.authorJob ?: getString(R.string.job_searching)
            eventType.text = event.type ?: ""
            eventDatetime.text = DateUtils.formatIsoDate(event.datetime)
            eventDescription.text = event.content

            like.text = event.likeOwnerIds.size.toString()
            like.isChecked = event.isLikedBy(currentUserId)
            like.isEnabled = isAuthorized

            participateButton.text = event.participantsIds.size.toString()
            participateButton.isChecked = event.isParticipating(currentUserId)
            participateButton.isEnabled = isAuthorized

            // Обложка
            event.attachment?.let {
                attachment.visibility = View.VISIBLE
                attachment.loadAttachment(it.url)
            } ?: run {
                attachment.visibility = View.GONE
            }

            // КАРТА
            event.coords?.let { coords ->
                mapView.visibility = View.VISIBLE

                // Устанавливаем маркер только если координаты изменились
                if (currentCoords != coords) {
                    currentCoords = coords
                    setupMap(mapView, coords)
                }
            } ?: run {
                mapView.visibility = View.GONE
                currentCoords = null
                // Удаляем маркер, если координаты исчезли
                placemark?.let {
                    mapView.map.mapObjects.remove(it)
                    placemark = null
                }
            }
        }
    }

    private fun setupMap(mapView: MapView, coords: Coords) {
        Log.d("MapDebug", "Устанавливаем маркер: lat=${coords.lat}, lon=${coords.long}")

        // Если координаты (0.0, 0.0) — маркер в Атлантическом океане
        if (coords.lat == 0.0 && coords.long == 0.0) {
            Log.w("MapDebug", "Координаты нулевые!")
            return
        }

        try {
            val map = mapView.map
            val point = Point(coords.lat, coords.long)

            map.move(CameraPosition(point, 15f, 0f, 0f))

            placemark?.let { map.mapObjects.remove(it) }

            // Конвертируем вектор в Bitmap т.к. на эмулятор не работает векторная иконкаф
            val iconDrawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_location_pin_24dp
            )
            val iconBitmap: Bitmap? = iconDrawable?.toBitmap(
                width = 96,   // увеличиваем размер для лучшей видимости
                height = 96
            )

            placemark = map.mapObjects.addPlacemark().apply {
                geometry = point
                iconBitmap?.let { bitmap ->
                    // Передаём уже готовый Bitmap — MapKit его точно отобразит
                    setIcon(ImageProvider.fromBitmap(bitmap))
                }
            }

            Log.d("MapDebug", "Маркер успешно добавлен")
        } catch (e: Exception) {
            Log.e("MapDebug", "Ошибка настройки карты", e)
            binding.mapView.visibility = View.GONE
        }
    }


    private fun openUsersListFragment(type: UserListType) {
        // TODO Навигация
    }

    enum class UserListType { SPEAKERS, LIKERS, PARTICIPANTS }
}