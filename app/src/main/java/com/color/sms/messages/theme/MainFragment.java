package com.color.sms.messages.theme;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.activity.ComposeActivity;
import com.color.sms.messages.theme.activity.NewMessageActivity;
import com.color.sms.messages.theme.adapter.ConversationAdapter;
import com.color.sms.messages.theme.block.utilBlock.BlockListController;
import com.color.sms.messages.theme.databinding.ActivityMainBinding;
import com.color.sms.messages.theme.model.Conversation;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.ContactListController;
import com.color.sms.messages.theme.utils.MessageUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        ConversationAdapter.ConversationListener {

    private static final int REQUEST_GET_CONTACT = 162;
    private ActivityMainBinding binding;
    private ConversationAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container);
        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_main, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView();
        setUpRecyclerView();
    }

    protected void initLoader() {
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private void initView() {
        binding.swipeRefresh.setOnRefreshListener(this::initLoader);
        binding.fabNewMessage.setOnClickListener(v -> startActivityForResult(new Intent(requireContext(), NewMessageActivity.class), REQUEST_GET_CONTACT));
        binding.recyclerConversation.addOnScrollListener(getScrollListener());
    }

    private RecyclerView.OnScrollListener getScrollListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager manager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                assert manager != null;
                boolean enabled = manager.findFirstCompletelyVisibleItemPosition() == 0;
                binding.swipeRefresh.setEnabled(enabled);
            }
        };
    }

    private void setUpRecyclerView() {
        mAdapter = new ConversationAdapter(requireContext(), this);
        binding.recyclerConversation.setAdapter(mAdapter);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //set view
        binding.progressLoading.setVisibility(View.VISIBLE);
        binding.recyclerConversation.setVisibility(View.GONE);
        binding.fabNewMessage.setVisibility(View.GONE);

        Uri baseUri = Uri.parse("content://mms-sms/conversations?simple=true");

        String[] reqCols = new String[]{"*"};
        return new CursorLoader(requireContext(), baseUri, reqCols, null, null, "date DESC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        try {
            if (loader.getId() == 0) {
                binding.progressLoading.setVisibility(View.GONE);
                binding.recyclerConversation.setVisibility(View.VISIBLE);
                binding.fabNewMessage.setVisibility(View.VISIBLE);
                ArrayList<Conversation> mConversations = new ArrayList<>();
                BlockListController blockListController = new BlockListController();

                String[] reqCols = new String[]{"_id", "recipient_ids", "message_count", "snippet", "date", "read"};
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    while (cursor.moveToNext()) {
                        int msg_count = cursor.getInt(cursor.getColumnIndex(reqCols[2]));
                        String address = MessageUtils.getContact(requireContext(), cursor.getString(cursor.getColumnIndex(reqCols[1])));

                        if (msg_count > 0 && !(address == null || address.equalsIgnoreCase(""))) {
                            Conversation conversation = new Conversation();
                            conversation.setBlocked(blockListController.isInBlacklist(address));
                            conversation.setId(cursor.getInt(cursor.getColumnIndex(reqCols[0])));
                            conversation.setRecipientIds(cursor.getString(cursor.getColumnIndex(reqCols[1])));
                            conversation.setMessageCount(cursor.getInt(cursor.getColumnIndex(reqCols[2])));
                            conversation.setSnippet(cursor.getString(cursor.getColumnIndex(reqCols[3])));
                            conversation.setDate(cursor.getLong(cursor.getColumnIndex(reqCols[4])));
                            conversation.setRead(cursor.getInt(cursor.getColumnIndex(reqCols[5])));
                            conversation.setAddress(MessageUtils.getContact(requireContext(), cursor.getString(cursor.getColumnIndex(reqCols[1]))));
                            mConversations.add(conversation);
                        }
                    }
                }

                mAdapter.setData(mConversations);

            }
        } catch (Exception e) {
            Log.e("Ex", "onLoadFinished: " + e.toString());
        } finally {
            if (binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
            binding.progressLoading.setVisibility(View.GONE);
            binding.recyclerConversation.setVisibility(View.VISIBLE);
            binding.fabNewMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ContactListController.getInstance().init();
        initLoader();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void onConversationClicked(Conversation conversation) {
        if (conversation.getRead() == 0) {
            MessageUtils.markMessageRead(requireContext(), conversation.getAddress());
        }
        Intent intent = new Intent(requireContext(), ComposeActivity.class);
        intent.putExtra("id", conversation.getId());
        intent.putExtra("phone", conversation.getAddress());
        intent.putExtra("color", conversation.getColor());
        intent.putExtra("type", Constants.TYPE_CONVERSATION);
        startActivityForResult(intent, Constants.REQUEST_NONE);
    }
}
