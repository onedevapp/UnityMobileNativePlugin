package com.onedevapp.unitynativeplugin;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.onedevapp.nativeplugin.AndroidBridge;
import com.onedevapp.nativeplugin.imagepicker.ImagePickerManager;
import com.onedevapp.nativeplugin.rt_permissions.OnPermissionListener;
import com.onedevapp.nativeplugin.rt_permissions.PermissionManager;
import com.onedevapp.nativeplugin.share.ShareManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity
{
    public final String TAG = MainActivity.class.getName();
    ImageView imageViewProfilePic;
    List<String> fileList = new ArrayList<>();
    Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for(int i=0; i<3;i++){

            try {
                fileList.add(CreateNewFileName("file_"+i+".txt"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        findViewById(R.id.enable_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionManager.Builder(MainActivity.this)
                        .handler(new OnPermissionListener() {
                            @Override
                            public void onPermissionGranted(String[] grantPermissions, boolean all) {

                                if(all)
                                    AndroidBridge.EnableLocation(MainActivity.this);
                            }

                            @Override
                            public void onPermissionDenied(String[] deniedPermissions) {
                                if(AndroidBridge.CheckPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)){
                                    // open device settings when the permission is
                                    // denied permanently
                                    AndroidBridge.OpenSettings(MainActivity.this);
                                }
                            }

                            @Override
                            public void onPermissionError(String errorMessage) {
                                Log.v(TAG, errorMessage);
                            }
                        })
                        .addPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .requestPermission();

            }

        });

        findViewById(R.id.imageViewProfilePic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* ImagePickerManager.Builder(MainActivity.this)
                        .handler(new OnImagePickerListener() {
                            @Override
                            public void onImageSelected(String imageDetails) {

                                try {
                                    JSONObject jso = new  JSONObject(imageDetails);
                                    boolean status = jso.getBoolean("status");
                                    if(status){
                                        String imageBase64 = jso.getString("imageBase64");
                                        ImageView image = (ImageView) findViewById(R.id.imageViewProfilePic);

                                        byte[] decodedString = Base64.decode(imageBase64, Base64.DEFAULT);
                                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                        image.setImageBitmap(decodedByte);
                                        fileUri = Uri.parse(jso.getString("uri"));
                                    }else{
                                        Log.v(TAG, jso.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    Log.v(TAG, e.toString());
                                }
                            }
                        })
                        .setPickerType(0)
                        .openImagePicker();*/

            }
        });
        findViewById(R.id.share_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareManager.Builder(MainActivity.this)
                        .setHeader("Sharing to")
                        .setMessage("Initial sharing message")
                        .shareTextContent();
            }
        });
        findViewById(R.id.share_files).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareManager.Builder(MainActivity.this)
                        .setHeader("Sharing to")
                        .setMessage("Initial sharing message")
                        .addMultipleFilePaths(fileList.toArray(new String[0]))
                        .shareMultipleFileContent();
            }
        });
        findViewById(R.id.share_text_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ShareManager.Builder(MainActivity.this)
                        .setHeader("Sharing to")
                        .setMessage("Initial sharing message")
                        .addMultipleFilePaths(fileList.toArray(new String[0]))
                        .shareFileContent();
            }
        });
        findViewById(R.id.share_on_whatsapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareManager share = ShareManager.Builder(MainActivity.this)
                        .setHeader("Sharing to")
                        .setWhatsAppMobileNo("")
                        .setMessage("Initial sharing message");
                if(fileUri != null)
                    share.addFileUri(fileUri);
                share.shareOnWhatsApp();
            }
        });
        findViewById(R.id.share_on_mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareManager share =ShareManager.Builder(MainActivity.this)
                        .setHeader("Sharing to")
                        .setMessage("Initial sharing message")
                        .addEmailAddress("xyz@gmai.com");
               /* if(fileUri != null)
                    share.addFileUri(fileUri);
                share.addMultipleFilePaths(fileList.toArray(new String[0]));*/
                share.shareOnEmail("Hello there!");
            }
        });
    }

    public String CreateNewFileName(String fileName) throws Exception{
        FileOutputStream fOut = openFileOutput(fileName,MODE_PRIVATE);
        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
        myOutWriter.append("Hello World!");
        myOutWriter.close();
        fOut.close();
        return new File(MainActivity.this.getFilesDir(), fileName).getAbsolutePath();
    }
}
