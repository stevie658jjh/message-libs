package com.color.sms.messages.theme.block.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.ToggleButton;

import com.color.sms.messages.theme.block.utilBlock.BlockListController;

public class SMSBlockerBroadcast extends BroadcastReceiver {
    String incomingNumber = "";
    String act_ser_sms = "";
    ToggleButton t, t1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];
            for (int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                incomingNumber = msgs[i].getOriginatingAddress();
            }
        }
        if (new BlockListController().isInBlacklist(incomingNumber)) // if incomingNumber need to be blocked
        {
            abortBroadcast();
        }
    }
}
