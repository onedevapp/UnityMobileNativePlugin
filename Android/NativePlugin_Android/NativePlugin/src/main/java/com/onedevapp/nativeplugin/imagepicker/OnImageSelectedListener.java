package com.onedevapp.nativeplugin.imagepicker;

/**
 * Callback method when an image has been selected or captured.
 */
public interface OnImageSelectedListener {

    /**
     * Details of image selected
     * @param status boolean whether image selected is success or failure
     * @param message if status is true then message will be image details json else error details
     * @param errorCode type of an error
     */
    void onImageSelected(boolean status, String message, int errorCode);
}
