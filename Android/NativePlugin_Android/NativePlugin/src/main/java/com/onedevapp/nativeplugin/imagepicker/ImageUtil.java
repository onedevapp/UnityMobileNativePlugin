package com.onedevapp.nativeplugin.imagepicker;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.onedevapp.nativeplugin.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Helper functions used in Image Compression by AndroidWave
 * <p>
 * Created on : Dec 30, 2018
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class ImageUtil {

    private static final String CHILD_DIR = "OneCache";
    private static final String FILE_NAME = "JPEG_";
    private static final String TEMP_FILE_NAME = "img";
    private static final String FILE_EXTENSION = ".jpg";
    public static final String IMAGE_FILE_DATE_FORMAT = "yyyyMMddHHmmss";

    private static final int COMPRESS_QUALITY = 100;

    private ImageUtil() {

    }

    /**
     * To compress image file
     *
     * @param imageFile       image file to compress
     * @param reqWidth        width to compress image
     * @param reqHeight       height to compress image
     * @param compressFormat  Bitmap.CompressFormat
     * @param quality         quality ranges from 0 to 100 of actual image (Higher is good)
     * @param destinationPath path to save
     * @return compressed image file
     * @throws IOException IOException when file cant create or read
     */
    static File compressImage(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality, String destinationPath) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File(destinationPath).getParentFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            fileOutputStream = new FileOutputStream(destinationPath);
            // write the compressed bitmap at the destination specified by destinationPath.
            decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(compressFormat, quality, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

        return new File(destinationPath);
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
        String timeStamp = new SimpleDateFormat(IMAGE_FILE_DATE_FORMAT).format(new Date());
        String mFileName = FILE_NAME+ "_" + timeStamp;
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (isTempFile)
            return File.createTempFile(mFileName, FILE_EXTENSION, storageDir);
        else
            // Return the file target for the photo based on filename
            return new File(storageDir.getPath() + File.separator + mFileName);
    }

    /**
     * Save image to the App cache
     *
     * @param bitmap to save to the cache
     * @return file dir when file was saved
     */
    public static File saveImgToCache(Context context, Bitmap bitmap) {
        File cacheFile = null;
        String timeStamp = new SimpleDateFormat(IMAGE_FILE_DATE_FORMAT).format(new Date());
        String fileName = TEMP_FILE_NAME+ "_" + timeStamp;

        try {
            cacheFile = new File(context.getCacheDir(), CHILD_DIR );
            cacheFile.mkdirs();

            FileOutputStream stream = new FileOutputStream(cacheFile + File.separator + fileName + FILE_EXTENSION );
            bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESS_QUALITY, stream);
            stream.close();
        } catch (IOException e) {
            Constants.WriteLog("saveImgToCache error: " + bitmap + "::" + e.toString());
        }
        //return cacheFile;
        return new File(cacheFile.getPath() + File.separator + fileName + FILE_EXTENSION );
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
                Constants.WriteLog("getUriFromFile error: " + e.toString());
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
            Constants.WriteLog("loadFromUri error: " + e.toString());
        }
        return image;
    }

    /**
     * Get Image Orientation
     *
     * @param imageFile image file to read orientation
     * @return orientation
     * @throws IOException IOException when file cant create or read
     */
    public static int GetImageOrientation(File imageFile) throws IOException {
        ExifInterface exif;
        exif = new ExifInterface(imageFile.getAbsolutePath());
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
    }

    /**
     * Get Image Metadata from file
     *
     * @param imageFile image file to read Metadata
     * @return Metadata
     */
    public static BitmapFactory.Options GetImageMetadata(File imageFile) {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        return options;
    }

    /**
     * decodeSampledBitmap From File
     *
     * @param imageFile image file to compress
     * @param reqWidth  width to compress image
     * @param reqHeight height to compress image
     * @return compressed bitmap
     * @throws IOException IOException when file cant create or read
     */
    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = GetImageMetadata(imageFile);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly

        int orientation = GetImageOrientation(imageFile);
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }


    /**
     * decodeSampledBitmap From Bitmap
     *
     * @param mBitmap   image bitmap to compress
     * @param reqWidth  width to compress image
     * @param reqHeight height to compress image
     * @return compressed bitmap
     */
    static Bitmap decodeSampledBitmapFromBitmap(Bitmap mBitmap, int reqWidth, int reqHeight) {
        float scale = Math.min(((float) reqHeight / mBitmap.getWidth()), ((float) reqWidth / mBitmap.getHeight()));

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmap, reqWidth, reqHeight, true);
        return Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
    }

    /**
     * calculate In SampleSize
     *
     * @param options   BitmapFactory.Options
     * @param reqWidth  width to compress image
     * @param reqHeight height to compress image
     * @return SampleSize in int
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
