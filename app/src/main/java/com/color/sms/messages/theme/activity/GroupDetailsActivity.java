package com.color.sms.messages.theme.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.Function;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailsActivity extends BaseActivity {

    private String groupName;
    private String groupNumbers;
    private String groupIdContact;
    private String[] numbers;
    private String[] names;
    private String[] idContact;
    private List<Contact> contacts;
    private String type;
    private ContactAdapter contactAdapter;
    private ImageView btnEditGroupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        type = getIntent().getStringExtra("type");
        groupName = getIntent().getStringExtra("group_name");
        groupNumbers = getIntent().getStringExtra("group_number");
        contacts = getIntent().getParcelableArrayListExtra("contact");
        groupIdContact = getIntent().getStringExtra("id_contact");

        if (type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
            String delimiter = ", ";
            numbers = groupNumbers.split(delimiter);
            names = groupName.split(delimiter);
            idContact = groupIdContact.split(delimiter);
        }

        initView();
        setUpRecyclerView();
    }

    private void initView() {
        TextView tvGroupName = findViewById(R.id.group_name);
        btnEditGroupName = findViewById(R.id.btnEdit);

        tvGroupName.setText(groupName);
        btnEditGroupName.setOnClickListener(v -> {
        });

        findViewById(R.id.btn_back).setOnClickListener(v -> onBackPressed());
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_group_contact);
        if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE)) {
            contactAdapter = new ContactAdapter(contacts);
        } else {
            contactAdapter = new ContactAdapter(addContact(numbers));
        }
        recyclerView.setAdapter(contactAdapter);
    }

    private List<Contact> addContact(String[] numbers) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < numbers.length; i++) {
            Contact contact = new Contact();
            contact.setId(idContact[i]);
            contact.setName(names[i]);
            contact.setPhone(numbers[i]);
            contacts.add(contact);
        }
        return contacts;
    }

    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        private List<Contact> contacts;

        ContactAdapter(List<Contact> contacts) {
            this.contacts = contacts;
        }

        @NonNull
        @Override
        public ContactAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.list_item_contact_group, viewGroup, false);
            return new ContactAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder viewHolder, int position) {
            viewHolder.tvImage.setText(String.valueOf(contacts.get(position).getName().charAt(0)));
            viewHolder.tvName.setText(contacts.get(position).getName());
            viewHolder.tvPhone.setText(contacts.get(position).getPhone());
        }

        @Override
        public int getItemCount() {
            return contacts == null ? 0 : contacts.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvImage, tvName, tvPhone;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvImage = itemView.findViewById(R.id.tvImage);
                tvName = itemView.findViewById(R.id.tvName);
                tvPhone = itemView.findViewById(R.id.tv_phone);

                itemView.setOnClickListener(v -> Function.detailsContactIntent(contacts.get(getAdapterPosition()).getId()));
            }
        }
    }
}
