package com.onedevapp.nativeplugin.imagepicker;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

/**
 * Helper functions used in Image Compression by AndroidWave
 *
 * Created on : Dec 30, 2018
 * Author     : AndroidWave
 * Website    : https://androidwave.com/
 */
public class ImageCompressor {
    //max width and height values of the compressed image is taken as 612x816
    private int maxWidth = 612;
    private int maxHeight = 816;
    private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    private int quality = 80;
    private String destinationDirectoryPath;

    /**
     * Constructor
     * @param context current Context
     */
    public ImageCompressor(Context context) {
        destinationDirectoryPath = context.getCacheDir().getPath() + File.separator + "images";
    }

    /**
     * set Max Width
     * @param maxWidth compression max width
     * @return ImageCompressor itself
     */
    public ImageCompressor setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * set Max Height
     * @param maxHeight compression max height
     * @return ImageCompressor itself
     */
    public ImageCompressor setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        return this;
    }

    /**
     * set Compress Format
     * @param compressFormat Bitmap.CompressFormat
     * @return ImageCompressor itself
     */
    public ImageCompressor setCompressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    /**
     * set Quality
     * @param quality compression quality
     * @return ImageCompressor itself
     */
    public ImageCompressor setQuality(int quality) {
        this.quality = quality;
        return this;
    }

    /**
     * set Destination Directory Path
     * @param destinationDirectoryPath path to save file
     * @return ImageCompressor itself
     */
    public ImageCompressor setDestinationDirectoryPath(String destinationDirectoryPath) {
        this.destinationDirectoryPath = destinationDirectoryPath;
        return this;
    }

    /**
     * compress To File
     * @param imageFile file to compress
     * @return new compressed file
     * @throws IOException IOException when file cant create or read
     */
    public File compressToFile(File imageFile) throws IOException {
        return compressToFile(imageFile, imageFile.getName());
    }

    /**
     * compress To File
     * @param imageFile file to compress
     * @param compressedFileName compressed file name
     * @return new compressed file
     * @throws IOException IOException when file cant create or read
     */
    public File compressToFile(File imageFile, String compressedFileName) throws IOException {
        return ImageUtil.compressImage(imageFile, maxWidth, maxHeight, compressFormat, quality,
                destinationDirectoryPath + File.separator + compressedFileName);
    }

    /**
     * compress To Bitmap from file
     * @param imageFile file to compress
     * @return  new compressed bitmap
     * @throws IOException IOException when file cant create or read
     */
    public Bitmap compressToBitmap(File imageFile) throws IOException {
        return ImageUtil.decodeSampledBitmapFromFile(imageFile, maxWidth, maxHeight);
    }

    /**
     * compress To Bitmap from bitmap
     * @param mBitmap bitmap to compress
     * @return new compressed bitmap
     */
    public Bitmap compressToBitmap(Bitmap mBitmap) {
        return ImageUtil.decodeSampledBitmapFromBitmap(mBitmap, maxWidth, maxHeight);
    }
}
