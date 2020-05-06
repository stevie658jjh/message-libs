package com.color.sms.messages.theme.adapter

import androidx.recyclerview.widget.RecyclerView
import com.color.sms.messages.theme.databinding.ListItemTextOnlyBinding

class TextOnlyViewHolder(val binding: ListItemTextOnlyBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(name: String) {
        binding.tvText.text = name
    }
}
