package com.onedevapp.unitynativeplugin;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.onedevapp.nativeplugin.AndroidBridge;
import com.onedevapp.nativeplugin.rt_permissions.OnPermissionListener;
import com.onedevapp.nativeplugin.rt_permissions.PermissionManager;

public class MainActivity extends FragmentActivity
{
    public final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }
}
