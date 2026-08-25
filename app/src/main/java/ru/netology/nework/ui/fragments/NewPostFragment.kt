package ru.netology.nework.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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
import ru.netology.nework.data.dto.PostItem
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.utils.AndroidUtils
import ru.netology.nework.utils.StringArg
import ru.netology.nework.ui.viewmodel.PostViewModel
import ru.netology.nework.utils.PostItemArg
import kotlin.getValue

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postItemArg: PostItem? by PostItemArg()

        private const val DRAFT_KEY = "new_post_draft"
    }

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
        val binding = FragmentNewPostBinding.inflate(
            inflater,
            container,
            false
        )
        this.fragmentBinding = binding

        //  Определяем режим: редактирование или создание
        val postToEdit = arguments?.postItemArg

        if (postToEdit != null) {
            // Режим редактирования: передаем пост в ViewModel
            viewModel.edit(postToEdit)
        } else {
            // Режим создания: проверяем аргумент текста или черновик
            val initialText = arguments?.textArg ?: sharedPreferences.getString(DRAFT_KEY, "")
            if (!initialText.isNullOrEmpty()) {
                viewModel.changeContent(initialText)
            }
        }

        // Реактивное заполнение UI из состояния ViewModel
        viewModel.edited.observe(viewLifecycleOwner) { post ->
            if (post.id != 0) { // Это существующий пост
                binding.edit.setText(post.content)
                viewModel.changePhoto(Uri.parse(post.attachment?.url))
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) {
            if (it.uri == null) {
                binding.photoContainer.visibility = View.GONE
                return@observe
            }

            binding.photoContainer.visibility = View.VISIBLE
            binding.photo.setImageURI(it.uri)
        }

        arguments?.textArg
            ?.let(binding.edit::setText)

        binding.edit.requestFocus()

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            ImagePicker.getError(it.data),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    Activity.RESULT_OK -> viewModel.changePhoto(it.data?.data)
                }
            }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(
                    arrayOf(
                        "image/png",
                        "image/jpeg",
                    )
                )
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.removePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }

        // Навигация при успешном создании/обновлении
        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        // Обработка кнопки "Сохранить" в меню
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
                menu.findItem(R.id.save)?.title = if (postToEdit != null) getString(R.string.save) else getString(R.string.publish)

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        fragmentBinding?.let {
                            viewModel.changeContent(it.edit.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                            if (postToEdit == null) {
                                sharedPreferences.edit { remove(DRAFT_KEY) }
                            }
                        }
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)

        // Обработка системной кнопки "Назад" для сохранения черновика
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val draftText = binding.edit.text?.toString()?.trim()
                // Сохраняем черновик только если мы в режиме создания и текст не пустой
                if (postToEdit == null && !draftText.isNullOrEmpty()) {
                    sharedPreferences.edit { putString(DRAFT_KEY, draftText) }
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)

        return binding.root
    }

    override fun onDestroyView() {
        fragmentBinding = null
        super.onDestroyView()
    }
}

//TODO Refactor