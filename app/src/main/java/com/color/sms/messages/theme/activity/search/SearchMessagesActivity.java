package com.color.sms.messages.theme.activity.search;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.ComposeActivity;
import com.color.sms.messages.theme.activity.PreviewMmsImageActivity;
import com.color.sms.messages.theme.activity.search.adapter.ImageLinkPreviewListener;
import com.color.sms.messages.theme.activity.search.adapter.ImageSearchClickListener;
import com.color.sms.messages.theme.activity.search.adapter.SearchCategoryAdapter;
import com.color.sms.messages.theme.activity.search.adapter.SearchCategoryListener;
import com.color.sms.messages.theme.activity.search.adapter.SearchContactAdapter;
import com.color.sms.messages.theme.activity.search.adapter.SearchContactListener;
import com.color.sms.messages.theme.activity.search.adapter.SearchImageAdapter;
import com.color.sms.messages.theme.activity.search.adapter.SearchLinkPreviewAdapter;
import com.color.sms.messages.theme.activity.search.model.Search;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.block.utilBlock.BlockListController;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.ContactListController;
import com.color.sms.messages.theme.utils.Function;
import com.color.sms.messages.theme.utils.MessageUtils;
import com.color.sms.messages.theme.utils.MyApplication;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import com.color.sms.messages.theme.databinding.ActivitySearchMessagesBinding;

public class SearchMessagesActivity extends BaseActivity implements SearchCategoryListener, SearchContactListener, ImageSearchClickListener, ImageLinkPreviewListener {
    private ActivitySearchMessagesBinding binding;

