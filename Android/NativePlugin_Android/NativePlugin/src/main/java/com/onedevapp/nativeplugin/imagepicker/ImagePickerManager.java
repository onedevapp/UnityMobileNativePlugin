package com.onedevapp.nativeplugin.imagepicker;

import android.app.Activity;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/**
 * ImagePickerManager is the responsible class to capture or pick image from gallery.
 */
public class ImagePickerManager {

    // region Declarations
    private static ImagePickerManager instance;
    private final WeakReference<Activity> mActivityWeakReference; //Activity references

    //The result returned from this plugin
    private int mPickerType = 0;
    private boolean mTempFile = true;
    private ImageCompressor mCompressor = null;
    //endregion

    //region Constructor

    /**
     * Creates a builder
     *
     * @param activity the activity
     * @return a new {@link ImagePickerManager} instance
     */
    public static ImagePickerManager Builder(Activity activity) {
        if (instance == null) {
            instance = new ImagePickerManager(activity);
        }
        return instance;
    }

    //Private constructor with activity
    private ImagePickerManager(Activity activity) {
        this.mActivityWeakReference = new WeakReference<>(activity);
        mCompressor = new ImageCompressor(activity);
    }
    //endregion

    /**
     * Set the pickerType either choice = 0, camera = 1 or gallery = 2
     *
     * @param pickerType mode of image
     * @return ImagePickerManager itself
     */
    public ImagePickerManager setPickerType(int pickerType) {
        if (pickerType < 0 || pickerType > 3) {
            pickerType = 0;
        }

        this.mPickerType = pickerType;
        return instance;
    }

    /**
     * Set the image max width to compress
     *
     * @param maxWidth sets the image compressor max width
     * @return ImagePickerManager itself
     */
    public ImagePickerManager setMaxWidth(int maxWidth) {
        mCompressor.setMaxWidth(maxWidth);
        return instance;
    }

    /**
     * Set the image max height to compress
     *
     * @param maxHeight sets the image compressor max height
     * @return ImagePickerManager itself
     */
    public ImagePickerManager setMaxHeight(int maxHeight) {
        mCompressor.setMaxHeight(maxHeight);
        return instance;
    }


    /**
     * Set the image quality from 1 to 100
     *
     * @param quality sets the image compressor quality
     * @return ImagePickerManager itself
     */
    public ImagePickerManager setQuality(int quality) {
        mCompressor.setQuality(quality);
        return instance;
    }

    /**
     * To create temp file while capturing image
     *
     * @param isTempFile create file as temp
     * @return ImagePickerManager itself
     */
    public ImagePickerManager createImageAsTemp(boolean isTempFile) {
        this.mTempFile = isTempFile;
        return instance;
    }

    // region helper functions

    /**
     * Returns the current activity
     */
    protected Activity getActivity() {
        return mActivityWeakReference.get();
    }

    public void openImagePicker() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                bundle.putInt("pickerType", mPickerType);
                bundle.putBoolean("createTempFile", mTempFile);
                ImagePickerFragment.build(mCompressor, bundle).requestNow(getActivity());
            }
        });
    }
    //endregion
}
