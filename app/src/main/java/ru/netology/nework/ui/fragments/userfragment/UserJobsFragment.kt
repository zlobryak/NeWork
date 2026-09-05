package ru.netology.nework.ui.fragments.userfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.data.repository.post.PostRepository
import ru.netology.nework.databinding.FragmentFeedBinding
import ru.netology.nework.ui.adapters.UserJobPagingAdapter
import ru.netology.nework.ui.adapters.UserWallPostPagingAdapter
import ru.netology.nework.ui.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
class UserJobsFragment : Fragment() {

    @Inject
    lateinit var repository: PostRepository

    private val viewModel: UserViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentFeedBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: UserJobPagingAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView и PagingDataAdapter
        binding.list.layoutManager = LinearLayoutManager(requireContext())

        adapter = UserJobPagingAdapter(
            object : UserWallPostPagingAdapter.OnInteractionListener {

            }

        )

    }
}