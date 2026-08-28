package ru.netology.nework.ui.fragments

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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.StringArg
import ru.netology.nework.ui.viewmodel.PostViewModel
import androidx.navigation.fragment.navArgs
import androidx.core.net.toUri
import ru.netology.nework.view.loadAttachment

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
        private const val DRAFT_KEY = "new_post_draft"
    }

    private val args: NewPostFragmentArgs by navArgs()
    private val viewModel: PostViewModel by activityViewModels()
    private var fragmentBinding: FragmentNewPostBinding? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("PostDraftPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        this.fragmentBinding = binding

        //  Определяем режим: редактирование или создание
        val postToEdit = args.postItemArg
        val isEditMode = postToEdit != null

        // Инициализируем состояние ViewModel (Единая точка входа для данных)
        if (isEditMode) {
            viewModel.edit(postToEdit)
        } else {
            val initialText = arguments?.textArg ?: sharedPreferences.getString(DRAFT_KEY, "")
            if (!initialText.isNullOrEmpty()) {
                viewModel.changeContent(initialText)
            }
        }

        observeViewModel(binding, isEditMode)

        // Настраиваем взаимодействие с UI
        setupImagePicker(binding)
        setupMenu(isEditMode)
        setupBackPressed(isEditMode, binding)

        // Запрашиваем фокус только при создании нового поста
        if (!isEditMode) {
            binding.edit.requestFocus()
        }

        return binding.root
    }

    // Методы

    private fun observeViewModel(binding: FragmentNewPostBinding, isEditMode: Boolean) {
        viewModel.edited.observe(viewLifecycleOwner) { post ->
            // Всегда берем текст из ViewModel, чтобы избежать рассинхронизации
            binding.edit.setText(post.content)

            // Если это редактирование и есть вложение, инициализируем фото
            if (isEditMode && post.attachment?.url != null) {
                viewModel.changePhoto(post.attachment.url.toUri())
            }
        }

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

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    private fun setupImagePicker(binding: FragmentNewPostBinding) {
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

    private fun setupMenu(isEditMode: Boolean) {
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
                menu.findItem(R.id.save)?.title = if (isEditMode) getString(R.string.save) else getString(R.string.publish)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeContent(it.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())

                            // Очищаем черновик только при создании нового поста
                            if (!isEditMode) {
                                sharedPreferences.edit { remove(DRAFT_KEY) }
                            }
                        }
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)
    }

    private fun setupBackPressed(isEditMode: Boolean, binding: FragmentNewPostBinding) {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val draftText = binding.edit.text?.toString()?.trim()

                // Сохраняем черновик только для новых постов, если текст не пустой
                if (!isEditMode && !draftText.isNullOrEmpty()) {
                    sharedPreferences.edit { putString(DRAFT_KEY, draftText) }
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}

//TODO НА этот экран не должен попадать неавторизованный пользователь
//TODO Сохранять черновик