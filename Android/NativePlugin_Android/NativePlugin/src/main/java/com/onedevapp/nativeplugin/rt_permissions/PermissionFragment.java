package com.onedevapp.nativeplugin.rt_permissions;


import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.onedevapp.nativeplugin.Constants;

import java.util.ArrayList;

import static com.onedevapp.nativeplugin.Constants.*;


public class PermissionFragment extends Fragment {

    // region Declarations
    private OnPermissionListener mOnPermissionListener; //Callback listener
    private ArrayList<String> mGrantedPermissionsList;  //holds granted permissions in the request permissions.
    private ArrayList<String> mDeniedPermissionsList;   //holds denied permissions in the request permissions.
    private String[] mPermissionsArray;

    //Rational dialog
    private String mRationalMessage;
    private String mRationalDialogPositiveText;
    private String mRationalDialogNegativeText;

    //Settings dialog
    private String mSettingDialogMessage;
    private String mSettingDialogPositiveText;
    private String mSettingDialogNegativeText;

    //endregion

    /**
     * Default constructor
     */
    public PermissionFragment()
    {
        mOnPermissionListener = null;
    }

    public static PermissionFragment newInstance() {
        return new PermissionFragment();
    }

    /**
     * Fragment builder
     *
     * @param mOnPermissionListener handler for callback
     * @param bundle arguments for the fragments
     * @return InvisibleFragment instance
     */
    public static PermissionFragment build(OnPermissionListener mOnPermissionListener, Bundle bundle) {
        PermissionFragment fragment = new PermissionFragment();
        fragment.setPermissionListener(mOnPermissionListener);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Set the callback handler
     *
     * @param mOnPermissionListener permission callback
     */
    private void setPermissionListener(OnPermissionListener mOnPermissionListener) {
        this.mOnPermissionListener = mOnPermissionListener;
    }

    /**
     * Start fragment
     *
     * @param activity current context
     */
    public void requestNow(Activity activity){
        if (activity != null) {
            if (Looper.getMainLooper() != Looper.myLooper()) {
                throw new RuntimeException("you must request permission in main thread!!");
            }
            //if(activity instanceof AppCompatActivity)
            //    ((AppCompatActivity) activity).getSupportFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commit();
            //else
                activity.getFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commit();
        } else {
            throw new RuntimeException("activity is null!!");
        }
    }

    // region system functions

    /**
     * Save bundle values
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putStringArray(EXTRA_PERMISSIONS, this.mPermissionsArray);
        outState.putString(EXTRA_RATIONALE_MESSAGE, this.mRationalMessage);
        outState.putString(EXTRA_RATIONALE_POSITIVE, this.mRationalDialogPositiveText);
        outState.putString(EXTRA_RATIONALE_NEGATIVE, this.mRationalDialogNegativeText);
        outState.putString(EXTRA_SETTINGS_MESSAGE, this.mSettingDialogMessage);
        outState.putString(EXTRA_SETTINGS_POSITIVE, this.mSettingDialogPositiveText);
        outState.putString(EXTRA_SETTINGS_NEGATIVE, this.mSettingDialogNegativeText);

        super.onSaveInstanceState(outState);
    }

    /**
     * Restore bundle values
     */
    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if(savedInstanceState!=null) {
            // Retrieve the user email value from bundle.
            this.mPermissionsArray = savedInstanceState.getStringArray(EXTRA_PERMISSIONS);
            this.mRationalMessage = savedInstanceState.getString(EXTRA_RATIONALE_MESSAGE);
            this.mRationalDialogPositiveText = savedInstanceState.getString(EXTRA_RATIONALE_POSITIVE);
            this.mRationalDialogNegativeText = savedInstanceState.getString(EXTRA_RATIONALE_NEGATIVE);
            this.mSettingDialogMessage = savedInstanceState.getString(EXTRA_SETTINGS_MESSAGE);
            this.mSettingDialogPositiveText = savedInstanceState.getString(EXTRA_SETTINGS_POSITIVE);
            this.mSettingDialogNegativeText = savedInstanceState.getString(EXTRA_SETTINGS_NEGATIVE);
        }
    }

