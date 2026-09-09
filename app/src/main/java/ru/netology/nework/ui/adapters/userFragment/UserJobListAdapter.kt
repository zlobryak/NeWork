package ru.netology.nework.ui.adapters.userFragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.data.dto.job.JobItem
import ru.netology.nework.databinding.JobCardBinding
import ru.netology.nework.ui.adapters.userFragment.UserJobListAdapter.JobViewHolder
import ru.netology.nework.utils.DateUtils

class UserJobListAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val isMyJobs: Boolean
) : ListAdapter<JobItem, JobViewHolder>(JOB_COMPARATOR) {

    interface OnInteractionListener {
        fun onRemove(job: JobItem) {}
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobViewHolder {
        val binding = JobCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListener, isMyJobs)
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    class JobViewHolder(
        private val binding: JobCardBinding,
        private val onInteractionListener: OnInteractionListener,
        private val isMyJobs: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(job: JobItem) {
            binding.apply {
                name.text = job.name
                start.text = DateUtils.formatIsoDate(job.start)
                finish.text = DateUtils.formatIsoDate(job.finish)
                position.text = job.position

                deleteButton.visibility =
                    if (isMyJobs) View.VISIBLE else View.INVISIBLE

                deleteButton.setOnClickListener {
                    onInteractionListener.onRemove(job)
                }
            }
        }
    }


    companion object {        private val JOB_COMPARATOR: DiffUtil.ItemCallback<JobItem> =
            object : DiffUtil.ItemCallback<JobItem>() {
                override fun areItemsTheSame(oldItem: JobItem, newItem: JobItem): Boolean =
                    oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: JobItem, newItem: JobItem): Boolean =
                    oldItem == newItem
            }
    }
}
