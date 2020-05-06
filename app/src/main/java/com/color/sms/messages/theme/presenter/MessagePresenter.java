package com.color.sms.messages.theme.presenter;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.color.sms.messages.theme.model.MediaAddingModel;
import com.color.sms.messages.theme.utils.FileController;

public class MessagePresenter {
    private MMSSettings settings;
    private Transaction transaction;
    private Context context;

    public MessagePresenter(final Context context) {
        this.context = context;
        settings = MMSSettings.get(context);
        if (TextUtils.isEmpty(settings.getMmsc())) {
            ApnUtils.initDefaultApns(context, () -> settings = MMSSettings.get(context, true));
        }
        settingTransaction(context);
    }

    private void settingTransaction(Context context) {
        com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
        sendSettings.setMmsc(settings.getMmsc());
        sendSettings.setProxy(settings.getMmsProxy());
        sendSettings.setPort(settings.getMmsPort());
        sendSettings.setUseSystemSending(true);
        transaction = new Transaction(context, sendSettings);
    }

    public void sendMessage(final String string, final String phoneNumber, long threadId) {
        Message message = new Message(string, phoneNumber);
        sendMessage(message, threadId);
    }

    private byte[] getImageGif(String url) {
        try {
            URL imageUrl = new URL(url);
            URLConnection ucon = imageUrl.openConnection();

            InputStream is = ucon.getInputStream();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int read = 0;

            while ((read = is.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }

            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            Log.d("ImageManager", "Error: " + e.toString());
        }
        return null;
    }

    @SuppressLint("StaticFieldLeak")
    public void sendMessage(@NonNull final Message message, final long threadID, MediaAddingModel mediaAddingModel) {
        switch (mediaAddingModel.getType()) {
            case GIF:
                new AsyncTask<Void, Void, byte[]>() {
                    @Override
                    protected byte[] doInBackground(Void... voids) {
                        return getImageGif(mediaAddingModel.getPath());
                    }

                    @Override
                    protected void onPostExecute(byte[] bytes) {
                        message.addMedia(bytes, "image/gif");
                        sendMessage(message, threadID);
                    }
                }.execute();
                return;
            case CONTACT:
                // send sms
                break;
            case AUDIO:
                message.addAudio(FileController.getAudioByte(mediaAddingModel.getPath()));
                break;
            case EMOJI:
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected Bitmap doInBackground(Void... voids) {
                        try {
                            URL url = new URL(mediaAddingModel.getPath());
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            return BitmapFactory.decodeStream(input);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (bitmap != null) {
                            message.addImage(bitmap, mediaAddingModel.getMinType());
                            sendMessage(message, threadID);
                        }
                    }
                }.execute();
                return;
            case ATTACH_IMAGE:
                message.addImage(mediaAddingModel.getBitmap(), mediaAddingModel.getMinType());
                break;
            case ATTACH_VIDEO:
                message.addVideo(FileController.convertVideoToBytes(Uri.parse(mediaAddingModel.getPath())));
                break;
            case ATTACH_FILE:
                message.addMedia(FileController.getAudioByte(mediaAddingModel.getPath()), FileController.getMimeType(FileController.getUri(mediaAddingModel.getPath())));
                break;
        }
        sendMessage(message, threadID);
    }

    public void sendMessage(@NonNull final Message message, final long threadID) {
        new Thread(() -> {
            if (threadID == 0) {
                transaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            } else {
                transaction.sendNewMessage(message, threadID);
            }
        }).start();
    }

    public String[] getContactByNumber(final String number) {
        String[] data = new String[2];
        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            Cursor cur = context.getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                    null, null, null);
            if (cur.moveToFirst()) {
                int nameIdx = cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                data[0] = cur.getString(nameIdx);
                String contactId = cur.getString(cur
                        .getColumnIndex(ContactsContract.PhoneLookup._ID));
                String photoUri = getContactPhotoUri(Long.parseLong(contactId));
                if (photoUri != null)
                    data[1] = photoUri;
                else
                    data[1] = null;
                cur.close();
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getContactPhotoUri(long contactId) {
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                contactId);
        String imagePath = null;
        if (photoUri != null) {
            imagePath = photoUri.toString();
        }
        return imagePath;
    }

    public interface Listener {
        void onLoaded();
    }

}
