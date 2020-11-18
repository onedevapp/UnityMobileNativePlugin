package com.onedevapp.nativeplugin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.onedevapp.nativeplugin.rt_permissions.PermissionUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import static com.onedevapp.nativeplugin.Utils.*;

public class AndroidBridge {

    // region Declarations
    private static ProgressDialog mProgressDialog;  //Progress dialog reference to dismiss later
    private static WeakReference<Activity> mActivityWeakReference; //Activity references
    //endregion

    /**
     * Shows toast message
     *
     * @param activity current context
     * @param message message to be shown
     * @param length toast length, 1 means long else short
     */
    public static void ShowToast(final Activity activity, final String message, final int length)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(activity, message, (length == 1 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
            }
        });
    }

    /**
     * Shows alert dialog
     *
     * @param activity current context
     * @param title alert dialog title
     * @param message alert message
     * @param positiveLabel positive button text
     * @param positiveListener button onclick listener
     */
    public static void ShowAlertMessage(final Activity activity, final String title, final String message, final String positiveLabel, final OnClickListener positiveListener)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        positiveListener.onClick();
                    }
                });
                builder.setCancelable(false);

                builder.show();
            }
        });
    }

    /**
     * Shows alert dialog with two  options
     *
     * @param activity current context
     * @param title alert dialog title
     * @param message alert message
     * @param positiveLabel positive button text
     * @param positiveListener positive button onclick listener
     * @param negativeLabel negative button text
     * @param negativeListener negative button onclick listener
     */
    public static void ShowConfirmationMessage(final Activity activity, final String title, final String message, final String positiveLabel, final OnClickListener positiveListener, final String negativeLabel, final OnClickListener negativeListener)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title);
                builder.setMessage(message);
                builder.setPositiveButton(positiveLabel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        positiveListener.onClick();
                    }
                });
                builder.setNegativeButton(negativeLabel, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        negativeListener.onClick();
                    }
                });
                builder.setCancelable(false);

                builder.show();
            }
        });
    }

    /**
     * Shows progressbar dialog
     * @param activity  current context
     * @param title  progress dialog title
     * @param message  progress dialog message
     * @param cancelable can cancel dialog
     */
    public static void ShowProgressBar(final Activity activity, final String title, final String message, final boolean cancelable)
    {
        mActivityWeakReference = new WeakReference<>(activity);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (title.isEmpty() && message.isEmpty()){
                    mProgressDialog = new ProgressDialog(activity, R.style.TransparentProgressDialog);
                }
                else{
                    mProgressDialog = new ProgressDialog(activity);
                    mProgressDialog.setMessage(message);
                    mProgressDialog.setTitle(title);
                }
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(cancelable);
                mProgressDialog.setCanceledOnTouchOutside(cancelable);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mProgressDialog.show();
            }
        });
    }

    /**
     * Dismiss current progressbar dialog
     */
    public static void DismissProgressBar()
    {
        try
        {
            mActivityWeakReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mProgressDialog != null)
                        mProgressDialog.dismiss();
                }
            });
        }
        catch (Exception localException) {
            Constants.WriteLog(localException.toString());
        }finally {
            mActivityWeakReference = null;
        }
    }

    /**
     * Show time picker dialog
     *
     * @param activity  current context
     * @param mOnSelectListener callback handler
     */
    public static void ShowTimePicker(final Activity activity, final OnSelectListener mOnSelectListener){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Get Current Time
                final Calendar c = Calendar.getInstance();
                final int mHour = c.get(Calendar.HOUR_OF_DAY);
                final int mMinute = c.get(Calendar.MINUTE);

                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                mOnSelectListener.onSelected(hourOfDay + ":" + String.format("%02d", minute) +":00");
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });
    }

    /**
     * Show time picker dialog
     * @param activity  current context
     * @param mHour required hour
     * @param mMinute required minutes
     * @param is24Hours to show 24 hrs or 12 hrs format
     * @param mOnSelectListener callback handler
     */
    public static void ShowTimePicker(final Activity activity, final int mHour, final int mMinute, final boolean is24Hours, final OnSelectListener mOnSelectListener){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(activity,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {

                                mOnSelectListener.onSelected(hourOfDay + ":" + String.format("%02d", minute) +":00");
                            }
                        }, mHour, mMinute, is24Hours);
                timePickerDialog.show();
            }
        });
    }

    /**
     * Show date picker dialog
     *
     * @param activity  current context
     * @param mOnSelectListener callback handler
     */
    public static void ShowDatePicker(final Activity activity, final OnSelectListener mOnSelectListener){

        // Get Current Date
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // store the data in one string and set it to text
                                String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth
                                        + "/" + year;
                                mOnSelectListener.onSelected(selectedDate);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    /**
     * Show date picker dialog
     *
     * @param activity current context
     * @param mYear required year
     * @param mMonth required month
     * @param mDay required day
     * @param mOnSelectListener callback handler
     */
    public static void ShowDatePicker(final Activity activity, final int mYear, final int mMonth, final int mDay, final OnSelectListener mOnSelectListener){
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // store the data in one string and set it to text
                                String selectedDate = (monthOfYear + 1) + "/" + dayOfMonth
                                        + "/" + year;
                                mOnSelectListener.onSelected(selectedDate);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });
    }

    /**
     * Opens application settings page
     * @param activity current context
     */
    public static void OpenSettings(final Activity activity)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Go to your app's Settings page to let user turn on the necessary permissions.
                try{
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                }catch (NullPointerException e){
                    Constants.WriteLog("Exception (resume) : "+ e.toString());
                }
            }
        });
    }

    /**
     * Opens application settings page
     * Passes Constants.REQUEST_CODE_OPEN_SETTINGS as Result Code
     *
     * @param activity current context
     */
    public static void OpenSettingsForResult(final Activity activity)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Go to your app's Settings page to let user turn on the necessary permissions.
                try{
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivityForResult(intent, Constants.REQUEST_CODE_OPEN_SETTINGS);
                }catch (NullPointerException e){
                    Constants.WriteLog("Exception (resume) : "+ e.toString());
                }
            }
        });
    }

    /**
     * Enable location service
     * @param activity current context
     */
    public static void EnableLocation(final Activity activity) {

        if (CheckPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            SettingsClient client = LocationServices.getSettingsClient(activity);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

            task.addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    // All location settings are satisfied. The client can initialize
                    // location requests here.
                    // ...
                }
            });

            task.addOnFailureListener(activity, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity, Constants.REQUEST_LOCATION_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                }
            });
        }else{
            Constants.WriteLog("ACCESS_FINE_LOCATION permission not granted");
        }
    }

    /**
     * Checks if the phone is rooted.
     *
     * @return true if the phone is rooted, false otherwise.
     */
    public static boolean IsDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    /**
     * Check whether any network is available or not
     *
     * @param activity current context
     * @return boolean true if any network available else false
     */
    @SuppressLint("MissingPermission")
    public static boolean IsNetworkAvailable(final Activity activity) {

        ConnectivityManager connMgr = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Constants.WriteLog("Active network is wifi");
                return true;
            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Constants.WriteLog("Active network is mobile");
                return true;
            }else{
                return false;
            }
        }else{
            Constants.WriteLog("No active network found");
            return false;
        }
    }

    /**
     * This method checks whether the given permission is already granted or not.
     *
     * @param activity      This is context of the current activity
     * @param permission    This is the permission we need to check
     * @return  boolean     Returns True if already permission granted for this permission else false.
     */
    public static boolean CheckPermission(Activity activity,  String permission) {

        return PermissionUtils.checkPermission(activity, permission);
    }

    /**
     * This method checks whether the given permission can show rationale dialog.
     *
     * @param activity      This is context of the current activity
     * @param permission    This is the permission we need to check
     * @return  boolean     Returns True if permission is requested but not granted and can show rationale dialog else false.
     */
    public static boolean CheckPermissionRationale(Activity activity,  String permission) {

        return PermissionUtils.checkPermissionRationale(activity, permission);
    }

    /**
     * OnClick listener for every action
     */
    public interface OnClickListener {
        void onClick();
    }

    /**
     * OnSelect listener for every select action
     */
    public interface OnSelectListener {
        void onSelected(String selectedValue);
    }
}
