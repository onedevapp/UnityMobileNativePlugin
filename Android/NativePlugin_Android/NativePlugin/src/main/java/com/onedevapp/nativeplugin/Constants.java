package com.onedevapp.nativeplugin;

import android.util.Log;

public class Constants {

    public static final int REQUEST_LOCATION_CHECK_SETTINGS = 9874;
    public static final int REQUEST_CODE_PERMISSION = 9875;   //Code for request normal permissions.
    public static final int REQUEST_CODE_OPEN_SETTINGS = 9876;    //Code for request to open settings.

    public static final int PLAY_STORE_UPDATE = 0;
    public static final int THIRD_PARTY_UPDATE = 1;
    public static final String PERMISSION_PREFERENCES = "Permission_Prefs";
    public static final String EXTRA_PERMISSIONS = "permissions_extra";
    public static final String EXTRA_RATIONALE_MESSAGE = "rationale_message_extra";
    public static final String EXTRA_RATIONALE_POSITIVE = "rationale_positive_extra";
    public static final String EXTRA_RATIONALE_NEGATIVE = "rationale_negative_extra";
    public static final String EXTRA_SETTINGS_MESSAGE = "settings_message_extra";
    public static final String EXTRA_SETTINGS_POSITIVE = "settings_positive_extra";
    public static final String EXTRA_SETTINGS_NEGATIVE = "settings_negative_extra";

    public static boolean enableLog = false;

    public static void WriteLog(String message) {
        if (enableLog) Log.d("NativePlugin", message);
    }

}
