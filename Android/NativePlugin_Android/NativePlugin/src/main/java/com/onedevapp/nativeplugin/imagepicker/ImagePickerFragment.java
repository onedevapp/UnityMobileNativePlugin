package com.onedevapp.nativeplugin.imagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import com.onedevapp.nativeplugin.AndroidBridge;
import com.onedevapp.nativeplugin.Constants;
import com.onedevapp.nativeplugin.rt_permissions.OnPermissionListener;
import com.onedevapp.nativeplugin.rt_permissions.PermissionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class ImagePickerFragment extends Fragment {

    // region Declarations
    File mPhotoFile;
    boolean createTempFile = true;
    ImageCompressor mCompressor;
    OnImageSelectedListener selectedListener;

    /**
     * Set the Listener
     */
    private void setListener(OnImageSelectedListener selectedListener) {
        this.selectedListener = selectedListener;
    }

    /**
     * Set the Image Compressor
     */
    private void setCompressor(ImageCompressor mCompressor) {
        if (mCompressor == null)
            this.mCompressor = new ImageCompressor(getActivity());
        else
            this.mCompressor = mCompressor;
    }

    /**
     * Fragment builder
     *
     * @param bundle arguments for the fragments
     * @return InvisibleFragment instance
     */
    public static ImagePickerFragment build(OnImageSelectedListener selectedListener, ImageCompressor mCompressor, Bundle bundle) {
        ImagePickerFragment fragment = new ImagePickerFragment();
        fragment.setListener(selectedListener);
        fragment.setCompressor(mCompressor);
        fragment.setArguments(bundle);
        return fragment;
    }

    /**
     * Start fragment
     *
     * @param activity current context
     */
    public void requestNow(Activity activity) {
        if (activity != null) {
            if (Looper.getMainLooper() != Looper.myLooper()) {
                throw new RuntimeException("you must request file picker in main thread!!");
            }

            activity.getFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commit();

        } else {
            throw new RuntimeException("activity is null!!");
        }
    }

    /**
     * When fragment is created
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            int pickerType = bundle.getInt("pickerType", 0);
            createTempFile = bundle.getBoolean("createTempFile", true);
            if (pickerType == 0) selectImage();
            else if (pickerType == 1) requestStoragePermission(true);
            else if (pickerType == 2) requestStoragePermission(false);
        } else
            selectImage();
    }


    /**
     * Alert dialog for capture or select from galley
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    requestStoragePermission(true);
                } else if (items[item].equals("Choose from Library")) {
                    requestStoragePermission(false);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                    removeFragment();
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    /**
     * Capture image from camera
     */
    @SuppressLint("QueryPermissionsNeeded")
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageUtil.createImageFile(getActivity(), createTempFile);
            } catch (IOException ex) {
                // Error occurred while creating the File
                reportUpdateError(Constants.EC_IMAGE_PICKER_FILE_CANT_CREATE, ex.toString());
                removeFragment();
            }
            if (photoFile != null) {

                Uri photoURI = ImageUtil.getUriFromFile(getActivity(), photoFile);

                //Uri photoURI = FileProvider.getUriForFile(getActivity(),
                //        getActivity().getApplicationContext().getPackageName() + ".native_plugin-file-provider",
                //        photoFile);
                mPhotoFile = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO);

            }
        }
    }


    /**
     * Select image fro gallery
     */
    private void dispatchGalleryIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, Constants.REQUEST_GALLERY_PHOTO);
    }

    /**
     * Requesting multiple permissions (storage and camera) at once
     * This uses multiple permission model from dexter
     * On permanent denial opens settings dialog
     */
    private void requestStoragePermission(final boolean isCamera) {

        PermissionManager.Builder(getActivity())
                .handler(new OnPermissionListener() {
                    @Override
                    public void onPermissionGranted(String[] grantPermissions, boolean all) {

                        if (all)
                            if (isCamera) {
                                dispatchTakePictureIntent();
                            } else {
                                dispatchGalleryIntent();
                            }
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions) {
                        // open device settings when the permission is denied permanently
                        AndroidBridge.OpenSettings(getActivity());
                    }

                    @Override
                    public void onPermissionError(String errorMessage) {
                        reportUpdateError(Constants.EC_IMAGE_PICKER_PERMISSION_FAILED, errorMessage);
                    }
                })
                .addPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .addPermission(Manifest.permission.CAMERA)
                .requestPermission();

    }


    /**
     * Handle the request result when user switch back from Settings.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        // super, because overridden method will make the handler null, and we don't want that.
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap mPhotoBitmap = null;

            Uri selectedImage = null;
            try {
                if (requestCode == Constants.REQUEST_TAKE_PHOTO) {
                    mPhotoBitmap = mCompressor.compressToBitmap(mPhotoFile);
                    selectedImage = ImageUtil.getUriFromFile(getActivity(), mPhotoFile);
                } else if (requestCode == Constants.REQUEST_GALLERY_PHOTO) {
                    assert data != null;
                    selectedImage = data.getData();
                    //mPhotoBitmap = mCompressor.compressToBitmap(new File(Utils.getRealPathFromUri(getActivity(), selectedImage)));
                    mPhotoBitmap = mCompressor.compressToBitmap(ImageUtil.loadFromUri(getActivity(), selectedImage));
                }

                /*ByteArrayOutputStream stream = new ByteArrayOutputStream();

                assert mPhotoBitmap != null;
                mPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                String temp = Base64.encodeToString(byteArray, Base64.DEFAULT);*/

                JSONObject jso = new JSONObject();
                try {
                    File cacheFile = ImageUtil.saveImgToCache(getActivity(), mPhotoBitmap);
                    BitmapFactory.Options imageMetaData = ImageUtil.GetImageMetadata(cacheFile);
                    int orientation = ImageUtil.GetImageOrientation(cacheFile);

                    assert mPhotoBitmap != null;
                    jso.put("width", imageMetaData.outWidth);
                    jso.put("height", imageMetaData.outHeight);
                    jso.put("mimeType", imageMetaData.outMimeType);
                    jso.put("orientation", orientation);
                    assert selectedImage != null;
                    jso.put("uri", selectedImage.toString());
                    jso.put("path", (mPhotoFile != null) ? mPhotoFile.getAbsolutePath() : "");
                    jso.put("cacheFilePath", cacheFile.getAbsolutePath());
                    //jso.put("imageBase64", temp);
                    selectedListener.onImageSelected(true, jso.toString(), 0);

                } catch (JSONException e) {

                    reportUpdateError(Constants.EC_IMAGE_PICKER_INTERNAL_ERROR, e.toString());
                }
            } catch (Exception e) {

                reportUpdateError(Constants.EC_IMAGE_PICKER_FILE_NOT_READABLE, e.toString());
            }
        }

    }

    protected void removeFragment() {
        //Remove this fragment when work is done
        getFragmentManager().beginTransaction().remove(this).commit();

        try {
            Intent resumeUnityActivity = new Intent(getActivity(), getActivity().getClass());
            resumeUnityActivity.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getActivity().startActivityIfNeeded(resumeUnityActivity, 0);
        } catch (Exception e) {
            Constants.WriteLog("errorCode::" + e.toString() + "::error::" + Constants.EC_IMAGE_PICKER_INTERNAL_ERROR);
            //reportUpdateError(Constants.EC_IMAGE_PICKER_INTERNAL_ERROR, e.toString());
        }
    }

    /**
     * Common functions to report error to users
     *
     * @param errorCode error code
     * @param error     error message
     */
    protected void reportUpdateError(int errorCode, String error) {
        Constants.WriteLog("errorCode::" + errorCode + "::error::" + error);
        selectedListener.onImageSelected(false, error, errorCode);
    }
}
