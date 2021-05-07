package com.onedevapp.nativeplugin.imagepicker;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Helper functions used in Image Compression by AndroidWave
 *
 * Created on : Dec 30, 2018
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class ImageUtil {
    private ImageUtil() {

    }

    /**
     * To compress image file
     * @param imageFile image file to compress
     * @param reqWidth  width to compress image
     * @param reqHeight height to compress image
     * @param compressFormat Bitmap.CompressFormat
     * @param quality quality ranges from 0 to 100 of actual image (Higher is good)
     * @param destinationPath path to save
     * @return  compressed image file
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
     * decodeSampledBitmap From File
     * @param imageFile image file to compress
     * @param reqWidth  width to compress image
     * @param reqHeight height to compress image
     * @return compressed bitmap
     * @throws IOException IOException when file cant create or read
     */
    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly
        ExifInterface exif;
        exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
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
     * @param mBitmap image bitmap to compress
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
     * @param options BitmapFactory.Options
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
