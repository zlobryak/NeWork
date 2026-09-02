package ru.netology.nework.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R

class PostLoadStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<PostLoadStateAdapter.LoadStateViewHolder>() {

    class LoadStateViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val errorMsg: TextView = view.findViewById(R.id.error_msg)
        val retryButton: Button = view.findViewById(R.id.retry_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_load_state, parent, false)
        return LoadStateViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.progressBar.visibility =
            if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
        holder.retryButton.visibility =
            if (loadState is LoadState.Error) View.VISIBLE else View.GONE
        holder.errorMsg.visibility = if (loadState is LoadState.Error) View.VISIBLE else View.GONE

        holder.retryButton.setOnClickListener { retry() }
    }
}