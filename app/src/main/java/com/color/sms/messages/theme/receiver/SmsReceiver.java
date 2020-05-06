/*
 * Copyright 2014 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.color.sms.messages.theme.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.telephony.SmsMessage;

import androidx.core.app.NotificationCompat;

import com.color.sms.messages.theme.R;
import com.color.sms.messages.theme.activity.ComposeActivity;
import com.color.sms.messages.theme.block.utilBlock.BlockListController;
import com.color.sms.messages.theme.model.ConversationComing;
import com.color.sms.messages.theme.utils.Constants;
import com.color.sms.messages.theme.utils.MessageUtils;
import com.color.sms.messages.theme.utils.MyApplication;

public class SmsReceiver extends BroadcastReceiver {

    private NotificationManager mNotificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
//            if (null != onDataUpdateListener) {
//                onDataUpdateListener.onUpdateData(true);
//            }
        Object[] smsExtra = (Object[]) intent.getExtras().get("pdus");
        String body = "";
        String address = "";
        long time = 0;

        assert smsExtra != null;
        for (Object o : smsExtra) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) o);
            body += sms.getMessageBody();
            address = sms.getOriginatingAddress();
            time = sms.getTimestampMillis();
        }

        if (address != null && address.contains("+84")) {
            address = address.replace("+84", "0");
        }
        //insert sms
        BlockListController blockListController = new BlockListController();
        if (!blockListController.isInBlacklist(address)) {
            long thread_id = MessageUtils.getOrCreateThreadId(context, address);
            MessageUtils.saveSmsReceiver(context, address, body, time, thread_id);

            if (MyApplication.getCurrentMessageId() != thread_id) {
                createNotification(context, address, body, (int) thread_id);
            }

            ConversationComing conversationComing = new ConversationComing(address, body, thread_id, time);
            updateMessage(context, conversationComing);
        }
    }

    private void updateMessage(Context context, ConversationComing conversationComing) {
        Intent intent = new Intent(Constants.ACTION_UPDATE_MESSAGE);
        intent.putExtra(Constants.PUT_OBJ, conversationComing);
        context.sendBroadcast(intent);
    }

    public void createNotification(Context context, String title, String message, int thread_id) {
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
