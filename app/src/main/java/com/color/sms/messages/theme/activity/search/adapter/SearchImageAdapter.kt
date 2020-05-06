package com.color.sms.messages.theme.activity.search.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.color.sms.messages.theme.databinding.ListItemImageBinding
import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.activity.search.model.Search

class SearchImageAdapter(val list: MutableList<Search.MMS>, val listener: ImageSearchClickListener, val typeLoad: Search.NAME) : RecyclerView.Adapter<ImageSearchViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ImageSearchViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_image, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ImageSearchViewHolder, position: Int) = holder.bind(list[position], typeLoad, listener)
}

class ImageSearchViewHolder(val binding: ListItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(data: Search.MMS, typeLoad: Search.NAME, listener: ImageSearchClickListener) {
        if (typeLoad.name.contains(Search.NAME.Videos.name)) {
            binding.video.visibility = View.VISIBLE
        } else {
            binding.video.visibility = View.GONE
        }

        binding.uri = data.image
        binding.root.setOnClickListener { listener.onImageClicked(data, typeLoad) }
    }
}

interface ImageSearchClickListener {
    fun onImageClicked(data: Search.MMS, typeLoad: Search.NAME)
}