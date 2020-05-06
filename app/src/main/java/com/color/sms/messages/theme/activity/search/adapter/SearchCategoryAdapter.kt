package com.color.sms.messages.theme.activity.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.activity.search.model.Search
import com.color.sms.messages.theme.databinding.ListItemCategoryBinding
import com.color.sms.messages.theme.utils.MyApplication

class SearchCategoryAdapter(val data: List<Search.Category>, val listener: SearchCategoryListener) : RecyclerView.Adapter<SearchCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SearchCategoryViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.list_item_category, parent, false))

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: SearchCategoryViewHolder, position: Int) = holder.bind(data[position], listener)
}

class SearchCategoryViewHolder(val binding: ListItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(category: Search.Category, listener: SearchCategoryListener) {
        binding.category = category
//        glide4Engine.loadImage(binding.img, category.icon)
        Glide.with(MyApplication.getInstance()).load(category.icon).into(binding.img)
        binding.root.setOnClickListener {
            listener.onCategorySelected(category)
        }
    }
}

interface SearchCategoryListener {
    fun onCategorySelected(category: Search.Category)
}
