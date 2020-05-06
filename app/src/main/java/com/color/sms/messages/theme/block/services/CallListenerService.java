package com.color.sms.messages.theme.block.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;


public class CallListenerService extends Service {

    TelephonyManager m_telephonyManager;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        m_telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        return super.onStartCommand(intent, flags, startId);
    }
}
