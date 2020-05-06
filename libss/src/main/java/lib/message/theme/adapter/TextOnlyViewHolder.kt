package lib.message.theme.adapter

import androidx.recyclerview.widget.RecyclerView
import lib.message.theme.databinding.ListItemTextOnlyBinding

class TextOnlyViewHolder(val binding: ListItemTextOnlyBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(name: String) {
        binding.tvText.text = name
    }
}
