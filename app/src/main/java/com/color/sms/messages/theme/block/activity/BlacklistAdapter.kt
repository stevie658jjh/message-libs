package com.color.sms.messages.theme.block.activity

import android.graphics.PorterDuff
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.color.sms.messages.theme.databinding.ListItemContactBlackBinding
import com.color.sms.messages.theme.R
import com.color.sms.messages.theme.activity.search.adapter.glide4Engine
import com.color.sms.messages.theme.adapter.TextOnlyViewHolder
import com.color.sms.messages.theme.block.utilBlock.BlockListController
import com.color.sms.messages.theme.model.Contact

class BlacklistAdapter(val data: List<Contact>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var contactSelected = arrayListOf<Contact>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == -1) {
            TextOnlyViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.list_item_text_only, parent, false))
        } else {
            BlacklistViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.list_item_contact_black, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (TextUtils.isEmpty(data[position].phone)) return -1
        return 0
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == -1) {
            (holder as TextOnlyViewHolder).bind(data[position].name)
        } else {
            (holder as BlacklistViewHolder).bind(data[position])
        }
    }

    fun removeItem(name: String) {
        for (i in contactSelected.indices) {
            if (contactSelected[i].name.equals(name, ignoreCase = true)) {
                contactSelected.removeAt(i)
            }
        }
        for (i in data.indices) {
            if (data[i].name.equals(name, ignoreCase = true)) {
                notifyItemChanged(i)
            }
        }
    }
}

class BlacklistViewHolder(val binding: ListItemContactBlackBinding) : RecyclerView.ViewHolder(binding.root) {
    private val blockListController = BlockListController()
    var contact: Contact? = null
    fun bind(contact: Contact) {
        this.contact = contact
        binding.item = contact
        if (!TextUtils.isEmpty(contact.photoUri)) {
            binding.imgAvatar.visibility = View.VISIBLE
            glide4Engine.loadImage(binding.imgAvatar, contact.photoUri)
        } else {
            binding.contactBackground.background.setColorFilter(contact.color, PorterDuff.Mode.SRC_OVER)
            binding.imgAvatar.visibility = View.GONE
        }
        binding.switchItem.isChecked = blockListController.isInBlacklist(contact.phone)

        binding.switchItem.setOnCheckedChangeListener { buttonView, isChecked ->
            run {
                if (buttonView.isPressed) {
                    if (isChecked) {
                        blockListController.addIntoBlacklist(contact.phone)
                    } else {
                        blockListController.deleteOutOfBlacklist(contact.phone)
                    }
                }
            }
        }

    }
}
