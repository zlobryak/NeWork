package ru.netology.nework.ui.fragments.events

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.data.dto.Coords
import ru.netology.nework.data.dto.event.IsOnline
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.ui.fragments.users.UsersListFragment
import ru.netology.nework.ui.viewmodel.events.EventNewViewModel
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.StringArg
import ru.netology.nework.view.loadAttachment
import java.util.Date
import kotlin.getValue

@AndroidEntryPoint
class NewEventFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg

        // Используем отдельный ключ, чтобы черновики постов и событий не конфликтовали
        private const val DRAFT_KEY_EVENT = "new_event_draft"

        // Ключи для хранения всего добавленного контекста черновика события
        private const val DRAFT_KEY_DATETIME = "new_event_draft_datetime"
        private const val DRAFT_KEY_IS_ONLINE = "new_event_draft_is_online"
        private const val DRAFT_KEY_LAT = "new_event_draft_lat"
        private const val DRAFT_KEY_LON = "new_event_draft_lon"
        private const val DRAFT_KEY_PHOTO = "new_event_draft_photo"

        private const val MAP_PICKER_DIALOG_TAG = "MapPickerDialog"
    }

    // Для редактирования
    private val args: NewEventFragmentArgs by navArgs()

    private val viewModel: EventNewViewModel by viewModels()
    private var fragmentBinding: FragmentNewEventBinding? = null
    private lateinit var sharedPreferences: SharedPreferences

    private var isFillDone = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("EventDraftPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)
        this.fragmentBinding = binding

        // Определяем режим: редактирование или создание
        val eventToEdit = args.eventItemArg
        val isEditMode = eventToEdit != null

        if (isEditMode) {
            viewModel.edit(eventToEdit)
        } else {
            // Восстанавливаем весь сохраненный контекст из черновика
            restoreDraftIfNeeded()
        }

        observeViewModel(binding)
        setupImagePicker(binding)
        setupMenu(isEditMode)
        setupBackPressed(isEditMode)
        setupDateOptions(binding)
        setupPlacePicker()
        setupSpeakerPicker()

        if (!isEditMode && !isFillDone) {
            binding.editContent.requestFocus()
        }

        return binding.root
    }

    // Загружает черновик (текст, дата, тип, место, фото) в ViewModel
    private fun restoreDraftIfNeeded() {
        val content = arguments?.textArg
            ?: sharedPreferences.getString(DRAFT_KEY_EVENT, "")

        val hasCoords = sharedPreferences.contains(DRAFT_KEY_LAT) &&
                sharedPreferences.contains(DRAFT_KEY_LON)
        val coords = if (hasCoords) {
            Coords(
                sharedPreferences.getFloat(DRAFT_KEY_LAT, 0f).toDouble(),
                sharedPreferences.getFloat(DRAFT_KEY_LON, 0f).toDouble(),
            )
        } else null

        viewModel.restoreDraft(
            content = content,
            dateTime = sharedPreferences.getString(DRAFT_KEY_DATETIME, null),
            type = when {
                sharedPreferences.contains(DRAFT_KEY_IS_ONLINE) ->
                    if (sharedPreferences.getBoolean(DRAFT_KEY_IS_ONLINE, true))
                        IsOnline.ONLINE
                    else
                        IsOnline.OFFLINE
                else -> null
            },
            coords = coords,
            photoUrl = sharedPreferences.getString(DRAFT_KEY_PHOTO, null),
        )
    }

    private fun observeViewModel(binding: FragmentNewEventBinding) {
        viewModel.edited.observe(viewLifecycleOwner) { event ->
            val text = event.content ?: ""
            if (binding.editContent.text.toString() != text) {
                binding.editContent.setText(text)
                // Курсор в конец текста только при первоначальном заполнении (редактирование/черновик)
                if (!isFillDone) {
                    binding.editContent.setSelection(binding.editContent.text?.length ?: 0)
                    isFillDone = true
                }
            }

            // Отображаем выбранное место, чтобы координаты были видны на экране создания
            event.coords?.let { coords ->
                binding.placeLabel.visibility = View.VISIBLE
                binding.placeLabel.text = getString(R.string.selected_place, coords.lat, coords.long)
            } ?: run {
                binding.placeLabel.visibility = View.GONE
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.photoContainer.visibility = View.GONE
                binding.removePhoto.visibility = View.GONE
            } else {
                binding.photoContainer.visibility = View.VISIBLE
                binding.removePhoto.visibility = View.VISIBLE
                val uriString = photoModel.uri.toString()

                if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    binding.photo.loadAttachment(uriString)
                } else {
                    binding.photo.setImageURI(photoModel.uri)
                }
            }
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { error ->
            Snackbar.make(requireView(), error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setupImagePicker(binding: FragmentNewEventBinding) {
        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when (result.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(result.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> {
                        viewModel.changePhoto(result.data?.data)
                    }
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.removePhoto()
        }
    }

    // FAB выбора даты и типа события: открывает нижний лист с опциями,
    // по подтверждении записывает дату/время и тип (ONLINE/OFFLINE) в ViewModel
    private fun setupDateOptions(binding: FragmentNewEventBinding) {
        binding.choseDateButton.setOnClickListener {
            val sheet = EventOptionsBottomSheetFragment.newInstance()
            sheet.setListener(object : EventOptionsBottomSheetFragment.EventOptionsListener {
                override fun onEventOptionsConfirmed(date: Date, isOnline: Boolean) {
                    viewModel.changeEventOptions(date, isOnline)
                }
            })
            sheet.show(childFragmentManager, EventOptionsBottomSheetFragment.TAG)
        }
    }

    // Кнопка «добавить координаты»: показывает Яндекс карту в виде dialog-фрагмента.
    // Такой фрагмент переживает конфигурационные изменения (пересоздание этого экрана),
    // поэтому карта не исчезает, а введенный текст не стирается.
    private fun setupPlacePicker() {
        fragmentBinding?.addPlace?.setOnClickListener {
            // Защита от двойного открытия
            if (parentFragmentManager.findFragmentByTag(MAP_PICKER_DIALOG_TAG) != null) {
                return@setOnClickListener
            }

            val currentCoords = viewModel.edited.value?.coords
            MapPickerFragment.newInstance(currentCoords)
                .show(parentFragmentManager, MAP_PICKER_DIALOG_TAG)
        }

        // Подписываемся на результат выбора места один раз
        parentFragmentManager.setFragmentResultListener(
            MapPickerFragment.REQUEST_KEY_PLACE,
            viewLifecycleOwner,
        ) { _, bundle ->
            if (bundle.getBoolean(MapPickerFragment.ARG_CANCELED)) {
                return@setFragmentResultListener
            }
            val lat = bundle.getDouble(MapPickerFragment.ARG_LAT)
            val lon = bundle.getDouble(MapPickerFragment.ARG_LON)
            viewModel.changePlace(Coords(lat, lon))
        }
    }

    // Кнопка «пригласить спикера»: открывает общий список пользователей
    // в режиме выбора (UsersListFragment) и принимает выбранных через
    // setFragmentResultListener, сохраняя id спикеров в edited.speakerIds
    private fun setupSpeakerPicker() {
        fragmentBinding?.addUser?.setOnClickListener {
            val initial = viewModel.edited.value?.speakerIds?.toIntArray() ?: intArrayOf()
            findNavController().navigate(
                NewEventFragmentDirections.actionEventNewFragmentToUsersListFragment().apply {
                    selectionMode = true
                    initialSelected = initial
                }
            )
        }

        // Подписываемся на результат выбора один раз
        parentFragmentManager.setFragmentResultListener(
            UsersListFragment.REQUEST_KEY_SELECTED_USERS,
            viewLifecycleOwner,
        ) { _, bundle ->
            if (bundle.getBoolean(UsersListFragment.ARG_CANCELED)) {
                return@setFragmentResultListener
            }
            val ids = bundle.getIntArray(UsersListFragment.ARG_SELECTED_IDS)?.toSet()
                ?: emptySet()
            viewModel.changeSpeakers(ids)
        }
    }

    private fun setupMenu(isEditMode: Boolean) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                // Используем один и тот же лэйаут для сохранения
                menuInflater.inflate(R.menu.menu_new_post, menu)
                menu.findItem(R.id.save)?.title =
                    if (isEditMode) getString(R.string.save) else getString(R.string.publish)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let { binding ->
                            // Сначала обновляем ViewModel актуальными данными из UI
                            viewModel.changeContent(binding.editContent.text.toString())

                            // Вызываем сохранение
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())

                            if (!isEditMode) {
                                clearDraft()
                            }
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupBackPressed(isEditMode: Boolean) {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!isEditMode) {
                    saveDraft()
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    // Сохраняем весь добавленный контекст (текст, дата, тип, место, фото),
    // чтобы он не потерялся при закрытии/пересоздании экрана
    private fun saveDraft() {
        val event = viewModel.edited.value ?: return
        val text = event.content.orEmpty().trim()
        val photoUri = viewModel.photo.value?.uri?.toString()

        val isEmpty = text.isEmpty() &&
                event.coords == null &&
                event.datetime.isEmpty() &&
                photoUri.isNullOrEmpty()

        if (isEmpty) {
            clearDraft()
            return
        }

        sharedPreferences.edit {
            putString(DRAFT_KEY_EVENT, text)
            if (event.datetime.isNotEmpty()) {
                putString(DRAFT_KEY_DATETIME, event.datetime)
            } else {
                remove(DRAFT_KEY_DATETIME)
            }
            putBoolean(DRAFT_KEY_IS_ONLINE, event.type != IsOnline.OFFLINE)
            event.coords?.let {
                putFloat(DRAFT_KEY_LAT, it.lat.toFloat())
                putFloat(DRAFT_KEY_LON, it.long.toFloat())
            } ?: run {
                remove(DRAFT_KEY_LAT)
                remove(DRAFT_KEY_LON)
            }
            if (!photoUri.isNullOrEmpty()) {
                putString(DRAFT_KEY_PHOTO, photoUri)
            } else {
                remove(DRAFT_KEY_PHOTO)
            }
        }
    }

    private fun clearDraft() {
        sharedPreferences.edit {
            remove(DRAFT_KEY_EVENT)
            remove(DRAFT_KEY_DATETIME)
            remove(DRAFT_KEY_IS_ONLINE)
            remove(DRAFT_KEY_LAT)
            remove(DRAFT_KEY_LON)
            remove(DRAFT_KEY_PHOTO)
        }
    }

    // Черновик нужен только в режиме создания
    override fun onPause() {
        super.onPause()
        if (args.eventItemArg == null) {
            saveDraft()
        }
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}