package com.color.sms.messages.theme.block.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;

import com.color.sms.messages.theme.block.utilBlock.BlockListController;
import com.color.sms.messages.theme.utils.MyApplication;

public class CallBlockerBroadcast extends BroadcastReceiver {
    private TelephonyManager m_telephonyManager;
    private ITelephony m_telephonyService;
    private AudioManager m_audioManager;
    Context context;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        m_telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class c = null;
            c = Class.forName(m_telephonyManager.getClass().getName());
            Method m = null;
            m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            m_telephonyService = (ITelephony) m.invoke(m_telephonyManager);
            m_audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            m_telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyPhoneStateListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            int permissionCheck = ContextCompat.checkSelfPermission(MyApplication.getInstance(), Manifest.permission.READ_PHONE_STATE);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    boolean whichService = new BlockListController().isInBlacklist(incomingNumber.substring(incomingNumber.length() - 10));
                    if (whichService) // if incoming Number need to be blocked
                    {
                        m_audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        m_telephonyService.endCall();
                    }
                }
            }
        }
    }
}
