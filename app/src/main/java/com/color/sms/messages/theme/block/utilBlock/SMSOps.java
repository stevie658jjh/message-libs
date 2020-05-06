package com.color.sms.messages.theme.block.utilBlock;

import android.annotation.SuppressLint;


public class SMSOps {

    private static SMSOps instance;

    private SMSOps() {
    }

    public static SMSOps getInstance() {
        if (instance == null) {
            instance = new SMSOps();
        }
        return instance;
    }

    @SuppressLint("HardwareIds")
    public void checkSimChange() {
    }
}
