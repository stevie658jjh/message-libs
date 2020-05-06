package com.color.sms.messages.theme.block.activity;

import android.Manifest;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.block.utilBlock.TextWatcherAbstract;
import com.color.sms.messages.theme.databinding.ActivityBlackBlockListBinding;
import com.color.sms.messages.theme.dialog.MyAlertDialog;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.Function;

import java.util.ArrayList;
import java.util.List;

public class BlackListActivity extends BaseActivity {
    private ActivityBlackBlockListBinding binding;
    private List<Contact> listSearch = new ArrayList<>();
    private BlacklistAdapter blacklistAdapter, searchListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_black_block_list);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_black_block_list);
        binding.setActivity(this);
        checkPermissionPhoneState();
    }

    private void checkPermissionPhoneState() {
        checkUserPermission(new PermissionListener() {
            @Override
            public void OnAcceptedAllPermission() {
                getBlacklist();
                init();
            }

            @Override
            public void OnCancelPermission() {
                MyAlertDialog.sShare.dialog(BlackListActivity.this, "Permission request", "We need to use those permissions in order to use this feature, turn it on again ?", dialog -> {
                    dialog.dismiss();
                    checkPermissionPhoneState();
                }, dialog -> {
                    dialog.dismiss();
                    finish();
                }).show();
            }

            @Override
            public void OnNeverRequestPermission() {
                MyAlertDialog.sShare.dialog(BlackListActivity.this, "Permission request", "We need to use those permissions in order to use this feature, open setting to turn it in ?", dialog -> {
                    Function.openDeviceSettingApp();
                    dialog.dismiss();
                    finish();
                }, dialog -> {
                    dialog.dismiss();
                    finish();
                }).show();
            }
        }, new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE});
    }

    private void init() {
        searchListAdapter = new BlacklistAdapter(listSearch);
        binding.rcSearch.setAdapter(searchListAdapter);
        binding.tvSearch.addTextChangedListener(new TextWatcherAbstract() {
            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    listSearch.clear();
                    binding.rcSearch.setVisibility(View.VISIBLE);
                    binding.rcContacts.setVisibility(View.GONE);
                    for (int i = 0; i <= Constants.contactList.size() - 1; i++) {
                        if (Constants.contactList.get(i).getName().toUpperCase().contains(s.toString().toUpperCase())
                                || Constants.contactList.get(i).getPhone().contains(s.toString())) {
                            listSearch.add(Constants.contactList.get(i));
                        }
                    }
                    searchListAdapter.notifyDataSetChanged();
                } else {
                    binding.rcSearch.setVisibility(View.GONE);
                    binding.rcContacts.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getBlacklist() {
        blacklistAdapter = new BlacklistAdapter(getContactList());
        binding.rcContacts.setAdapter(blacklistAdapter);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
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
}
