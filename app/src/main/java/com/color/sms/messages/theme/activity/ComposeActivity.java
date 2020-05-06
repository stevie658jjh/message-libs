package com.color.sms.messages.theme.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;

import com.color.sms.messages.theme.MainActivity;
import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.adapter.MessageAdapter;
import com.color.sms.messages.theme.base.BaseActivity;
import com.color.sms.messages.theme.dialog.DeleteConversationDialog;
import com.color.sms.messages.theme.fragment.media.MediaFragment;
import com.color.sms.messages.theme.model.Contact;
import com.color.sms.messages.theme.model.ConversationComing;
import com.color.sms.messages.theme.model.GroupMessage;
import com.color.sms.messages.theme.model.MediaAddingModel;
import com.color.sms.messages.theme.model.Message;
import com.color.sms.messages.theme.presenter.ComposePresenter;
import com.color.sms.messages.theme.presenter.MessagePresenter;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.FileController;
import com.color.sms.messages.theme.utils.Function;
import com.color.sms.messages.theme.utils.MessageGroupDBHelper;
import com.color.sms.messages.theme.utils.MessageUtils;
import com.color.sms.messages.theme.utils.MyApplication;
import com.color.sms.messages.theme.utils.SharedPreferenceHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.sucho.placepicker.AddressData;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComposeActivity extends BaseActivity implements MediaFragment.MediaListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private long mThreadId;
    private String mPhoneNumber = "";
    private int color;
    private String type = "";
    private String groupNumber;
    private String[] contactList;
    private String[] idContact;
    private String idContactGroup;
    private EditText mTvBody;
    private ImageView mImgSend;
    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Message> mMessageSms = new ArrayList<>();
    private List<Message> mMessageMMs = new ArrayList<>();
    private List<Contact> contacts;

    private ImageView btnAddMedia;
    private View viewAbsForFragment;
    private LinearLayoutCompat frameLayout;
    private MediaFragment mediaFragment;
    private BottomSheetBehavior<LinearLayoutCompat> bottomSheetBehavior;
    private View backgroundTransparent;
    private ImageView backgroundMain;
    private RelativeLayout borderBody;

    private ComposePresenter composePresenter;
    private MessagePresenter messagePresenter;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverUpdateNewMessage;

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelableArrayList("KEY", mMessageSms);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mMessageSms = savedInstanceState.getParcelableArrayList("KEY");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        type = getIntent().getStringExtra("type");
        color = getIntent().getIntExtra("color", R.color.colorPrimary);
        contactList = getIntent().getStringArrayExtra("list_phone");
        contacts = getIntent().getParcelableArrayListExtra("contact");

        if (type.equalsIgnoreCase(Constants.TYPE_CONVERSATION)) {
            mThreadId = getIntent().getIntExtra("id", 0);
            mPhoneNumber = getIntent().getStringExtra("phone");
        } else if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE)) {
            idContact = getIntent().getStringArrayExtra("id_contact");
            mPhoneNumber = getIntent().getStringExtra("group_name");
        } else if (type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
            mThreadId = getIntent().getIntExtra("id", 0);
            mPhoneNumber = getIntent().getStringExtra("group_name");
            groupNumber = getIntent().getStringExtra("group_number");
            idContactGroup = getIntent().getStringExtra("id_contact");
        } else {
            mPhoneNumber = getIntent().getStringExtra("phone");
            mPhoneNumber = mPhoneNumber.replaceAll("\\s+", "");
            mThreadId = MessageUtils.getOrCreateThreadId(this, mPhoneNumber);
        }

        MyApplication.setCurrentMessageId(mThreadId);

        initView();
        initLoader();
        initMediaAdding();
        initBottomSheet();
        setUpToolbar();

        onClick();
        onKeyboardListener();
        registerUpdateMessage();
    }

    private void loadMMs() {
        @SuppressLint("StaticFieldLeak")
        MessageUtils.MmsTask loadTask = new MessageUtils.MmsTask(this, (int) mThreadId) {
            @Override
            protected void onProgressUpdate(Message... prog) {
                if (this.activity.get() != null) {
                    if (prog != null) {
                        addMms(prog[0], mMessageMMs);
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                mMessageSms.addAll(mMessageMMs);

                Collections.sort(mMessageSms, (o1, o2) -> Long.compare(o1.getDate(), o2.getDate()));
                setUpRecyclerView();
            }
        };
        loadTask.execTask();
    }

    public void addMms(Message sms, List<Message> messages) {
        messages.add(sms);
        messages.size();
    }

    private void onKeyboardListener() {
        KeyboardVisibilityEvent.setEventListener(this,
                isOpen -> {
                    if (isOpen) {
                        if (viewAbsForFragment.getVisibility() == View.VISIBLE) {
                            hideMedia();
                        }
                    }
                });
    }

    private void initMediaAdding() {
        RecyclerView mediaView = findViewById(R.id.mediaView);
        messagePresenter = new MessagePresenter(this);
        mediaView.post(() -> composePresenter = new ComposePresenter(mediaView));
    }

    private void initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(frameLayout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                switch (state) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        viewAbsForFragment.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
            }
        });
        bottomSheetBehavior.setPeekHeight(BaseActivity.sKeyBoardHeight);
    }

    private void onClick() {
        mImgSend.setOnClickListener(v -> sendMessage());
        btnAddMedia.setOnClickListener(v -> {
            if (viewAbsForFragment.getVisibility() == View.GONE) {
                showMedia();
            } else {
                hideMedia();
            }
        });
    }

    public void onTakePhoto(View view) {
        checkUserPermission(new PermissionListener() {
            @Override
            public void OnAcceptedAllPermission() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }

            @Override
            public void OnCancelPermission() {
            }

            @Override
            public void OnNeverRequestPermission() {
            }
        }, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
    }

    private void sendMessage() {
        if (composePresenter.getMediaAdding() != null &&
                composePresenter.getMediaAdding().size() <= 0) {
            sendSMS(mTvBody.getText().toString());
        }
    }

    private void sendMMS(MediaAddingModel mediaAddingModel) {
        String body;
        if (mediaAddingModel.getType().equals(MediaAddingModel.Type.CONTACT)) {
            body = "Name: " + mediaAddingModel.getContact().getName() + "\n" + "Phone: " + mediaAddingModel.getContact().getPhone();
            sendSMS(body);
        } else {
            body = "" + mTvBody.getText().toString();
            com.klinker.android.send_message.Message messageSender = new com.klinker.android.send_message.Message(body, mPhoneNumber);
            messagePresenter.sendMessage(messageSender, mThreadId, mediaAddingModel);
            addTooRecyclerView(mediaAddingModel);
        }
        // reset view
        mTvBody.setText("");
        composePresenter.onMediaEmpty();
    }

    private void addTooRecyclerView(MediaAddingModel mediaAddingModel) {
        Message message = new Message();
        message.setDate(System.currentTimeMillis());
        message.setType(0);
        message.setUser(mPhoneNumber);
        switch (mediaAddingModel.getType()) {
            case GIF:
                message.setImage(Uri.parse(mediaAddingModel.getPath()));
                message.setGif(true);
                break;
            case ATTACH_IMAGE:
                message.setBitmap(mediaAddingModel.getBitmap());
                break;
            case EMOJI:
                message.setImage(Uri.parse(mediaAddingModel.getPath()));
                break;
            case AUDIO:
                message.setAudio(Uri.parse(mediaAddingModel.getPath()));
                break;
            case ATTACH_VIDEO:
                message.setVideo(Uri.parse(mediaAddingModel.getPath()));
                break;
            case ATTACH_FILE:
                break;
        }
        addNewMessageToRecyclerView(message);
    }

    private void sendSMS(String body) {
        if (body.length() > 0) {
            mTvBody.setText("");

            com.klinker.android.send_message.Message messageSender;
            if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE) || type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
                if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE)) {
                    MessageGroupDBHelper messageGroupDBHelper = new MessageGroupDBHelper(this);
                    GroupMessage groupMessage = new GroupMessage();
                    groupMessage.setGroupName(mPhoneNumber);
                    groupMessage.setDate(System.currentTimeMillis());
                    groupMessage.setGroupNumber(TextUtils.join(", ", contactList));
                    groupMessage.setContactId(TextUtils.join(", ", idContact));
                    groupMessage.setBody(body);
                    messageGroupDBHelper.addGroup(groupMessage);

                    messageSender = new com.klinker.android.send_message.Message(body,
                            contactList);
                    messagePresenter.sendMessage(messageSender, 0);
                } else if (type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
                    MessageGroupDBHelper messageGroupDBHelper = new MessageGroupDBHelper(this);

                    Message message1 = new Message();
                    message1.setId(mThreadId);
                    message1.setBody(body);
                    message1.setDate(System.currentTimeMillis());
                    messageGroupDBHelper.addMessage(message1);

                    String delimiter = ", ";
                    String phones[] = groupNumber.split(delimiter);
                    messageSender = new com.klinker.android.send_message.Message(body,
                            phones);
                    messagePresenter.sendMessage(messageSender, 0);
                }
                //add message group
                Message message = new Message();
                message.setDate(System.currentTimeMillis());
                message.setType(Constants.MESSAGE_TYPE_SENT);
                message.setBody(body);
                addNewMessageToRecyclerView(message);
            } else {
                messageSender = new com.klinker.android.send_message.Message(body,
                        mPhoneNumber);
                messagePresenter.sendMessage(messageSender, mThreadId);
            }
        }
    }

    private void hideMedia() {
        viewAbsForFragment.setVisibility(View.GONE);
        frameLayout.setVisibility(View.GONE);
//        Glide4Engine.getInstance().loadImage(btnAddMedia, getResources().getDrawable(R.drawable.vector_plus));
        btnAddMedia.setImageResource(R.drawable.vector_plus);
    }

    private void showMedia() {
        if (mediaFragment == null) {
            mediaFragment = new MediaFragment();
            mediaFragment.setListener(this);
        }
//        Glide4Engine.getInstance().loadImage(btnAddMedia, getResources().getDrawable(R.drawable.vector_down));
        btnAddMedia.setImageResource(R.drawable.vector_down);
        viewAbsForFragment.setVisibility(View.VISIBLE);
        viewAbsForFragment.post(() -> {
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) viewAbsForFragment.getLayoutParams();
            layoutParams.height = BaseActivity.sKeyBoardHeight;
            viewAbsForFragment.setLayoutParams(layoutParams);
        });
        frameLayout.setVisibility(View.VISIBLE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragmentMedia, mediaFragment);
        fragmentTransaction.commit();
    }

    private void initView() {
        viewAbsForFragment = findViewById(R.id.fragmentMediaPre);
        frameLayout = findViewById(R.id.fragmentMedia);
        btnAddMedia = findViewById(R.id.btnAddMedia);
        mTvBody = findViewById(R.id.tv_body);
        mImgSend = findViewById(R.id.img_send);
        recyclerView = findViewById(R.id.recycler_messageList);
        backgroundMain = findViewById(R.id.background_main);
        backgroundTransparent = findViewById(R.id.background_transparent);
        borderBody = findViewById(R.id.border_body);
        messageAdapter = new MessageAdapter(this, color);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setHasFixedSize(true);
        changeColor();
    }

    private void changeColor() {
        int backgroundCompose = SharedPreferenceHelper.getInstance(this).getInt(Constants.BACKGROUND_THEME_COMPOSE);
        if (backgroundCompose != 0) {
            backgroundMain.setBackgroundResource(backgroundCompose);
            backgroundTransparent.setVisibility(View.VISIBLE);
            mTvBody.setHintTextColor(getResources().getColor(R.color.textGrey));
            mTvBody.setTextColor(getResources().getColor(R.color.white));
            borderBody.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
        }
        btnAddMedia.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
        mImgSend.setImageTintList(ColorStateList.valueOf(color));
        findViewById(R.id.btnCamera).getBackground().setColorFilter(color, PorterDuff.Mode.SRC_OVER);
    }

    private void setUpRecyclerView() {
        recyclerView.post(() -> {
            if (type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
                MessageGroupDBHelper messageGroupDBHelper = new MessageGroupDBHelper(this);
                messageAdapter.setData(messageGroupDBHelper.getMessages((int) mThreadId));
            } else {
                messageAdapter.setData(mMessageSms);
            }
        });
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(MessageUtils.getContactbyPhoneNumber(this, mPhoneNumber));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if (type.equals(Constants.TYPE_NEW_MESSAGE)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.call:
                onCall();
                return true;
            case R.id.detail:
                Function.detailsContactIntent(String.valueOf(MessageUtils.getContactIDFromNumber(mPhoneNumber, this)));
                return true;
            case R.id.add_contact:
                Function.addContactIntent(mPhoneNumber);
                return true;
            case R.id.delete:
                new DeleteConversationDialog(this, mThreadId, () -> {
                    setResult(Constants.RESULT_CHANGE);
                    onBackPressed();
                }).show();

                return true;
            case R.id.group_detail:
                Intent intent = new Intent(this, GroupDetailsActivity.class);
                intent.putExtra("group_name", mPhoneNumber);
                intent.putExtra("group_number", groupNumber);
                intent.putExtra("type", type);
                if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE)) {
                    intent.putParcelableArrayListExtra("contact", (ArrayList<? extends Parcelable>) contacts);
                } else {
                    intent.putExtra("id_contact", idContactGroup);
                }
                startActivity(intent);
                return true;
            case R.id.help:
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:themecolor.technology@gmail.com"));
                startActivity(Intent.createChooser(emailIntent, "Send feedback"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onCall() {
        Intent intentCall = new Intent(Intent.ACTION_DIAL);
        intentCall.setData(Uri.parse("tel:" + mPhoneNumber));
        startActivity(intentCall);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose, menu);
        if (type.equalsIgnoreCase(Constants.TYPE_NEW_GROUP_MESSAGE) || type.equalsIgnoreCase(Constants.TYPE_GROUP_MESSAGE)) {
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(false);
        } else {
            if (Character.isLetter(mPhoneNumber.charAt(1))) {
                menu.getItem(0).setVisible(false);
                menu.getItem(1).setVisible(false);
            }
            if (MessageUtils.contactExists(this, mPhoneNumber)) {
                menu.getItem(1).setVisible(false);
            } else {
                menu.getItem(2).setVisible(false);
            }
            menu.getItem(3).setVisible(false);
        }
        return true;
    }

    @Override
    public void onImageAdded(String path) {
        MediaAddingModel mediaAddingModel = new MediaAddingModel();
        mediaAddingModel.setType(MediaAddingModel.Type.EMOJI);
        mediaAddingModel.setPath(path);
        sendMMS(mediaAddingModel);
    }

    @Override
    public void onImageGifAdded(String path) {
        MediaAddingModel mediaAddingModel = new MediaAddingModel();
        mediaAddingModel.setType(MediaAddingModel.Type.GIF);
        mediaAddingModel.setPath(path);
        sendMMS(mediaAddingModel);
    }

    @Override
    public void onAudioAdded(String audioFile) {
        long size = new File(audioFile).length();
        if (size <= 300 * 1024) {
            MediaAddingModel mediaAddingModel = new MediaAddingModel();
            mediaAddingModel.setType(MediaAddingModel.Type.AUDIO);
            mediaAddingModel.setPath(audioFile);
            sendMMS(mediaAddingModel);
        }
    }

    @Override
    public void onAttachFileAdded(String filePath) {
        MediaAddingModel.Type typeMedia = FileController.getFileType(FileController.getUri(filePath));
        long size = new File(filePath).length();
        if (size <= 300 * 1024) {
            if (typeMedia.equals(MediaAddingModel.Type.ATTACH_IMAGE) || typeMedia.equals(MediaAddingModel.Type.ATTACH_VIDEO)) {
                MediaAddingModel mediaAddingModel = new MediaAddingModel();
                mediaAddingModel.setType(typeMedia);
                mediaAddingModel.setPath(filePath);
                if (typeMedia.equals(MediaAddingModel.Type.ATTACH_IMAGE)) {
                    mediaAddingModel.setBitmap(Function.uriToBitmap(filePath));
                }
                sendMMS(mediaAddingModel);

            } else if (typeMedia == MediaAddingModel.Type.AUDIO) {
                onAudioAdded(filePath);
            } else if (typeMedia == MediaAddingModel.Type.GIF) {
                onImageGifAdded(filePath);
            } else {
                Toast.makeText(this, "This file format is not supported.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Can't send file, because the file is larger than 300KB. ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onContactAdded(Contact contact) {
        MediaAddingModel mediaAddingModel = new MediaAddingModel();
        mediaAddingModel.setType(MediaAddingModel.Type.CONTACT);
        mediaAddingModel.setContact(contact);
        sendMMS(mediaAddingModel);
    }

    @Override
    public void onAddressAdded(AddressData addressData) {
        String body;
        if (addressData.getAddressList() == null || addressData.getAddressList().size() == 0) {
            body = "https://www.google.com/maps/search/?api=1&query=" + addressData.getLatitude() + "," + addressData.getLongitude();
        } else {
            String val = addressData.getAddressList().get(0).getThoroughfare() + ", " + addressData.getAddressList().get(0).getSubAdminArea() + ", " + addressData.getAddressList().get(0).getAdminArea();
            body = Uri.parse("https://www.google.com/maps/search/?api=1")
                    .buildUpon()
                    .appendQueryParameter("query", val)
                    .build().toString();
        }
        sendSMS(body);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && requestCode == REQUEST_IMAGE_CAPTURE) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap bitmap = (Bitmap) extras.get("data");
                MediaAddingModel mediaAddingModel = new MediaAddingModel();
                mediaAddingModel.setType(MediaAddingModel.Type.ATTACH_IMAGE);
                mediaAddingModel.setBitmap(bitmap);
                sendMMS(mediaAddingModel);
            }
        }
    }

    private void initLoader() {
        LoaderManager.getInstance(this).initLoader(1, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Uri baseUri = Uri.parse("content://sms/");
        return new CursorLoader(getApplicationContext(), baseUri, null, "thread_id="
                + mThreadId, null, "date ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 1) {
            if ((mMessageSms == null || mMessageSms.size() == 0) && cursor != null && cursor.getColumnCount() > 0) {
                while (cursor.moveToNext()) {
                    Message message = new Message();
                    message.setThreadId(cursor.getInt(cursor.getColumnIndex("thread_id")));
                    message.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                    message.setAddress(cursor.getString(cursor.getColumnIndex("address")));
                    message.setBody(cursor.getString(cursor.getColumnIndex("body")));
                    message.setDate(cursor.getLong(cursor.getColumnIndex("date")));
                    message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    mMessageSms.add(message);
                }
                loadMMs();
            }
        } else {
            throw new IllegalArgumentException("no loader id handled!");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    private void registerUpdateMessage() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Constants.ACTION_UPDATE_MESSAGE)) {
                    ConversationComing conversationComing = intent.getParcelableExtra(Constants.PUT_OBJ);
                    if (conversationComing != null && conversationComing.getThread_id() == mThreadId) {
                        Message message = new Message(conversationComing.getThread_id(),
                                conversationComing.getAddress(),
                                conversationComing.getBody(),
                                conversationComing.getTime(),
                                Constants.MESSAGE_TYPE_INBOX);
                        if (messageAdapter != null) {
                            MessageUtils.markMessageRead(ComposeActivity.this, message.getAddress());
                            addNewMessageToRecyclerView(message);
                        }
                    }
                }
            }
        };

        broadcastReceiverUpdateNewMessage = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase("UPDATE_MESSAGE_JUST_SENT")) {
                    ContentValues contentValues = intent.getParcelableExtra("obj");
                    if (contentValues != null) {
                        Message message = new Message(contentValues);
                        if (message.getThreadId() == mThreadId && messageAdapter != null) {
                            addNewMessageToRecyclerView(message);
                        }
                    } else if (intent.getBooleanExtra("status", false)) {
                        long id = intent.getLongExtra("id", 0);
                        long time = intent.getLongExtra("date", System.currentTimeMillis());
                        int type = intent.getIntExtra("type", 4);
                        messageAdapter.updateMessageStatus(id, type, time);
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_UPDATE_MESSAGE);

        IntentFilter intentFilterUpdateMessage = new IntentFilter();
        intentFilterUpdateMessage.addAction("UPDATE_MESSAGE_JUST_SENT");

        registerReceiver(broadcastReceiver, intentFilter);
        registerReceiver(broadcastReceiverUpdateNewMessage, intentFilterUpdateMessage);
    }

    private void addNewMessageToRecyclerView(Message message) {
        setResult(Constants.RESULT_CHANGE);
        mMessageSms.add(message);
        messageAdapter.addNewMessage(message);
        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.setCurrentMessageId(0);
        if (messageAdapter != null) {
            messageAdapter.releaseMedia();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mThreadId != 0) {
            MyApplication.setCurrentMessageId(mThreadId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.setCurrentMessageId(0);
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        if (broadcastReceiverUpdateNewMessage != null) {
            unregisterReceiver(broadcastReceiverUpdateNewMessage);
        }
    }
}
