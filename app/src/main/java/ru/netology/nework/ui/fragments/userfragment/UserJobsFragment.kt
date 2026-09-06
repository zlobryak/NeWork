package ru.netology.nework.ui.fragments.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.ui.adapters.UserJobListAdapter
import ru.netology.nework.ui.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {

    @Inject
    lateinit var repository: PostRepository
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: UserViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: UserJobListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.list.layoutManager = LinearLayoutManager(requireContext())

        val currentUserId = appAuth.authStateFlow.value.id.toInt()
        val profileUserId = viewModel.userId.value
        val isMyJobs = profileUserId == currentUserId

        adapter = UserJobListAdapter(
            isMyJobs = isMyJobs,
            onInteractionListener = object : UserJobListAdapter.OnInteractionListener {
                override fun onRemove(job: JobItem) {
                    viewModel.removeJob(job)
                }
            }
        )

        binding.list.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobsState.collect { resource ->
                    when (resource) {
                        is UserViewModel.Resource.Success -> {
                            // submitList автоматически применит DiffUtil и обновит UI
                            adapter.submitList(resource.data)
                        }
                        is UserViewModel.Resource.Error -> {
                            //TODO Показать ошибку
                        }
                        is UserViewModel.Resource.Loading -> {
                            //TODO Показать прогресс-бар
                        }
                        null -> {
                            // Пустое состояние
                        }
                    }
                }
            }
        }
    }
}