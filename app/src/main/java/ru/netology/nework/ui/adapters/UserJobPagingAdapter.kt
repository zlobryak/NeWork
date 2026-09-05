package ru.netology.nework.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.data.dto.post.PostItem
import ru.netology.nework.databinding.JobCardBinding
import ru.netology.nework.databinding.PostCardBinding
import ru.netology.nework.ui.adapters.UserJobPagingAdapter.JobViewHolder

class UserJobPagingAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<JobItem, JobViewHolder>(JOB_COMPARATOR) {

    interface OnInteractionListener {
        fun onRemove(post: PostItem) {}
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {
        val binding = JobCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {
        val job = getItem(position)
        if (job != null) {
            holder.bind(job)
        }
    }

    class JobViewHolder(
        private val binding: JobCardBinding,
        private val onInteractionListener: OnInteractionListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: JobItem) {
            binding.apply {
                name.text = job.name
                start.text = job.start
                finish.text = job.finish
                position.text = job.position

                deleteButton.visibility =
                    if (
                //TODO Отображать кнопку удаления только для своих работ
                ) View.VISIBLE else View.INVISIBLE

            }
        }

    }


    companion object {
        // DiffUtil говорит Paging 3, как сравнивать посты, чтобы не перерисовывать весь список
        private val JOB_COMPARATOR: DiffUtil.ItemCallback<JobItem> =
            object : DiffUtil.ItemCallback<JobItem>() {
                override fun areItemsTheSame(oldItem: JobItem, newItem: JobItem): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: JobItem, newItem: JobItem): Boolean =
                    oldItem == newItem
            }
    }
}



