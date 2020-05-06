package com.color.sms.messages.theme.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MMSSettings {

    private static MMSSettings settings;

    private static final String MMSC_PREF = "mmsc_url";
    private static final String MMS_PROXY_PREF = "mms_proxy";
    private static final String MMS_PORT_PREF = "mms_port";

    private String mmsc;
    private String mmsProxy;
    private String mmsPort;

    public static MMSSettings get(Context context) {
        return get(context, false);
    }

    public static MMSSettings get(Context context, boolean forceReload) {
        if (settings == null || forceReload) {
            settings = init(context);
        }

        return settings;
    }

    private MMSSettings() {
    }

    private static MMSSettings init(Context context) {
        MMSSettings settings = new MMSSettings();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        settings.mmsc = sharedPreferences.getString(MMSC_PREF, "");
        settings.mmsProxy = sharedPreferences.getString(MMS_PROXY_PREF, "");
        settings.mmsPort = sharedPreferences.getString(MMS_PORT_PREF, "");

        return settings;
    }

    public String getMmsc() {
        return mmsc;
    }

    public String getMmsProxy() {
        return mmsProxy;
    }

    public String getMmsPort() {
        return mmsPort;
    }
}