    /**
     * When fragment is created
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mOnPermissionListener == null)
        {
            getFragmentManager().beginTransaction().remove(this).commit();
        }
        else
        {
            this.mPermissionsArray = getArguments().getStringArray(EXTRA_PERMISSIONS);
            this.mRationalMessage = getArguments().getString(EXTRA_RATIONALE_MESSAGE);
            this.mRationalDialogPositiveText = getArguments().getString(EXTRA_RATIONALE_POSITIVE);
            this.mRationalDialogNegativeText = getArguments().getString(EXTRA_RATIONALE_NEGATIVE);
            this.mSettingDialogMessage = getArguments().getString(EXTRA_SETTINGS_MESSAGE);
            this.mSettingDialogPositiveText = getArguments().getString(EXTRA_SETTINGS_POSITIVE);
            this.mSettingDialogNegativeText = getArguments().getString(EXTRA_SETTINGS_NEGATIVE);

            startRequestPermission();
        }
    }

    /**
     * Handle the request result when user switch back from Settings.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_SETTINGS && mOnPermissionListener != null) {
            checkPermissionAfterResult();
        }
        else{
            Constants.WriteLog("mOnPermissionListener is null on result");
            mOnPermissionListener.onPermissionError("mOnPermissionListener is null on result");
        }
        // super, because overridden method will make the handler null, and we don't want that.
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Callback for the result from requesting permissions. This method is invoked for every call on requestPermissions
     *
     * @param requestCode   The request code passed in requestPermissions
     * @param permissions   The requested permissions
     * @param grantResults  The grant results for the corresponding permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE_PERMISSION) {

            // We can never holds granted permissions for safety, because user may turn some permissions off in settings.
            // So every time request, must request the already granted permissions again and refresh the granted permission set.
            mGrantedPermissionsList.clear(); // holds granted permissions in the request permissions.
            mDeniedPermissionsList.clear(); // holds denied permissions in the request permissions.

            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mGrantedPermissionsList.add(permissions[i]);
                }else {
                    mDeniedPermissionsList.add(permissions[i]);
                }
            }

            // If all permissions are granted.
            if (mGrantedPermissionsList.size() == permissions.length) {
                Constants.WriteLog("Yey, got all permissions.");
                mOnPermissionListener.onPermissionGranted(mGrantedPermissionsList.toArray(new String[0]), true);
            } else { // If some or none permissions are denied.
                mOnPermissionListener.onPermissionGranted(mGrantedPermissionsList.toArray(new String[0]), false);
                mOnPermissionListener.onPermissionDenied(mDeniedPermissionsList.toArray(new String[0]));
            }
        }

        removeFragment();
    }

    //endregion

    // region helper functions

    /**
     * Checks all permissions after returning from the settings window to the application
     */
    private void checkPermissionAfterResult() {

        mGrantedPermissionsList = new ArrayList<>();
        mDeniedPermissionsList = new ArrayList<>();

        for (String permission : mPermissionsArray) {
            if (PermissionUtils.isOverMarshmallow()) {
                if (PermissionUtils.checkPermission(getContext(), permission)) {
                    mGrantedPermissionsList.add(permission);
                }else{
                    mDeniedPermissionsList.add(permission);
                }
            }else{
                mGrantedPermissionsList.add(permission);
            }
        }

        // If all permissions are granted.
        if (mGrantedPermissionsList.size() == mPermissionsArray.length) {
            WriteLog("Yey, got all permissions.");
            mOnPermissionListener.onPermissionGranted(mGrantedPermissionsList.toArray(new String[0]), true);
        } else { // If some or none permissions are denied.
            mOnPermissionListener.onPermissionGranted(mGrantedPermissionsList.toArray(new String[0]), false);
            mOnPermissionListener.onPermissionDenied(mDeniedPermissionsList.toArray(new String[0]));
        }

        removeFragment();
    }

    /**
     * Function that performs request permission
     */
    private void startRequestPermission() {

        mGrantedPermissionsList = new ArrayList<>();
        mDeniedPermissionsList = new ArrayList<>();

        //Checks for any permissions are blocked or requested but not granted previously
        boolean anyRationalePermission = false;
        boolean anyBlockedPermission = false;

        for (String permission : mPermissionsArray) {
            if (PermissionUtils.isOverMarshmallow()) {
                if (!PermissionUtils.checkPermission(getContext(), permission)) {
                    mDeniedPermissionsList.add(permission);
                    if (shouldShowRequestPermissionRationale(permission)) {
                        anyRationalePermission = true;
                    }else{
                        //Permission is not requested for first time
                        if (PermissionUtils.isFirstTimeAskingPermission(getContext(), permission)) {
                            PermissionUtils.firstTimeAskingPermission(getContext(), permission);
                        }else{  //Permission denied on first time requested
                            anyBlockedPermission = true;
                        }
                    }
                }
            }else{
                mGrantedPermissionsList.add(permission);
            }
        }

        //If any permission is blocked, we'll show settings dialog to open
        if (anyBlockedPermission) {
            WriteLog("Showing settings dialog");
            showSettingDialog();
        }else if (anyRationalePermission) {  //If any permission is requested but not granted, we'll show rationale dialog
            WriteLog("Showing rationale dialog");
            showRationaleDialog();
        } else {    //we'll request all permissions to grant
            WriteLog("No rationale permission found.");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(mPermissionsArray, REQUEST_CODE_PERMISSION);
            }else{
                mOnPermissionListener.onPermissionGranted(mGrantedPermissionsList.toArray(new String[0]), true);
            }
        }
    }

    /**
     * Shows Settings dialog
     */
    void showSettingDialog() {
        PermissionUtils.showDialogMessage(getActivity(), mSettingDialogMessage,
                mSettingDialogPositiveText,
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        // Go to your app's Settings page to let user turn on the necessary permissions.
                        try{
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, REQUEST_CODE_OPEN_SETTINGS);
                        }catch (NullPointerException e){
                            Constants.WriteLog("Exception (resume) : "+ e.toString());
                            mOnPermissionListener.onPermissionError(e.toString());
                        }
                    }
                },
                mSettingDialogNegativeText,
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mOnPermissionListener.onPermissionDenied(mDeniedPermissionsList.toArray(new String[0]));
                    }
                });
    }

    /**
     * Shows Rational dialog
     */
    void showRationaleDialog() {
        PermissionUtils.showDialogMessage(getActivity(), mRationalMessage,
                mRationalDialogPositiveText,
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(mPermissionsArray, REQUEST_CODE_PERMISSION);
                        }
                    }
                },
                mRationalDialogNegativeText,
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        mOnPermissionListener.onPermissionDenied(mDeniedPermissionsList.toArray(new String[0]));
                    }
                });
    }


    protected void removeFragment() {
        //Remove this fragment when work is done
        getFragmentManager().beginTransaction().remove(this).commit();

        try
        {
            Intent resumeUnityActivity = new Intent(getActivity(), getActivity().getClass());
            resumeUnityActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getActivity().startActivityIfNeeded(resumeUnityActivity, 0);
        }
        catch (Exception e)
        {
            mOnPermissionListener.onPermissionError(e.toString());
        }
    }
    //endregion
}
