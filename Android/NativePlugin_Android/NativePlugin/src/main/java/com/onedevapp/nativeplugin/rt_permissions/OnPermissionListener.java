package com.onedevapp.nativeplugin.rt_permissions;

/**
 * Callback methods when permissions are requested.
 */
public interface OnPermissionListener {

    /**
     * On any permission granted
     *
     * @param grantPermissions all permissions which are granted
     * @param all              true if all permissions are granted else false
     */
    void onPermissionGranted(String[] grantPermissions, boolean all);

    /**
     * On any permission denied
     *
     * @param deniedPermissions all permissions which are denied
     */
    void onPermissionDenied(String[] deniedPermissions);

    /**
     * On any error throws during permission call.
     *
     * @param errorMessage error message
     */
    void onPermissionError(String errorMessage);

}
