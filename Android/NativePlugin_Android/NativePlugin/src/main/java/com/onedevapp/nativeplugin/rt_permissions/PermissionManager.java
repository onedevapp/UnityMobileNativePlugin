package com.onedevapp.nativeplugin.rt_permissions;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.onedevapp.nativeplugin.Constants;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PermissionManager {

    // region Declarations
    private static PermissionManager instance;

    public final String FRAGMENT_TAG = "InvisibleFragment"; //TAG of InvisibleFragment to find and create.

    private OnPermissionListener mOnPermissionListener; //Callback listener
    private Set<String> mPermissionsSet;  //
    private WeakReference<Activity> mActivityWeakReference; //Activity references

    //Rational dialog
    private String mRationalMessage = "This app needs permission to work without any problems.";
    private String mRationalDialogPositiveText = "Yes, Grant Permission";
    private String mRationalDialogNegativeText = "No, Deny It";

    //Settings dialog
    private String mSettingDialogMessage = "You have denied some permissions. Allow all permissions at [Settings] > [Permissions]";
    private String mSettingDialogPositiveText = "Go to Settings";
    private String mSettingDialogNegativeText = "No, Deny It";
    //endregion

    //region Constructor


    /**
     * Creates a builder that uses the default requestCode.
     *
     * @param activity the current activity
     * @return a new {@link PermissionManager} instance
     */
    public static PermissionManager Builder(Activity activity) {
        if (instance == null) {
            instance = new PermissionManager(activity);
        }
        return instance;
    }

    /**
     * Creates a builder
     *
     * @param fragment    the current fragment
     * @return a new {@link PermissionManager} instance
     */
    public static PermissionManager Builder(Fragment fragment) {
        if (instance == null) {
            instance = new PermissionManager(fragment);
        }
        return instance;
    }

    //Private constructor with activity
    private PermissionManager(Activity activity) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        mPermissionsSet = new HashSet<>();
    }

    //Private constructor with fragment
    private PermissionManager(Fragment fragment) {
        this.mActivityWeakReference = new WeakReference<>(fragment.getActivity());
        mPermissionsSet = new HashSet<>();
    }
    //endregion

    // region Setters

    /**
     * Add permissions that you want to request.
     * @param permission permission to be requested.
     * @return PermissionManager itself.
     */
    public PermissionManager addPermission(String permission){
        mPermissionsSet.add(permission);
        return this;
    }

    /**
     * All permissions that you want to request.
     * @param permissions permissions to be requested.
     * @return PermissionManager itself.
     */
    public PermissionManager addPermissions(String[] permissions){
        mPermissionsSet.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * Rational message will be shown when rational dialog is shown
     * @param rationalMessage rational message about why this permission is required.
     * @return PermissionManager itself.
     */
    public PermissionManager setRationalMessage(String rationalMessage){
        if(!rationalMessage.isEmpty()) this.mRationalMessage = rationalMessage;
        return this;
    }

    /**
     * Rational message will be shown when rational dialog is shown
     * @param dialogMessage rational message about why this permission is required.
     * @param dialogPositiveText rational positive button text.
     * @param dialogNegativeText rational Negative button text.
     * @return PermissionManager itself.
     */
    public PermissionManager setRationalDialog(String dialogMessage, String dialogPositiveText, String dialogNegativeText){
        if(!dialogMessage.isEmpty()) this.mRationalMessage = dialogMessage;
        if(!dialogPositiveText.isEmpty()) this.mRationalDialogPositiveText = dialogPositiveText;
        if(!dialogNegativeText.isEmpty()) this.mRationalDialogNegativeText = dialogNegativeText;
        return this;
    }

    /**
     * Settings message will be shown when "Never ask permission is enabled"
     * @param dialogMessage settings dialog message.
     * @param dialogPositiveText settings positive button text.
     * @param dialogNegativeText settings Negative button text.
     * @return PermissionManager itself.
     */
    public PermissionManager setSettingsDialog(String dialogMessage, String dialogPositiveText, String dialogNegativeText){
        if(!dialogMessage.isEmpty()) this.mSettingDialogMessage = dialogMessage;
        if(!dialogPositiveText.isEmpty()) this.mSettingDialogPositiveText= dialogPositiveText;
        if(!dialogNegativeText.isEmpty()) this.mSettingDialogNegativeText = dialogNegativeText;
        return this;
    }


    /**
     * Set the callback handler
     *
     * @param onPermissionListener the handler
     * @return the update manager instance
     */
    public PermissionManager handler(OnPermissionListener onPermissionListener) {
        this.mOnPermissionListener = onPermissionListener;
        return this;
    }
    //endregion

    // region helper functions

    /**
     * Returns the current activity
     */
    protected Activity getActivity() {
        return mActivityWeakReference.get();
    }

    //endregion

    /**
     * Request all manifest permissions at once in the fragment.
     */
    public void requestAllManifestPermissions(){

        mPermissionsSet.addAll(Objects.requireNonNull(PermissionUtils.getManifestPermissions(getActivity().getApplicationContext())));
        requestPermission();
    }

    /**
     * Request permissions at once in the fragment.
     */
    public void requestPermission(){

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mOnPermissionListener == null) {
                    Constants.WriteLog( "The permission listener callback interface must be implemented");
                    return;
                }

                if (!PermissionUtils.isOverMarshmallow()){
                    mOnPermissionListener.onPermissionGranted(null, true);
                    return;
                }

                if (mPermissionsSet.isEmpty()) {
                    Constants.WriteLog("The requested permission cannot be empty");
                    mOnPermissionListener.onPermissionGranted(null, true);
                    mOnPermissionListener.onPermissionError("The requested permission cannot be empty");
                    return;
                }

                if (!PermissionUtils.checkPermissionsInManifest(getActivity().getApplicationContext(), mPermissionsSet)) {
                    mOnPermissionListener.onPermissionGranted(null, false);
                    mOnPermissionListener.onPermissionError("The requested permission is not registered in the manifest");
                    return;
                }

                ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(getActivity().getApplicationContext(), mPermissionsSet);

                if (failPermissions == null || failPermissions.isEmpty()) {
                    mOnPermissionListener.onPermissionGranted(null, true);
                    mOnPermissionListener.onPermissionError("The requested permission has no denied permissions");
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putStringArray(Constants.EXTRA_PERMISSIONS, mPermissionsSet.toArray(new String[0]));
                bundle.putString(Constants.EXTRA_RATIONALE_MESSAGE, mRationalMessage);
                bundle.putString(Constants.EXTRA_RATIONALE_POSITIVE, mRationalDialogPositiveText);
                bundle.putString(Constants.EXTRA_RATIONALE_NEGATIVE, mRationalDialogNegativeText);
                bundle.putString(Constants.EXTRA_SETTINGS_MESSAGE, mSettingDialogMessage);
                bundle.putString(Constants.EXTRA_SETTINGS_POSITIVE, mSettingDialogPositiveText);
                bundle.putString(Constants.EXTRA_SETTINGS_NEGATIVE, mSettingDialogNegativeText);

                InvisibleFragment.build(mOnPermissionListener, bundle).requestNow(getActivity());
            }
        });
    }
}
