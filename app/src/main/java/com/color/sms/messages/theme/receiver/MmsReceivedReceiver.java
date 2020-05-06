package com.color.sms.messages.theme.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.ComposeActivity;
import com.color.sms.messages.theme.model.Message;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.MessageUtils;

public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {
    @Override
    public void onMessageReceived(Context context, Uri messageUri) {
        Message message = MessageUtils.getIdFromUriMms(context, messageUri);

        createNotification(context, MessageUtils.getContactByRecipientId(context, message.getThreadId()),
                "New message MMS", (int) message.getThreadId());
    }

    @Override
    public void onError(Context context, String error) {
        Log.d("CC", "onMessageReceived: " + error);
    }

    public void createNotification(Context context, String title, String message, int thread_id) {
        NotificationManager mNotificationManager;
        NotificationCompat.Builder mBuilder;
        String NOTIFICATION_CHANNEL_ID = "10001";

        Intent resultIntent = new Intent(context, ComposeActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("id", thread_id);
        resultIntent.putExtra("phone", title);
        resultIntent.putExtra("type", Constants.TYPE_CONVERSATION);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.ic_message_black_24dp);
        mBuilder.setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent);

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "SMS", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(0 /* Request Code */, mBuilder.build());
    }

}
