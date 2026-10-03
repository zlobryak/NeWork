package ru.netology.nework.ui.adapters.usersFeed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.ItemLoadStateBinding

/**
 * Ячейка состояния загрузки (прогресс / ошибка + retry) для списка пользователей.
 */
class UsersLoadStateAdapter(
    private val onRetryClickListener: () -> Unit,
) : LoadStateAdapter<UsersLoadStateAdapter.UsersLoadStateViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): UsersLoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UsersLoadStateViewHolder(binding, onRetryClickListener)
    }

    override fun onBindViewHolder(holder: UsersLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class UsersLoadStateViewHolder(
        private val binding: ItemLoadStateBinding,
        private val onRetryClickListener: () -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.progressBar.visibility =
                if (loadState is LoadState.Loading) android.view.View.VISIBLE
                else android.view.View.GONE
            binding.retryButton.visibility =
                if (loadState is LoadState.Error) android.view.View.VISIBLE
                else android.view.View.GONE
            binding.errorMsg.visibility =
                if (loadState is LoadState.Error) android.view.View.VISIBLE
                else android.view.View.GONE
            binding.retryButton.setOnClickListener { onRetryClickListener.invoke() }
        }
    }
}
