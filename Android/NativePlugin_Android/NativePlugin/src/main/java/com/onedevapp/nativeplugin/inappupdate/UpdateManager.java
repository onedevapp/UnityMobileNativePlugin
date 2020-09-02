package com.onedevapp.nativeplugin.inappupdate;

import android.app.Activity;

//import androidx.lifecycle.Lifecycle;
//import androidx.lifecycle.LifecycleObserver;
//import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.play.core.install.model.AppUpdateType;
import com.onedevapp.nativeplugin.Constants;

import java.lang.ref.WeakReference;

public class UpdateManager {//implements LifecycleObserver {

    // region Declarations
    private static UpdateManager instance;

    private int requestCode = 9877;      //Request code for activity
    private WeakReference<Activity> mActivityWeakReference; //Activity references
    private OnUpdateListener mOnUpdateListener; //Callback listener
    private BaseUpdateClass mUpdateApp; //Parent class for app update mode
    private boolean resumeUpdates = false;

    //endregion

    //region Constructor

    /**
     * Creates a builder that uses the default requestCode.
     *
     * @param activity the activity
     * @return a new {@link UpdateManager} instance
     */
    public static UpdateManager Builder(Activity activity) {
        if (instance == null) {
            instance = new UpdateManager(activity);
        }
        return instance;
    }

    /**
     * Creates a builder
     *
     * @param activity    the activity
     * @param requestCode the request code to later monitor this update request via onActivityResult()
     * @return a new {@link UpdateManager} instance
     */
    public static UpdateManager Builder(Activity activity, int requestCode) {
        if (instance == null) {
            instance = new UpdateManager(activity, requestCode);
        }
        return instance;
    }

    //Private constructor with activity
    private UpdateManager(Activity activity) {
        this.mActivityWeakReference = new WeakReference<>(activity);

        updateMode(Constants.PLAY_STORE_UPDATE);
    }

    //Private constructor with activity and request code
    private UpdateManager(Activity activity, int requestCode) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        this.requestCode = requestCode;

        updateMode(Constants.PLAY_STORE_UPDATE);
    }
    //endregion

    // region Setters

    /**
     * Set the update mode.
     *
     * @param updateMode the update type
     * @return UpdateManager itself
     */
    public UpdateManager updateMode(int updateMode) {
        if(updateMode == Constants.PLAY_STORE_UPDATE){
            mUpdateApp = new PlayStoreUpdate(this);
        }else if(updateMode == Constants.THIRD_PARTY_UPDATE){
            mUpdateApp = new ThirdPartyUpdate(this);
        } else {
            Constants.WriteLog("Unknown Update mode");
        }

        return this;
    }

    /**
     * Set the update type.
     *
     * @param updateType the update type
     * @return UpdateManager itself
     */
    public UpdateManager updateType(int updateType) {

        if(updateType == 0){
            mUpdateApp.setUpdateType(AppUpdateType.FLEXIBLE);
        }else if(updateType == 1){
            mUpdateApp.setUpdateType(AppUpdateType.IMMEDIATE);
        }else{
            Constants.WriteLog("Unknown Update type");
            if(mOnUpdateListener != null)
                mOnUpdateListener.onUpdateError(-1, "Unknown Update type");
        }
        return this;
    }

    /**
     * Set the callback handler
     *
     * @param onUpdateListener the handler
     * @return UpdateManager itself
     */
    public UpdateManager handler(OnUpdateListener onUpdateListener) {
        this.mOnUpdateListener = onUpdateListener;
        mUpdateApp.setHandler(this.mOnUpdateListener);
        return this;
    }

    /**
     * Checks that the update is not stalled during 'onResume()'.
     * If the update is downloaded but not installed, will notify
     * the user to complete the update.
     *
     * @param resumeUpdates the resume updates
     * @return the update manager instance
     */
    /*public UpdateManager resumeUpdates(boolean resumeUpdates) {
        this.resumeUpdates = resumeUpdates;
        return this;
    }*/

    /**
     * Set the update link for download
     *
     * @param mUpdateLink the link for third party URL
     * @return UpdateManager itself
     */
    public UpdateManager updateLink(String mUpdateLink) {
        if(!mUpdateLink.isEmpty())
            mUpdateApp.setUpdateLink(mUpdateLink);
        else
        {
            Constants.WriteLog("Update link can't be empty");
            if(mOnUpdateListener != null)
                mOnUpdateListener.onUpdateError(-1, "Update link can't be empty");
        }
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

    /**
     * Returns the Request code
     */
    protected int getRequestCode() { return requestCode; }

    /**
     * Common functions to report error to users
     *
     * @param errorCode the link for third party URL
     * @param error the link for third party URL
     */
    protected void reportUpdateError(int errorCode, String error) {
        Constants.WriteLog( "errorCode::" +errorCode);
        Constants.WriteLog( "error::" +error);
        if (instance.mOnUpdateListener != null) {
            instance.mOnUpdateListener.onUpdateError(errorCode, error);
        }
    }

    //endregion

    // region Public functions

    /**
     * Check Update
     */
    public void checkUpdate() {
        try {
            mUpdateApp.checkUpdate();
        } catch (Exception e) {
            reportUpdateError(-1, "checkUpdate() : Error :"+ e.toString());
        }
    }

    /**
     * Start Update
     */
    public void startUpdate() {
        try {
            mUpdateApp.startUpdate();
        } catch (Exception e) {
            reportUpdateError(-1, "startUpdate() : Error :"+ e.toString());
        }
    }

    /**
     * Complete Update
     */
    public void completeUpdate() {
        try {
            mUpdateApp.completeUpdate();
        } catch (Exception e) {
            reportUpdateError(-1, "completeUpdate() : Error :"+ e.toString());
        }
    }

    /**
     * Continue Update
     */
    public void continueUpdate() {
        try {
            mUpdateApp.continueUpdate();
        } catch (Exception e) {
            reportUpdateError(-1, "continueUpdate() : Error :"+ e.toString());
        }
    }
    //endregion

    //region Lifecycle
    /*@OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        if (resumeUpdates)
            continueUpdate();
    }*/

    //endregion

}