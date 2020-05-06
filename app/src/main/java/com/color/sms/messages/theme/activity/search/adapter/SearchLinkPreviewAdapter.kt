package com.color.sms.messages.theme.activity.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.color.sms.messages.theme.databinding.ListItemLinkPreviewBinding
import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.tools.linkPreview.ViewListener

class SearchLinkPreviewAdapter(val list: List<String>, val listener: ImageLinkPreviewListener) : RecyclerView.Adapter<SearchLinkPreviewViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchLinkPreviewViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_link_preview, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: SearchLinkPreviewViewHolder, position: Int) = holder.bind(list[position], listener)
}

class SearchLinkPreviewViewHolder(val binding: ListItemLinkPreviewBinding) : RecyclerView.ViewHolder(binding.root), ViewListener {
    private var url: String? = null
    override fun onSuccess(status: Boolean) {
        binding.progressLoading.visibility = View.GONE
        binding.viewError.visibility = View.GONE
    }

    override fun onError(e: Exception?) {
        binding.progressLoading.visibility = View.GONE
        binding.viewError.visibility = View.VISIBLE
        binding.tvLink.text = url
    }

    fun bind(url: String, listener: ImageLinkPreviewListener) {
        this.url = url
        binding.viewError.visibility = View.GONE
        binding.linkPreview.setLink(url, this)
        binding.root.setOnClickListener { listener.onImageClicked(url) }
    }
}

interface ImageLinkPreviewListener {
    fun onImageClicked(url: String)
}