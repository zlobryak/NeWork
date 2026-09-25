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
import ru.netology.nework.databinding.FragmentNewEventBinding
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
    }

    // Для редактирования
    private val args: NewEventFragmentArgs by navArgs()

    private val viewModel: EventNewViewModel by viewModels()
    private var fragmentBinding: FragmentNewEventBinding? = null
    private lateinit var sharedPreferences: SharedPreferences

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
            val initialText = arguments?.textArg ?: sharedPreferences.getString(DRAFT_KEY_EVENT, "")
            if (!initialText.isNullOrEmpty()) {
                viewModel.changeContent(initialText)
            }
        }

        observeViewModel(binding, isEditMode)
        setupImagePicker(binding)
        setupMenu(isEditMode)
        setupBackPressed(isEditMode, binding)
        setupDateOptions(binding)
        setupPlacePicker()

        if (!isEditMode) {
            binding.editContent.requestFocus()
        }

        return binding.root
    }

    private fun observeViewModel(binding: FragmentNewEventBinding, isEditMode: Boolean) {
        viewModel.edited.observe(viewLifecycleOwner) { event ->
            // Обновляем текст, если он изменился извне (например, загрузка черновика)
            // Важно: не делай это при каждом вводе пользователя, чтобы не сбивать курсор.
            // Обычно текст биндится через TextWatcher, который вызывает viewModel.changeContent()
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

    // Кнопка «добавить координаты»: показывает полноэкранный фрагмент с Яндекс картой,
    // по выбору точки записывает координаты в ViewModel (changePlace -> coords события)
    private fun setupPlacePicker() {
        fragmentBinding?.addPlace?.setOnClickListener {
            // Защита от двойного нажатия
            if (childFragmentManager.findFragmentByTag(MapPickerFragment.TAG) != null) {
                return@setOnClickListener
            }

            val currentCoords = viewModel.edited.value?.coords
            val picker = MapPickerFragment.newInstance(currentCoords)

            picker.setOnPlaceSelectedListener(object : MapPickerFragment.MapPickerListener {
                override fun onPlaceSelected(coords: Coords) {
                    viewModel.changePlace(coords)
                    hideMapPicker() // Скрываем карту после успешного выбора
                }

                override fun onCanceled() {
                    hideMapPicker() // Скрываем карту при отмене
                }
            })

            // Добавляем фрагмент в контейнер.
            childFragmentManager.beginTransaction()
                .replace(R.id.mapPickerContainer, picker, MapPickerFragment.TAG)
                .commit()
        }
    }

    private fun hideMapPicker() {
        val fragment = childFragmentManager.findFragmentByTag(MapPickerFragment.TAG)
        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
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
                            // Если есть другие поля (дата, место), убедись, что они уже записаны в ViewModel
                            // через их собственные слушатели (например, DatePicker callback)

                            // Вызываем сохранение
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())

                            if (!isEditMode) {
                                sharedPreferences.edit { remove(DRAFT_KEY_EVENT) }
                            }
                        }
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupBackPressed(isEditMode: Boolean, binding: FragmentNewEventBinding) {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val draftText = binding.editContent.text?.toString()?.trim()

                if (!isEditMode && !draftText.isNullOrEmpty()) {
                    sharedPreferences.edit { putString(DRAFT_KEY_EVENT, draftText) }
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}