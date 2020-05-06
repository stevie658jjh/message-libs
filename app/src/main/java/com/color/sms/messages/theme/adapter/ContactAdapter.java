package com.color.sms.messages.theme.adapter;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.databinding.ListItemTextOnlyBinding;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.utils.Glide4Engine;
import de.hdodenhof.circleimageview.CircleImageView;

public class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = -1;
    private List<Contact> contacts;
    private ContactListener listener;
    private boolean isSelectedGroup;
    private List<Contact> contactSelected;
    private Glide4Engine glide4Engine;

    public ContactAdapter(List<Contact> contacts, ContactListener listener) {
        this.listener = listener;
        this.contacts = contacts;
        this.glide4Engine = new Glide4Engine();
    }

    public void setContactSelected(List<Contact> contactSelected) {
        this.contactSelected = contactSelected;
    }

    public void setSelectedGroup(boolean selectedGroup) {
        isSelectedGroup = selectedGroup;
    }

    public List<Contact> getContactSelected() {
        return contactSelected;
    }

    public void removeItem(String name) {
        for (int i = 0; i < contactSelected.size(); i++) {
            if (contactSelected.get(i).getName().equalsIgnoreCase(name)) {
                contactSelected.remove(i);
            }
        }
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getName().equalsIgnoreCase(name)) {
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (contacts.get(position) == null || contacts.get(position).getPhone() == null)
            return HEADER;
        return 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        if (i == HEADER) {
            ListItemTextOnlyBinding binding = DataBindingUtil.inflate(layoutInflater, R.layout.list_item_text_only, viewGroup, false);
            return new TextOnlyViewHolder(binding);
        } else {
            View view = layoutInflater
                    .inflate(R.layout.list_item_contact, viewGroup, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolderParent, int position) {
        Contact contact = contacts.get(position);
        if (getItemViewType(position) == HEADER) {
            ((TextOnlyViewHolder) viewHolderParent).bind(contact.getName());
        } else {
            ViewHolder viewHolder = (ViewHolder) viewHolderParent;
            if (contact.getPhotoUri() != null) {
                glide4Engine.loadImage(viewHolder.imgAvatar, contact.getPhotoUri());
                viewHolder.imgAvatar.setVisibility(View.VISIBLE);
            } else {
                viewHolder.contactBackground.getBackground().setColorFilter(contact.getColor(), PorterDuff.Mode.SRC_OVER);
                viewHolder.imgAvatar.setVisibility(View.GONE);
            }
            viewHolder.tvType.setText(contact.getTypeString());
            viewHolder.tvImage.setText(String.valueOf(contact.getName().charAt(0)));
            viewHolder.tvName.setText(contact.getName());
            viewHolder.tvPhone.setText(contact.getPhone());
            viewHolder.imgSelected.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contacts == null ? 0 : contacts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvImage, tvName, tvPhone, tvType;
        ImageView imgSelected;
        RelativeLayout contactBackground;
        CircleImageView imgAvatar;
        LinearLayout itemLayout;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvImage = itemView.findViewById(R.id.tvImage);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            imgSelected = itemView.findViewById(R.id.imgSelected);
            tvType = itemView.findViewById(R.id.tvType);
            contactBackground = itemView.findViewById(R.id.contactBackground);

            itemView.setOnClickListener(v -> {
                if (isSelectedGroup) {
                    if (check(contacts.get(getAdapterPosition()))) {
                        imgSelected.setVisibility(View.VISIBLE);
                        listener.onContactSelected(contacts.get(getAdapterPosition()), true);
                    } else {
                        imgSelected.setVisibility(View.GONE);
                        listener.onContactSelected(contacts.get(getAdapterPosition()), false);
                    }
                } else {
                    listener.onContactSelected(contacts.get(getAdapterPosition()));
                }
            });
        }

        private boolean check(Contact contact) {
            if (contactSelected == null || contactSelected.size() == 0) {
                contactSelected = new ArrayList<>();
                contactSelected.add(contact);
                return true;
            }
            for (Contact c : contactSelected) {
                if (contact.getPhone().equalsIgnoreCase(c.getPhone())) {
                    contactSelected.remove(c);
                    return false;
                }
            }

            contactSelected.add(contact);
            return true;
        }
    }

    public interface ContactListener {
        void onContactSelected(Contact contact);

        void onContactSelected(Contact contact, boolean isAdd);
    }
}
