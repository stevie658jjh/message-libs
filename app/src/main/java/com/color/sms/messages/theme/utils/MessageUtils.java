package com.color.sms.messages.theme.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.color.sms.messages.theme.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;

public class MessageUtils {

    public static String getContactByRecipientId(Context context, long recipientId) {
        String contact = "";
        Uri uri = ContentUris.withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId);
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                contact = getContactbyPhoneNumber(context, cursor.getString(0));
            }
        } finally {
            cursor.close();
        }

        return contact;
    }

    public static String getContactbyPhoneNumber(Context context, String phoneNumber) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.NORMALIZED_NUMBER};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        String name = null;
        String nPhoneNumber = phoneNumber;

        try {

            if (cursor.moveToFirst()) {
                nPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.NORMALIZED_NUMBER));
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            }

        } finally {
            cursor.close();
        }

        if (name != null) { // if there is a display name, then return that
            return name;
        } else {
            return nPhoneNumber; // if there is not a display name, then return just phone number
        }
    }

    public static boolean contactExists(Context context, String number) {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri, mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public static int getContactIDFromNumber(String contactNumber, Context context) {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = 0;
        Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, contactNumber),
                new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
        while (contactLookupCursor.moveToNext()) {
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
        }
        contactLookupCursor.close();

        return phoneContactID;
    }

    public static String getContact(Context context, String _id) {
        ContentResolver cr = context.getContentResolver();
        Cursor pCur = cr.query(
                Uri.parse("content://mms-sms/canonical-addresses"), new String[]{"address"},
                "_id" + " = ?",
                new String[]{_id}, null);

        String contactAddress = null;

        if (pCur != null) {
            if (pCur.getCount() != 0) {
                pCur.moveToNext();
                contactAddress = pCur.getString(pCur.getColumnIndex("address"));
            }
            pCur.close();
        }
        return contactAddress;
    }

    public static String getContactNumber(Context context, final long recipientId) {
        String number = null;
        Cursor c = context.getContentResolver().query(ContentUris
                        .withAppendedId(Uri.parse("content://mms-sms/canonical-address"), recipientId),
                null, null, null, null);
        if (c != null && c.moveToFirst()) {
            number = c.getString(0);
            c.close();
        }
        return number;
    }

    public static void deleteConversation(long conversationId) {
        try {
            new Thread() {
                @Override
                public void run() {
                    Uri uri = Uri.parse("content://sms");
                    String[] projection = {"*"};
                    String selection = "thread_id = ?";
                    String[] selectionArgs = {"" + conversationId};
                    Cursor cr = MyApplication.getInstance().getContentResolver().query(uri, projection, selection, selectionArgs, "date DESC");
                    if (cr != null) {
                        ArrayList<Long> list_id_delete = new ArrayList<>();
                        while (cr.moveToNext()) {
                            list_id_delete.add(cr.getLong(cr.getColumnIndexOrThrow("_id")));
                        }
                        cr.close();
                        for (Long id : list_id_delete) {
                            MyApplication.getInstance().getContentResolver().delete(Uri.parse("content://sms/" + id), selection, selectionArgs);
                        }
                    }
                }
            }.start();

            new Thread() {
                @Override
                public void run() {
                    Uri uri = Uri.parse("content://mms");
                    String[] projection = {"*"};
                    String selection = "thread_id = ?";
                    String[] selectionArgs = {"" + conversationId};
                    Cursor cr = MyApplication.getInstance().getContentResolver().query(uri, projection, selection, selectionArgs, "date DESC");
                    if (cr != null) {
                        ArrayList<Long> list_id_delete = new ArrayList<>();
                        while (cr.moveToNext()) {
                            list_id_delete.add(cr.getLong(cr.getColumnIndexOrThrow("_id")));
                        }
                        cr.close();
                        for (Long id : list_id_delete) {
                            MyApplication.getInstance().getContentResolver().delete(Uri.parse("content://mms/" + id), selection, selectionArgs);
                        }
                    }
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long saveSms(Context context, String phoneContact, Message sms, long thread_id) {
        try {
            ContentValues values = new ContentValues();
            values.put("address", sms.getUser());
            values.put("body", sms.getBody());
            values.put("read", false);
            values.put("type", (!PhoneNumberUtils.compare(phoneContact, sms.getUser())) ? 2 : 1);
            values.put("status", 32);
            if (thread_id != -1)
                values.put("thread_id", thread_id);
            values.put("date", sms.getDate());
            Uri uri_id = context.getContentResolver().insert(Uri.parse("content://sms"), values);
            if (uri_id != null)
                return Long.parseLong(uri_id.toString().substring(14));
        } catch (Exception ex) {
            Toast.makeText(context, "store_sms\n" + ex.toString(), Toast.LENGTH_LONG).show();
        }
        return -1;
    }

    public static long saveMessageGroup(Context context, String address, String body, long thread_id) {
        try {
            ContentValues values = new ContentValues();
            values.put("address", address);
            values.put("body", body);
            values.put("read", false);
            values.put("type", Constants.MESSAGE_TYPE_OUTBOX);
            values.put("status", 32);
            if (thread_id != -1)
                values.put("thread_id", thread_id);
            values.put("date", System.currentTimeMillis());
            Uri uri_id = context.getContentResolver().insert(Uri.parse("content://sms"), values);
            if (uri_id != null)
                return Long.parseLong(uri_id.toString().substring(14));
        } catch (Exception ex) {
            Toast.makeText(context, "store_sms\n" + ex.toString(), Toast.LENGTH_LONG).show();
        }
        return -1;
    }

    public static void saveSmsReceiver(Context context, String address, String body, long time, long thread_id) {
        try {
            ContentValues values = new ContentValues();
            values.put("address", address);
            values.put("body", body);
            values.put("date", time);
            if (thread_id != -1)
                values.put("thread_id", thread_id);
            context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
        } catch (Exception ex) {
            Toast.makeText(context, "" + ex.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public static void markMessageRead(Context context, String number) {
        Uri uri = Uri.parse("content://sms/inbox");
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null)
                while (cursor.moveToNext()) {
                    if ((cursor.getString(cursor.getColumnIndex("address")).equals(number)) && (cursor.getInt(cursor.getColumnIndex("read")) == 0)) {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                        return;
                    }
                }
        } catch (Exception e) {
            Log.e("Mark Read", "Error in Read: " + e.toString());
        }
    }

    public static Long getOrCreateThreadId(Context context, String phone) {
        try {
            Uri threadIdUri = Uri.parse("content://mms-sms/threadID");
            Uri.Builder builder = threadIdUri.buildUpon();
            String[] recipients = {phone};
            for (String recipient : recipients) {
                builder.appendQueryParameter("recipient", recipient);
            }
            Uri uri = builder.build();
            long threadId = 0L;
            Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        threadId = cursor.getLong(0);
                    }
                } finally {
                    cursor.close();
                }
                return threadId;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }

    public static class UserPref {
        int max_conversation = 10;
        int max_sms = 21;
        boolean hide_keyboard = true;
        boolean first_upper = true;
        boolean vibrate = true;
        boolean vibrate_delivered = true;
        float text_size = 13.0F;
        boolean old_message = false;
        int old_message_num = 500;
        boolean drawer = false;
        int conversation_effect = 14;
        int listConversation_effect = 14;

        UserPref() {
        }

        void setUserPref(SharedPreferences pref) {
            String pref_nb_conv = pref.getString("conversation_onload_key", "10");
            String pref_nb_sms = pref.getString("sms_onload_key", "42");
            String pref_text_size = pref.getString("text_size_key", "13");
            this.conversation_effect = Integer.parseInt(pref.getString("conversation_jazzyeffect_key", "14"));
            this.listConversation_effect = Integer.parseInt(pref.getString("list_conversations_jazzyeffect_key", "14"));
            this.old_message = pref.getBoolean("old_message_key", false);
            this.old_message_num = Integer.parseInt(pref.getString("old_message_num_key", "500"));
            try {
                this.max_conversation = Integer.parseInt(pref_nb_conv);
            } catch (Exception ex) {
                this.max_conversation = 10000;
            }
            try {
                this.max_sms = Integer.parseInt(pref_nb_sms);
            } catch (Exception ex) {
                this.max_sms = 100000;
            }
            try {
                this.text_size = Float.parseFloat(pref_text_size);
            } catch (Exception ex) {
                this.text_size = 13.0F;
            }
            this.hide_keyboard = pref.getBoolean("hide_keyboard_key", true);
            this.first_upper = pref.getBoolean("first_uppercase_key", true);
            this.vibrate = pref.getBoolean("vibration_key", true);
            this.vibrate_delivered = pref.getBoolean("delivered_vibration_key", true);
            this.drawer = pref.getBoolean("drawer_start_opened_key", false);
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getUserPhone(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getLine1Number();
    }

    public static abstract class MmsTask extends AsyncTask<Void, Message, Boolean> {

        private final String mmsUri = "content://mms";
        private final String[] projection = {"*"};
        private long lastSms = -1;
        private String selection = "thread_id = ?";
        private ArrayList<String> selectionArgs = new ArrayList<String>();

        private final int conversationId;
        private UserPref userPref;
        @SuppressLint("StaticFieldLeak")
        private Context context;
        protected WeakReference<FragmentActivity> activity;

        protected MmsTask(FragmentActivity activity, int conversationId) {
            link(activity);
            this.conversationId = conversationId;
            selectionArgs.add("" + conversationId);
        }

        @Override
        protected void onPreExecute() {
            if (activity.get() != null) {
                context = activity.get().getApplicationContext();
                userPref = new UserPref();
                userPref.setUserPref(PreferenceManager.getDefaultSharedPreferences(context));
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (activity.get() != null) {
                if (!result) {
                }
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (activity.get() != null) {
                Cursor allMms = context.getContentResolver().query(
                        Uri.parse(mmsUri),
                        projection,
                        selection,
                        selectionArgs.toArray(new String[selectionArgs.size()]),
                        "date ASC");

                if (allMms != null) {
                    int count = 0;
                    while (count < userPref.max_sms && allMms.moveToNext()) {
                        long mmsId = allMms.getLong(allMms.getColumnIndex("_id"));
                        int type = allMms.getInt(allMms.getColumnIndex("msg_box"));
                        String user = getAddressNumber(mmsId);
                        if (user == null) {
                            user = getUserPhone(context);
                        }
                        Message mms = getMMSData(mmsId, user);
                        long date = allMms.getLong(allMms.getColumnIndex("date"));
                        mms.setDate(date * 1000);
                        mms.setType(type);
                        publishProgress(mms);
                        count += 1;
                    }
                    allMms.close();
                    if (count == 0 && lastSms == -1) {
                        publishProgress(null);
                    }
                }
                return true;
            }
            return false;
        }

        public static String getAddressNumber(long id) {
            String selectionAdd = "msg_id=" + id;
            String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
            Uri uriAddress = Uri.parse(uriStr);
            Cursor cAdd = MyApplication.getInstance().getContentResolver().query(uriAddress, null,
                    selectionAdd, null, null);
            String name = null;
            if (cAdd != null) {
                if (cAdd.moveToFirst()) {
                    do {
                        String number = cAdd.getString(cAdd.getColumnIndex("address"));
                        if (number != null) {
                            try {
                                Long.parseLong(number.replace("-", ""));
                                name = number;
                            } catch (NumberFormatException nfe) {
                                if (name == null) {
                                    name = number;
                                }
                            }
                        }
                    } while (cAdd.moveToNext());
                }
                cAdd.close();
            }
            return name;
        }

        private Message getMMSData(long mmsId, String user) {
            Message mms = null;
            String selectionPart = "mid=" + mmsId;
            Uri uri = Uri.parse("content://mms/part");
            try {
                mms = new Message();
                Cursor cPart = context.getContentResolver().query(uri, new String[]{"*"}, selectionPart, null, null);
                if (cPart != null) {
                    if (cPart.moveToFirst()) {
                        do {
                            mms.setId(mmsId);
                            mms.setUser(user);
                            String partId = cPart.getString(cPart.getColumnIndex("_id"));
                            String type = cPart.getString(cPart.getColumnIndex("ct"));

                            if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                    "image/gif".equals(type) || "image/jpg".equals(type) ||
                                    "image/png".equals(type)) {
                                mms.setImage(getMmsImageUri(partId));
                                mms.setGif("image/gif".equals(type));
                            }
                            if ("text/plain".equals(type)) {
                                String data = cPart.getString(cPart.getColumnIndex("_data"));
                                String body;
                                if (data != null) {
                                    body = getMmsText(partId);
                                } else {
                                    body = cPart.getString(cPart.getColumnIndex("text"));
                                }
                                mms.setBody(body);
                            }
                            if ("audio/wav".equals(type)) {
                                mms.setAudio(getMmsImageUri(partId));
                            }
                            if ("video/3gpp".equals(type)) {
                                mms.setVideo(getMmsImageUri(partId));
                            }
                        } while (cPart.moveToNext());
                        cPart.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return mms;
        }

        private String getMmsText(String partId) {
            Uri partURI = Uri.parse("content://mms/part/" + partId);
            InputStream is = null;
            StringBuilder sb = new StringBuilder();
            try {
                is = context.getContentResolver().openInputStream(partURI);
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return sb.toString();
        }

        private Uri getMmsImageUri(String partId) {
            return Uri.parse("content://mms/part/" + partId);
        }

        @Override
        abstract protected void onProgressUpdate(Message... prog);

        public void link(FragmentActivity pActivity) {
            activity = new WeakReference<>(pActivity);
        }

        public void execTask() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                execute();
            }
        }
    }

    public static Message getIdFromUriMms(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        Message message = null;
        if (cursor != null) {
            try {
                message = new Message();
                if (cursor.moveToFirst()) {
                    for (String name : cursor.getColumnNames()) {
                        long date = cursor.getLong(cursor.getColumnIndex("date")) * 1000;
                        long size = cursor.getLong(cursor.getColumnIndex("m_size"));
                        long threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));

                        message.setDate(date);
                        message.setThreadId(threadId);
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return message;
    }
}
