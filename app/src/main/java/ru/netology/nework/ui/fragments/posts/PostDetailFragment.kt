package ru.netology.nework.ui.fragments.posts

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.databinding.FragmentPostBinding
import ru.netology.nework.ui.adapters.users.UserListItem
import ru.netology.nework.ui.adapters.users.UsersAdapter
import ru.netology.nework.ui.viewmodel.posts.PostDetailViewModel
import ru.netology.nework.utils.DateUtils
import ru.netology.nework.view.loadAttachment
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class PostDetailFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels()

    private lateinit var mentionedAdapter: UsersAdapter
    private lateinit var likersAdapter: UsersAdapter

    private var placemark: PlacemarkMapObject? = null

    private var currentCoords: Coords? = null


    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                Log.d("MapDebug", "Точное разрешение на геолокацию получено")
                // Если координаты уже известны, перерисовываем карту с разрешениями
                currentCoords?.let { setupMap(binding.mapView, it) }
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                Log.d("MapDebug", "Грубое разрешение на геолокацию получено")
                currentCoords?.let { setupMap(binding.mapView, it) }
            }
            else -> {
                Log.e("MapDebug", "Разрешение на геолокацию отклонено. Карта может работать некорректно.")
            }
        }
    }

    private fun checkLocationPermissionsAndInitMap() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d("MapDebug", "Разрешения уже есть, карта будет работать корректно")
        } else {
            Log.d("MapDebug", "Запрашиваем разрешения на геолокацию")
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
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
        mentionedAdapter = UsersAdapter(
            onItemClick = { userId -> /* открыть профиль */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.SPEAKERS) }
        )
        likersAdapter = UsersAdapter(
            onItemClick = { userId -> /* открыть профиль */ },
            onMoreButtonClick = { openUsersListFragment(UserListType.LIKERS) }
        )

        binding.likersRecycler.adapter = likersAdapter
        binding.mentionedRecycler.adapter = mentionedAdapter
    }

    private fun setupListeners() {
        binding.likeButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id == 0L) {
                navigateToLogin()
            } else {
                viewModel.likeEvent()
            }
        }

        binding.mentionedButton

        binding.mentionedButton.setOnClickListener {
            if (appAuth.authStateFlow.value.id == 0L) {
                navigateToLogin()
            } else {
                viewModel.mentionedEvent()
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
                    viewModel.postState,
                    appAuth.authStateFlow
                ) { post, authState ->
                    post to authState
                }.collect { (post, authState) ->
                    post?.let { bindPost(it, authState) }
                }
            }
        }
    }

    private fun bindPost(post: PostItem, authState: AuthState) {
        val likers = post.getLikers().map {
            UserListItem.User(it.id, it.avatar, it.name ?: "")
        }
        val mentioned = post.getMentioned().map {
            UserListItem.User(it.id, it.avatar, it.name ?: "")
        }

        likersAdapter.submitList(likers)
        mentionedAdapter.submitList(mentioned)

        val currentUserId = authState.id
        val isAuthorized = currentUserId != 0L

        with(binding) {
            authorName.text = post.authorName
            authorJob.text = post.authorJob ?: getString(R.string.job_searching)

            likeButton.text = post.likeOwnerIds.size.toString()
            likeButton.isChecked = post.likedByMe
            likeButton.isEnabled = isAuthorized

            publicationDate.text = DateUtils.formatIsoDate(post.published)

            mentionedButton.text = post.mentionIds.size.toString()
            mentionedButton.isChecked = post.mentionedMe
            mentionedButton.isEnabled = isAuthorized

            // Обложка
            post.attachment?.let {
                attachment.visibility = View.VISIBLE
                attachment.loadAttachment(it.url)
            } ?: run {
                attachment.visibility = View.GONE
            }

            // КАРТА
            post.coords?.let { coords ->
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