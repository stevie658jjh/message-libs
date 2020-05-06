package com.color.sms.messages.theme.activity.search.adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.color.sms.messages.theme.databinding.ListItemContactGridBinding
import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.activity.search.model.Search
import com.color.sms.messages.theme.utils.Glide4Engine

var glide4Engine = Glide4Engine()

class SearchContactAdapter(val data: List<Search.ContactConversation>, val listener: SearchContactListener) : RecyclerView.Adapter<SearchContactViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchContactViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_contact_grid, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SearchContactViewHolder, position: Int) = holder.bind(data[position], listener)
}

class SearchContactViewHolder(val binding: ListItemContactGridBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(contactConversation: Search.ContactConversation, listener: SearchContactListener) {
        binding.contact = contactConversation.contact
        if (contactConversation.contact.photoUri != null) {
            binding.imgAvatar.visibility = View.VISIBLE
            glide4Engine.loadImage(binding.imgAvatar, contactConversation.contact.photoUri)
        } else {
            binding.imgAvatar.visibility = View.GONE
            binding.contactBackground.background.setColorFilter(contactConversation.contact.color, PorterDuff.Mode.SRC_OVER)
        }

        binding.root.setOnClickListener {
            listener.onContactSelected(contactConversation)
        }
    }
}

interface SearchContactListener {
    fun onContactSelected(contact: Search.ContactConversation)
}