    private List<Search.MMS> listImage = new ArrayList<>();
    private List<Search.MMS> listVideo = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_messages);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_messages);

        init();
        createCategory();
        setOnClick();
        initLoader();
    }

    private void init() {
        binding.recyclerResults.setHasFixedSize(true);
    }

    protected void initLoader() {
        LoaderManager.getInstance(this).initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                Uri baseUri = Uri.parse("content://mms-sms/conversations?simple=true");
                String[] reqCols = new String[]{"_id", "recipient_ids"};
                return new CursorLoader(getApplicationContext(), baseUri, reqCols, null, null, "date DESC");
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
                String[] reqCols = new String[]{"_id", "recipient_ids"};
                List<Search.Contact> lisAddress = new ArrayList<>();
                BlockListController blockListController = new BlockListController();
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        long idConversation = cursor.getLong(cursor.getColumnIndex(reqCols[0]));
                        String address = MessageUtils.getContactNumber(getApplicationContext(), cursor.getLong(cursor.getColumnIndex(reqCols[1])));
                        if (!blockListController.isInBlacklist(address)) {
                            lisAddress.add(new Search.Contact(idConversation, address));
                        }
                    }
                }
                new Handler().postDelayed(() ->
                        getListContactMessages(lisAddress), 300);
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {
            }
        });
    }

    private void getListContactMessages(List<Search.Contact> lisAddress) {
        if (ContactListController.getListContact().size() > 0) {
            List<Search.ContactConversation> list = new ArrayList<>();
            for (Contact contact : ContactListController.getListContact()) {
                for (int i = 0; i < lisAddress.size(); i++) {
                    if (contact != null && contact.getPhone() != null && lisAddress.get(i).getAddress() != null && contact.getPhone()
                            .replace(" ", "").equals(lisAddress.get(i).getAddress().replace(" ", ""))
                            && !list.contains(contact)) {
                        list.add(new Search.ContactConversation(lisAddress.get(i).getIdConversation(), contact));
                    }
                }
            }
            setUpRecyclerPeople(list);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void loadData(Search.NAME typeLoad) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
                binding.edtSearch.setTags(typeLoad.name());
                binding.progressLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                @SuppressLint("Recycle") Cursor allMms = MyApplication.getInstance().getContentResolver().query(
                        Uri.parse("content://mms"),
                        null,
                        null,
                        null,
                        "date DESC");

                if (allMms != null) {
                    allMms.moveToFirst();
                    listImage = new ArrayList<>();
                    listVideo = new ArrayList<>();
                    while (allMms.moveToNext()) {
                        long mmsId = allMms.getLong(allMms.getColumnIndex("_id"));
                        long date = allMms.getLong(allMms.getColumnIndex("date"));
                        String user = MessageUtils.MmsTask.getAddressNumber(mmsId);
                        if (user == null) {
                            user = MessageUtils.getUserPhone(getApplicationContext());
                        }

                        ParseMMS(String.valueOf(mmsId), typeLoad, date, user);
                    }
                    allMms.close();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                binding.progressLoading.setVisibility(View.GONE);
                if (typeLoad == Search.NAME.Images) {
                    setListImage(listImage, typeLoad);
                } else if (typeLoad == Search.NAME.Videos) {
                    setListImage(listVideo, typeLoad);
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void loadLink() {
        new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected void onPreExecute() {
                binding.edtSearch.setTags("Links");
                binding.progressLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected List<String> doInBackground(Void... voids) {
                Uri baseUri = Uri.parse("content://sms/");
                List<String> uriMessage = new ArrayList<>();
                Cursor cursor = MyApplication.getInstance().getContentResolver().query(
                        baseUri,
                        null,
                        null,
                        null,
                        "date DESC");

                if (cursor != null) {
                    cursor.moveToFirst();
                    while (cursor.moveToNext()) {
                        String body = cursor.getString(cursor.getColumnIndex("body"));
                        if (Function.isLink(body)) {
                            uriMessage.addAll(Function.pullLinks(body));
                        }
                    }
                    cursor.close();
                }
                return uriMessage;
            }

            @Override
            protected void onPostExecute(List<String> strings) {
                binding.progressLoading.setVisibility(View.GONE);
                setListLinkPreview(strings);
            }
        }.execute();
    }

    public void ParseMMS(String id, Search.NAME typeLoad, long date, String user) {
        Uri uri = Uri.parse("content://mms/part");
        String mmsId = "mid = " + id;
        String[] projec = {"ct", "_id"};
        Cursor c = getContentResolver().query(uri, projec, mmsId, null, null);
        if (c != null) {
            c.moveToFirst();
            while (c.moveToNext()) {
                String type = c.getString(c.getColumnIndex("ct"));
                String partId = c.getString(c.getColumnIndex("_id"));
                if (type.contains(typeLoad.name().replace("s", "").toLowerCase())) {
                    if (typeLoad == Search.NAME.Images) {
                        listImage.add(new Search.MMS(getMmsImageUri(partId), date, user));
                    }
                }

                if (type.contains("video/3gpp")) {
                    listVideo.add(new Search.MMS(getMmsImageUri(partId), date, user));
                }
            }
            c.close();
        }
    }

    private void setListImage(List<Search.MMS> listImage, Search.NAME typeLoad) {
        binding.lnView.setVisibility(View.GONE);
        binding.lnResults.setVisibility(View.VISIBLE);
        if (listImage.size() > 0) {
            binding.recyclerResults.setLayoutManager(new GridLayoutManager(this, 4));
            SearchImageAdapter searchImageAdapter = new SearchImageAdapter(listImage, this, typeLoad);
            binding.recyclerResults.setAdapter(searchImageAdapter);
            binding.imgEmpty.setVisibility(View.GONE);
            binding.recyclerResults.setVisibility(View.VISIBLE);
        } else {
            binding.imgEmpty.setVisibility(View.VISIBLE);
            binding.recyclerResults.setVisibility(View.GONE);
        }
    }

    private void setListLinkPreview(List<String> urls) {
        binding.lnView.setVisibility(View.GONE);
        binding.lnResults.setVisibility(View.VISIBLE);
        if (urls.size() > 0) {
            binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            SearchLinkPreviewAdapter searchLinkPreviewAdapter = new SearchLinkPreviewAdapter(urls, this);
            binding.recyclerResults.setAdapter(searchLinkPreviewAdapter);
            binding.imgEmpty.setVisibility(View.GONE);
            binding.recyclerResults.setVisibility(View.VISIBLE);
        } else {
            binding.imgEmpty.setVisibility(View.VISIBLE);
            binding.recyclerResults.setVisibility(View.GONE);
        }
    }

    private void setOnClick() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void createCategory() {
        List<Search.Category> list = new ArrayList<>();
        list.add(new Search.Category(R.drawable.vector_video_category, Search.NAME.Videos));
        list.add(new Search.Category(R.drawable.vector_image_category, Search.NAME.Images));
        list.add(new Search.Category(R.drawable.vector_link, Search.NAME.Links));
        setUpRecyclerCategory(list);
    }

    private void setUpRecyclerCategory(List<Search.Category> list) {
        SearchCategoryAdapter searchCategoryAdapter = new SearchCategoryAdapter(list, this);
        binding.recyclerCategory.setAdapter(searchCategoryAdapter);
        binding.lnCategory.setVisibility(View.VISIBLE);
    }

    private void setUpRecyclerPeople(List<Search.ContactConversation> contacts) {
        if (contacts != null && contacts.size() > 0) {
            SearchContactAdapter searchContactAdapter = new SearchContactAdapter(contacts, this);
            binding.recyclerPeople.setAdapter(searchContactAdapter);
            binding.lnPeople.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCategorySelected(@NotNull Search.Category category) {
        switch (category.getName()) {
            case Videos:
            case Images:
                loadData(category.getName());
                break;
            case Links:
                loadLink();
                break;
        }
    }

    @Override
    public void onContactSelected(@NotNull Search.ContactConversation contactConversation) {
        onStartConversation(contactConversation);
    }

    private void onStartConversation(Search.ContactConversation contactConversation) {
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra("id", (int) contactConversation.getIdConversation());
        intent.putExtra("phone", contactConversation.getContact().getPhone());
        intent.putExtra("color", contactConversation.getContact().getColor());
        intent.putExtra("type", Constants.TYPE_CONVERSATION);
        startActivity(intent);
    }

    @Override
    public void onImageClicked(@NotNull Search.MMS data, @NotNull Search.NAME typeLoad) {
        onPreviewImage(data, typeLoad);
    }

    private void onPreviewImage(Search.MMS data, Search.NAME typeLoad) {
        Intent intent = new Intent(this, PreviewMmsImageActivity.class);
        intent.putExtra("type", typeLoad.toString());
        intent.putExtra("address", data.getAddress());
        intent.putExtra("image", String.valueOf(data.getImage()));
        intent.putExtra("date", data.getDate());
        startActivity(intent);
    }

    @Override
    public void onImageClicked(@NotNull String url) {
    }

    private Uri getMmsImageUri(String partId) {
        return Uri.parse("content://mms/part/" + partId);
    }

    @Override
    public void onBackPressed() {
        if (binding.lnView.getVisibility() == View.GONE) {
            binding.lnView.setVisibility(View.VISIBLE);
            binding.lnResults.setVisibility(View.GONE);
            binding.edtSearch.setText("");
            return;
        }
        super.onBackPressed();
    }
}
