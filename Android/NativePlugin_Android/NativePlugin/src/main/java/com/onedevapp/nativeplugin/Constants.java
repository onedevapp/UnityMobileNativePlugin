package com.onedevapp.nativeplugin;

import android.util.Log;

import com.unity3d.player.UnityPlayer;

/**
 * Some Constant values
 */
public class Constants {

    public static final int REQUEST_LOCATION_CHECK_SETTINGS = 9874;
    public static final int REQUEST_CODE_PERMISSION = 9875;   //Code for request normal permissions.
    public static final int REQUEST_CODE_OPEN_SETTINGS = 9876;    //Code for request to open settings.
    public static final int REQUEST_TAKE_PHOTO = 9877;   //Code for request to open camera.
    public static final int REQUEST_GALLERY_PHOTO = 9878;    //Code for request to open gallery.

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

    public static final int EC_IMAGE_PICKER_PERMISSION_FAILED = 1;
    public static final int EC_IMAGE_PICKER_FILE_NOT_READABLE = 2;
    public static final int EC_IMAGE_PICKER_INTERNAL_ERROR = 4;
    public static final int EC_IMAGE_PICKER_FILE_CANT_CREATE = 5;


    public static final String UNITY_GAME_OBJECT = "MobileNativeManager";
    public static final String UNITY_IMAGE_PICKER_RESULT = "OnImagePickedResult";

    /**
     * To write library messages to logcat
     */
    public static boolean enableLog = false;

    /**
     * WriteLog to log library messages to logcat
     * Can toggle on/off with enableLog boolean at any time
     *
     * @param message Log Message
     */
    public static void WriteLog(String message) {
        if (enableLog) Log.d("NativePlugin", message);
    }


    /**
     * Send message to Unity's GameObject (named as UNITY_GAMEOBJECT)
     *
     * @param method  name of the method in GameObject's script
     * @param message the actual message
     */
    public static void sendMessageToUnityObject(String method, String message) {
        UnityPlayer.UnitySendMessage(Constants.UNITY_GAME_OBJECT, method, message);
    }
}
