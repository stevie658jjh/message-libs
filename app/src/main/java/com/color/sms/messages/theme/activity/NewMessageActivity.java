package com.color.sms.messages.theme.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.adapter.ContactAdapter;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.utils.Constants;
import mabbas007.tagsedittext.TagsEditText;

public class NewMessageActivity extends BaseActivity implements ContactAdapter.ContactListener {

    private ImageView btnBack, btnKeyboard;
    private ImageView btnAddGroup;
    private TextView tvTitleToolbar;
    private TagsEditText tvSearch;
    private RecyclerView rcContact, rcSearch;
    private List<Contact> listContacts = new ArrayList<>();
    private List<Contact> listSearch = new ArrayList<>();
    private ContactAdapter contactAdapter, searchAdapter;
    private String defaultGroupName;
    private LinearLayout lnGroupConversation;
    private boolean isBack;
    private boolean isCreateGroup = true;
    private boolean isKeyboard = true;
    private TextInputEditText tvGroupName;
    private String[] contacts;
    private String[] phones;
    private String[] idContact;
    private LinearLayout layoutContactMain, layoutGroupName, layoutImgContact;
    private ProgressBar progressLoading;
    private LinearLayout layoutNewNumber;
    private TextView tvNewNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        listContacts.clear();

        initView();
        getContactWithAsynTask();
        onClick();
    }

    private void onClick() {
        lnGroupConversation.setOnClickListener(v -> {
            contactAdapter.setSelectedGroup(true);
            isBack = true;
            tvTitleToolbar.setText("New group conversation");
            lnGroupConversation.setVisibility(View.GONE);
        });
        btnKeyboard.setOnClickListener(v -> {
            if (isKeyboard) {
                isKeyboard = false;
                btnKeyboard.setImageResource(R.drawable.ic_keyboard);
                tvSearch.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                isKeyboard = true;
                btnKeyboard.setImageResource(R.drawable.ic_keyboard_number);
                tvSearch.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            tvSearch.requestFocus();
            showKeyboard();
        });
        btnBack.setOnClickListener(v -> {
            if (isBack) {
                isBack = false;
                tvTitleToolbar.setText("New conversation");
                tvSearch.setText("");
                lnGroupConversation.setVisibility(View.VISIBLE);
                contactAdapter.setContactSelected(null);
                contactAdapter.setSelectedGroup(false);
                contactAdapter.notifyDataSetChanged();
            } else {
                onBackPressed();
            }
        });
        btnAddGroup.setOnClickListener(v -> {
            if (isCreateGroup) {
                showKeyboard();
                isCreateGroup = false;
                isBack = false;
                tvSearch.clearFocus();
                layoutContactMain.setVisibility(View.GONE);
                layoutGroupName.setVisibility(View.VISIBLE);
                tvGroupName.requestFocus();

                contacts = new String[contactAdapter.getContactSelected().size()];
                phones = new String[contactAdapter.getContactSelected().size()];
                idContact = new String[contactAdapter.getContactSelected().size()];
                for (int i = 0; i < contactAdapter.getContactSelected().size(); i++) {
                    contacts[i] = contactAdapter.getContactSelected().get(i).getName();
                    phones[i] = contactAdapter.getContactSelected().get(i).getPhone();
                    idContact[i] = contactAdapter.getContactSelected().get(i).getId();
                }
                tvGroupName.setHint(TextUtils.join(", ", contacts));
                defaultGroupName = TextUtils.join(", ", contacts);

                for (Contact contact : contactAdapter.getContactSelected()) {
                    TextView tv = new TextView(this);
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen._20sdp)
                            , (int) getResources().getDimension(R.dimen._20sdp));

                    param.setMargins(0, 0, 10, 0);
                    tv.setLayoutParams(param);
                    tv.setAllCaps(true);
                    tv.setText(String.valueOf(contact.getName().charAt(0)).toLowerCase().trim());
                    tv.setGravity(Gravity.CENTER);
                    tv.setTextColor(getResources().getColor(R.color.white));
                    tv.setTextSize(14);
                    tv.setBackground(getDrawable(R.drawable.circle));
                    tv.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                    layoutImgContact.addView(tv);
                }
            } else {
                String groupName;
                if (tvGroupName.getText().toString().equalsIgnoreCase("")) {
                    groupName = defaultGroupName;
                } else {
                    groupName = tvGroupName.getText().toString();
                }
                Intent intent = new Intent(NewMessageActivity.this, ComposeActivity.class);
                intent.putExtra("type", Constants.TYPE_NEW_GROUP_MESSAGE);
                intent.putExtra("group_name", groupName);
                intent.putExtra("list_phone", phones);
                intent.putExtra("id_contact", idContact);
                intent.putParcelableArrayListExtra("contact", (ArrayList<? extends Parcelable>) contactAdapter.getContactSelected());
                startActivity(intent);
                finish();
            }
        });
        layoutNewNumber.setOnClickListener(v -> {
            Intent intent = new Intent(NewMessageActivity.this, ComposeActivity.class);
            intent.putExtra("phone", tvSearch.getText().toString());
            intent.putExtra("type", Constants.TYPE_NEW_MESSAGE);
            startActivity(intent);
            finish();
        });
    }

    private void initView() {
        btnKeyboard = findViewById(R.id.btn_keyboard);
        progressLoading = findViewById(R.id.progressLoading);
        tvSearch = findViewById(R.id.tv_search);
        rcContact = findViewById(R.id.rc_contacts);
        rcSearch = findViewById(R.id.rc_search);
        btnBack = findViewById(R.id.btn_back);
        btnAddGroup = findViewById(R.id.btn_add_group);
        tvTitleToolbar = findViewById(R.id.tv_title_toolbar);
        lnGroupConversation = findViewById(R.id.ln_group_conversation);
        layoutContactMain = findViewById(R.id.ln_main_contact);
        layoutGroupName = findViewById(R.id.ln_group_name);
        tvGroupName = findViewById(R.id.tv_group_name);
        layoutImgContact = findViewById(R.id.ln_img_contact);
        layoutNewNumber = findViewById(R.id.ln_new_number);
        tvNewNumber = findViewById(R.id.tv_new_number);

        searchAdapter = new ContactAdapter(listSearch, this);
        rcSearch.setLayoutManager(new LinearLayoutManager(this));
        rcSearch.setAdapter(searchAdapter);
        tvSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isBack) {
                    if (s.length() > 0) {
                        listSearch.clear();
                        rcSearch.setVisibility(View.VISIBLE);
                        rcContact.setVisibility(View.GONE);
                        lnGroupConversation.setVisibility(View.GONE);
                        if (!isKeyboard) {
                            layoutNewNumber.setVisibility(View.VISIBLE);
                        }
                        tvNewNumber.setText("Send to " + s.toString());

                        for (int i = 0; i <= listContacts.size() - 1; i++) {
                            Contact contact = listContacts.get(i);
                            if (contact.getName().toUpperCase().contains(s.toString().toUpperCase())
                                    || (contact.getPhone() != null && contact.getPhone().contains(s.toString()))) {
                                listSearch.add(contact);
                            }
                        }

                        searchAdapter.notifyDataSetChanged();
                    } else {
                        rcSearch.setVisibility(View.GONE);
                        rcContact.setVisibility(View.VISIBLE);
                        lnGroupConversation.setVisibility(View.VISIBLE);
                        layoutNewNumber.setVisibility(View.GONE);
                    }
                } else {
                    btnAddGroup.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
            }
        });
        tvSearch.setOnKeyListener((v, keyCode, event) -> {
            if (canClick() && keyCode == KeyEvent.KEYCODE_DEL && ((TagsEditText) v).getTags() != null && ((TagsEditText) v).getTags().size() > 0) {
                contactAdapter.removeItem(((TagsEditText) v).getTags().get(((TagsEditText) v).getTags().size() - 1));
            }
            return false;
        });
    }

    private void getContactWithAsynTask() {
        listContacts = getContactList();
        layoutContactMain.setVisibility(View.VISIBLE);
        progressLoading.setVisibility(View.GONE);
        tvSearch.requestFocus();
        showKeyboard();
        contactAdapter = new ContactAdapter(listContacts, NewMessageActivity.this);
        rcContact.setLayoutManager(new LinearLayoutManager(NewMessageActivity.this));
        rcContact.setNestedScrollingEnabled(false);
        rcContact.setAdapter(contactAdapter);
    }

    private List<Contact> getContactList() {
        List<Contact> contacts = new ArrayList<>();
        String name = "";
        for (Contact contact : Constants.contactList) {
            Contact contactAdding = new Contact();
            if (name.isEmpty() || !name.equalsIgnoreCase(contact.getNameA())) {
                name = contact.getNameA();
                contactAdding.setName(name);
                contacts.add(contactAdding);
            }
            contacts.add(contact);
        }
        return contacts;
    }

    @Override
    public void onContactSelected(Contact contact) {
        Intent intent = new Intent();
        intent.putExtra(Constants.PUT_OBJ, contact);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onContactSelected(Contact contact, boolean isAdd) {
        String[] contacts = new String[contactAdapter.getContactSelected().size()];
        for (int i = 0; i < contactAdapter.getContactSelected().size(); i++) {
            contacts[i] = contactAdapter.getContactSelected().get(i).getName();
        }
        tvSearch.setTags(contacts);
    }
}
