package com.onedevapp.nativeplugin.rt_permissions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.onedevapp.nativeplugin.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;
import static com.onedevapp.nativeplugin.Constants.PERMISSION_PREFERENCES;

public final class PermissionUtils {

    /**
     * Empty Constructor
     */
    private PermissionUtils() {
    }

    /**
     * Check device build version
     *
     * @return true if build version is over Marshmallow else false
     */
    public static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * This method returns all the permissions in the manifest.
     *
     * @param context      This is context of the current activity
     * @return  List<String>     Returns List of permissions from the manifest.
     */
    static List<String> getManifestPermissions(Context context) {
        try {
            return Arrays.asList(context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    /**
     * This method checks whether the given permission is already granted or not.
     *
     * @param context      This is context of the current activity
     * @param permission    This is the permission we need to check
     * @return  boolean     Returns True if already permission granted for this permission else false.
     */
    public static boolean checkPermission(Context context,  String permission) {
        if (!isOverMarshmallow()) {
            return false;
        }
        //Determine whether you have been granted a particular permission.
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * This method checks whether the given permission can show rationale dialog.
     *
     * @param activity      This is context of the current activity
     * @param permission    This is the permission we need to check
     * @return  boolean     Returns True if permission is requested but not granted and can show rationale dialog else false.
     */
    public static boolean checkPermissionRationale(Activity activity, String permission) {
        if (!isOverMarshmallow()) {
            return false;
        }
        //If any permission is requested but not granted, we'll show rationale dialog
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Checks all permissions whether registered in the manifest
     *
     * @param context context of the current activity
     * @param requestPermissions set of permissions requested
     * @return true if all requested permissions are registered in the manifest
     */
    static boolean checkPermissionsInManifest(Context context, Set<String> requestPermissions) {
        List<String> manifestPermissions = getManifestPermissions(context);
        if (manifestPermissions != null && !manifestPermissions.isEmpty()) {
            for (String permission : requestPermissions) {
                if (!manifestPermissions.contains(permission)) {
                    Constants.WriteLog( permission + ": Permissions are not registered in the manifest file");
                    return false;
                }
            }
        } else {
            Constants.WriteLog( "No permissions are registered in the manifest file");
            return false;
        }
        return true;
    }

    /**
     * Gets list of all permissions got denied from the requested permissions
     *
     * @param context context of the current activity
     * @param permissions set of permissions requested
     * @return list of permission denied
     */
    static ArrayList<String> getFailPermissions(Context context, Set<String> permissions) {
        if (!isOverMarshmallow()) {
            return null;
        }

        ArrayList<String> failPermissions = null;

        for (String permission : permissions) {

            if (!checkPermission(context, permission)) {
                if (failPermissions == null) {
                    failPermissions = new ArrayList<>();
                }
                failPermissions.add(permission);
            }
        }

        return failPermissions;
    }

    /**
     * This method is used to display a Dialog Message to the user before prompting permission dialog.
     *
     * @param context   context of the current activity
     * @param message   the message we are displaying in the dialog to the user.
     * @param positiveLabel   positive text we are displaying in the dialog to the user.
     * @param negativeLabel   negative text we are displaying in the dialog to the user.
     * @param positiveListener    dialog OnClickListener for positive button to request for permission.
     * @param negativeListener    dialog OnClickListener for negative button to request for permission.
     */
    static void showDialogMessage(Context context, String message,String positiveLabel, DialogInterface.OnClickListener positiveListener,String negativeLabel, DialogInterface.OnClickListener negativeListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveLabel, positiveListener)
                .setNegativeButton(negativeLabel, negativeListener)
                .create()
                .show();
    }

    /**
     * Saves the requested permission as first time requested to false
     *
     * @param context context of the current activity
     * @param permission requested permission
     */
    static void firstTimeAskingPermission(Context context, String permission){
        SharedPreferences sharedPreference = context.getSharedPreferences(PERMISSION_PREFERENCES, MODE_PRIVATE);
        sharedPreference.edit().putBoolean(permission, false).apply();
    }

    /**
     * Get permission status as been requested for first time or not
     *
     * @param context context of the current activity
     * @param permission requested permission
     * @return true if not requested for first time else false
     */
    static boolean isFirstTimeAskingPermission(Context context, String permission){
        return context.getSharedPreferences(PERMISSION_PREFERENCES, MODE_PRIVATE).getBoolean(permission, true);
    }

}