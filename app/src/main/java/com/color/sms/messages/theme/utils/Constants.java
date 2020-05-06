package com.color.sms.messages.theme.utils;

import android.net.Uri;
import android.provider.Telephony.Threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.color.sms.messages.theme.model.Contact;

public final class Constants {
    public static final int MESSAGE_TYPE_ALL = 0;
    public static final int MESSAGE_TYPE_INBOX = 1;
    public static final int MESSAGE_TYPE_SENT = 2;
    public static final int MESSAGE_TYPE_DRAFT = 3;
    public static final int MESSAGE_TYPE_OUTBOX = 4;
    public static final int MESSAGE_TYPE_FAILED = 5; // for failed outgoing messages
    public static final int MESSAGE_TYPE_QUEUED = 6; // for messages to send later

    public static final int RESULT_CHANGE = 121;

    public static final int REQUEST_NONE = 111;

    public static final String ACTION_UPDATE_MESSAGE = "ACTION_UPDATE_MESSAGE";
    public static final String PUT_OBJ = "put_obj";
    public static final String PUT_BOOLEAN = "put_boolean";
    public static final String _ID = "_id";
    public static final String KEY_THREAD_ID = "thread_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_MSG = "msg";
    public static final String KEY_TYPE = "type";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_TIME = "time";

    public static final String ACTION_DATA_UPDATE_READY = "ACTION_DATA_UPDATE_READY";

    public static final String TYPE_CONVERSATION = "TYPE_CONVERSATION";
    public static final String TYPE_NEW_MESSAGE = "TYPE_NEW_MESSAGE";
    public static final String TYPE_NEW_GROUP_MESSAGE = "TYPE_NEW_GROUP_MESSAGE";
    public static final String TYPE_GROUP_MESSAGE = "TYPE_GROUP_MESSAGE";

    public static final Uri CONVERSATIONS_CONTENT_PROVIDER = Uri.parse("content://mms-sms/conversations?simple=true");
    public static final String[] ALL_THREADS_PROJECTION = {
            Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
            Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
            Threads.HAS_ATTACHMENT
    };

    public static List<Contact> contactList = new ArrayList<>();

    public static void sortList() {
        Collections.sort(contactList, (o1, o2) -> o1.getNameA().compareTo(o2.getNameA()));
    }

    public static final String BACKGROUND_THEME_MAIN = "BACKGROUND_THEME_MAIN";
    public static final String BACKGROUND_THEME_COMPOSE = "BACKGROUND_THEME_COMPOSE";
}
