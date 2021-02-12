package com.vincent.speedmeter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    TextView speedDisplay;
    Boolean hudMode = false;
    private String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
    private String TAG = "SpeedMeter";
    private boolean hasAllPermissions = true;
    private int rejectTimes = 0;
    private final int rejectTimesLimit = 5;
    private LocationManager locationManager;
    private float km = 1000;
    private float hour = 60*60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedDisplay = findViewById(R.id.speedDisplay);
        speedDisplay.setOnClickListener((v) -> {
            switchMode();
        });
        checkPermission();


    }

    @Override
    protected void onResume() {
        super.onResume();
        initLocationManager();
        hideSystemUI();
    }

    @SuppressLint("MissingPermission")
    private void initLocationManager(){

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            new AlertDialog.Builder(this).setMessage("GPS not enabled").setPositiveButton("ok",null).create().show();
            Log.d(TAG,"gps not enable");
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, l);
    }

    private LocationListener l = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location.hasSpeed()){
                float speed = location.getSpeed();
                int speedKmH = (int)Math.ceil(speed*hour/km);
                updateSpeed(speedKmH);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    private void updateSpeed(int Speed){
        runOnUiThread(()->{
            speedDisplay.setText(Speed+"km/h");
        });
    }

    private void checkPermission(){
        for(String permission:permissions){
            if(ContextCompat.checkSelfPermission(getApplicationContext(),permission) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{permission},999);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length >0){
            hasAllPermissions &= (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            Log.d(TAG,"hasAllPermissions:"+hasAllPermissions);
            if(!hasAllPermissions){
                Log.d(TAG,"request permission again");
                rejectTimes++;
                checkPermission();
                if(rejectTimes >= rejectTimesLimit){
                    finish();
                }
            }
        }

    }

    private void switchMode(){
        runOnUiThread(()->{
            if(!hudMode){
                speedDisplay.setScaleY(-1);
                speedDisplay.setTranslationX(1);
            }else{
                speedDisplay.setScaleY(1);
                speedDisplay.setTranslationX(0);
            }
            hudMode = !hudMode;
        });


    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}