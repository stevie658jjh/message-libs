package lib.message.theme;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.databinding.DataBindingUtil;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import lib.message.theme.activity.ComposeActivity;
import lib.message.theme.activity.GroupMessageActivity;
import lib.message.theme.activity.NewMessageActivity;
import lib.message.theme.activity.search.SearchMessagesActivity;
import lib.message.theme.adapter.ConversationAdapter;
import lib.message.theme.base.BaseActivity;
import lib.message.theme.block.activity.BlackListActivity;
import lib.message.theme.block.utilBlock.BlockListController;
import lib.message.theme.databinding.ActivityMainBinding;
import lib.message.theme.model.Contact;
import lib.message.theme.model.Conversation;
import lib.message.theme.utils.Constants;
import lib.message.theme.utils.ContactListController;
import lib.message.theme.utils.MessageUtils;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        ConversationAdapter.ConversationListener {

    private static final int REQUEST_GET_CONTACT = 162;
    private static final int REQUEST_BLOCK_CHANGE = 126;
    private ActivityMainBinding binding;
    private ConversationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setUpToolbar();
        initView();
        setUpRecyclerView();
    }

    protected void initLoader() {
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    private void initView() {
        binding.swipeRefresh.setOnRefreshListener(this::initLoader);
        binding.fabNewMessage.setOnClickListener(v -> startActivityForResult(new Intent(MainActivity.this, NewMessageActivity.class), REQUEST_GET_CONTACT));
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
        mAdapter = new ConversationAdapter(this, this);
        binding.recyclerConversation.setAdapter(mAdapter);
    }

    private void setUpToolbar() {

//        binding.toolbar.setTitle("Messages");
//        setSupportActionBar(binding.toolbar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_NONE && resultCode == Constants.RESULT_CHANGE) {
//            finish();
//            startActivity(getIntent());
        }

        if (requestCode == REQUEST_PERMISSION_SMS) {
            if (resultCode == Activity.RESULT_OK) {
                initLoader();
                ContactListController.getInstance().init();
            }
        }

        if (requestCode == REQUEST_BLOCK_CHANGE && resultCode == Activity.RESULT_OK) {
            mAdapter.updateBlackList();
        }

        if (requestCode == REQUEST_GET_CONTACT) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Contact contact = data.getParcelableExtra(Constants.PUT_OBJ);
                Intent intent = new Intent(this, ComposeActivity.class);
                intent.putExtra("phone", contact.getPhone());
                intent.putExtra("color", contact.getColor());
                intent.putExtra("type", Constants.TYPE_NEW_MESSAGE);
                startActivityForResult(intent, Constants.REQUEST_NONE);
            }
        }
    }

    protected boolean isDefaultSmsApp() {
        return getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(this));
    }

    protected void showDefaultSmsDialog() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        if (!Telephony.Sms.getDefaultSmsPackage(this).equals(getPackageName())) {
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        }
        this.startActivityForResult(intent, REQUEST_PERMISSION_SMS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            menu.getItem(4).setVisible(false);
            menu.getItem(5).setVisible(true);
        } else {
            menu.getItem(5).setVisible(false);
            menu.getItem(4).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.block) {
            startActivityForResult(new Intent(this, BlackListActivity.class), REQUEST_BLOCK_CHANGE);
        } else if (itemId == R.id.dark_mode || itemId == R.id.light_mode) {
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            finish();
            startActivity(new Intent(MainActivity.this, MainActivity.this.getClass()));
        } else if (itemId == R.id.help) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:themecolor.technology@gmail.com"));
            startActivity(Intent.createChooser(emailIntent, "Send feedback"));
        } else if (itemId == R.id.group_message) {
            startActivity(new Intent(this, GroupMessageActivity.class));
        } else if (itemId == R.id.search) {
            startActivity(new Intent(this, SearchMessagesActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
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
        return new CursorLoader(getApplicationContext(), baseUri, reqCols, null, null, "date DESC");
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
                        String address = MessageUtils.getContact(this, cursor.getString(cursor.getColumnIndex(reqCols[1])));

                        if (msg_count > 0 && !(address == null || address.equalsIgnoreCase(""))) {
                            Conversation conversation = new Conversation();
                            conversation.setBlocked(blockListController.isInBlacklist(address));
                            conversation.setId(cursor.getInt(cursor.getColumnIndex(reqCols[0])));
                            conversation.setRecipientIds(cursor.getString(cursor.getColumnIndex(reqCols[1])));
                            conversation.setMessageCount(cursor.getInt(cursor.getColumnIndex(reqCols[2])));
                            conversation.setSnippet(cursor.getString(cursor.getColumnIndex(reqCols[3])));
                            conversation.setDate(cursor.getLong(cursor.getColumnIndex(reqCols[4])));
                            conversation.setRead(cursor.getInt(cursor.getColumnIndex(reqCols[5])));
                            conversation.setAddress(MessageUtils.getContact(this, cursor.getString(cursor.getColumnIndex(reqCols[1]))));
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
    protected void onResume() {
        super.onResume();
        if (!isDefaultSmsApp()) {
            showDefaultSmsDialog();
        } else {
            ContactListController.getInstance().init();
            initLoader();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    @Override
    public void onConversationClicked(Conversation conversation) {
        if (conversation.getRead() == 0) {
            MessageUtils.markMessageRead(this, conversation.getAddress());
        }
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra("id", conversation.getId());
        intent.putExtra("phone", conversation.getAddress());
        intent.putExtra("color", conversation.getColor());
        intent.putExtra("type", Constants.TYPE_CONVERSATION);
        startActivityForResult(intent, Constants.REQUEST_NONE);
    }
}
