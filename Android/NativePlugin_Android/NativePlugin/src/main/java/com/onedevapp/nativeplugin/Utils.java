package com.onedevapp.nativeplugin;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Some helper functions
 */
public class Utils {

    /**
     * Checking the BUILD tag for test-keys. By default, stock Android ROMs from Google are built with release-keys tags.
     * If test-keys are present, this can mean that the Android build on the device is either a developer build or
     * an unofficial Google build.
     */
    public static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    /**
     * check if /system/app/Superuser.apk is present
     * This package is most often looked for on rooted devices. Superuser allows the user to authorize applications to run as root on the device.
     */
    public static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    /**
     * Check if the SU command was successful
     * Execute su and then id to check if the current user has a uid of 0 or if it contains (root).
     */
    public static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    /**
     * Create file with current timestamp name
     *
     * @param context current Context
     * @return New Image file
     * @throws IOException If a file could not be created
     */
    public static File createImageFile(Context context, boolean isTempFile) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (isTempFile)
            return File.createTempFile(mFileName, ".jpg", storageDir);
        else
            // Return the file target for the photo based on filename
            return new File(storageDir.getPath() + File.separator + mFileName);
    }

    /**
     * Get real file path from URI
     *
     * @param context    current Context
     * @param contentUri file URI
     * @return File path in Android Device
     */
    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static @Nullable
    Uri getUriFromFile(Context context, @Nullable File file) {
        if (file == null)
            return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".native_plugin-file-provider", file);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * Converts image URI to bitmap
     * Bitmap can be null if file URI is not accessible or not an image file
     *
     * @param context  current Context
     * @param photoUri file URI
     * @return Bitmap of file URI
     */
    public static Bitmap loadFromUri(Context context, Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
