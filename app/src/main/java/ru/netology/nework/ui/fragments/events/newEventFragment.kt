package ru.netology.nework.ui.fragments.events

import android.app.Activity
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.ui.viewmodel.events.NewEventViewModel
import ru.netology.nework.view.loadAttachment

// Отправка изображения — точная копия механики из NewPostFragment:
// ImagePicker -> viewModel.changePhoto(uri) -> при сохранении локальный файл
// грузится через endpoint media, URL подставляется как attachment события
@AndroidEntryPoint
class NewEventFragment : Fragment() {

    private val viewModel: NewEventViewModel by viewModels()
    private var fragmentBinding: FragmentNewEventBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewEventBinding.inflate(inflater, container, false)
        this.fragmentBinding = binding

        observeViewModel(binding)
        setupImagePicker(binding)
        setupMenu()

        binding.edit.requestFocus()

        return binding.root
    }

    // Методы

    private fun observeViewModel(binding: FragmentNewEventBinding) {
        viewModel.photo.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel.uri == null) {
                binding.photoContainer.visibility = View.GONE
            } else {
                binding.photoContainer.visibility = View.VISIBLE

                val uriString = photoModel.uri.toString()

                // Это удаленная ссылка или локальный файл?
                if (uriString.startsWith("http://") || uriString.startsWith("https://")) {
                    // Загружаем через Glide (так же, как в адаптере)
                    binding.photo.loadAttachment(uriString)
                } else {
                    // Загружаем локальный файл, выбранный пользователем
                    binding.photo.setImageURI(photoModel.uri)
                }
            }
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.errorEvent.observe(viewLifecycleOwner) { message ->
            fragmentBinding?.let {
                Snackbar.make(it.root, message, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setupImagePicker(binding: FragmentNewEventBinding) {
        val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Snackbar.make(binding.root, ImagePicker.getError(result.data), Snackbar.LENGTH_LONG).show()
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
            viewModel.changePhoto(null)
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
                menu.findItem(R.id.save)?.title = getString(R.string.publish)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeContent(it.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}
