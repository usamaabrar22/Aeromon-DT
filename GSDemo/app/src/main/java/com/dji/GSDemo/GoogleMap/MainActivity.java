package com.dji.GSDemo.GoogleMap;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.flightcontroller.FlightControllerState;
import dji.sdk.base.BaseProduct;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.products.Aircraft;


public class MainActivity extends FragmentActivity{
    protected static final String TAG = "MainActivity";

    private FlightController mFlightController;
    private double droneLocationLat, droneLocationLng, droneLocationAlt = 80;
    private TextView latT, lngT, altT;
    int toggleTextColor = 0;
    double altitude = 10;

    @Override
    protected void onResume(){
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);
        initFlightController();
        Log.e(TAG, "onResume caled");
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void setResultToToast(final String string){
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // When the compile and target version is higher than 22, please request the
        // following permissions at runtime to ensure the
        // SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE,
                    }
                    , 1);
        }

        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIDemoApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        latT = findViewById(R.id.latT);
        lngT = findViewById(R.id.lngT);
        altT = findViewById(R.id.altT);

    }

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "Inside Broadcast receiver");
            onProductConnectionChange();
            initFlightController();
        }
    };

    private void onProductConnectionChange()
    {
        initFlightController();
        Log.e(TAG, "onProductConnectionChange caled");
    }

    private void initFlightController() {
        Log.e(TAG, "initFlightController caled");

        BaseProduct product = DJIDemoApplication.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                mFlightController = ((Aircraft) product).getFlightController();
            }
        }

        if (mFlightController != null) {
            mFlightController.setStateCallback(new FlightControllerState.Callback() {

                @Override
                public void onUpdate(FlightControllerState djiFlightControllerCurrentState) {
                    droneLocationLat = djiFlightControllerCurrentState.getAircraftLocation().getLatitude();
                    droneLocationLng = djiFlightControllerCurrentState.getAircraftLocation().getLongitude();
                    droneLocationAlt = djiFlightControllerCurrentState.getAircraftLocation().getAltitude();

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "Handler called");
                            if (Double.isNaN(droneLocationLat) || Double.isNaN(droneLocationLng)){

                                Toast.makeText(MainActivity.this, droneLocationLat+"\n"+droneLocationLng+"\n"+droneLocationAlt, Toast.LENGTH_SHORT).show();

                            }
                            else{
                                Toast.makeText(MainActivity.this, droneLocationLat+"\n"+droneLocationLng+"\n"+droneLocationAlt, Toast.LENGTH_SHORT).show();

                                                        setTextToViews(droneLocationLat+"", droneLocationLng+"", droneLocationAlt+"");
                                droneLocationLat = getTrimmedLatZ(droneLocationLat);
                                droneLocationLng = getTrimmedLngX(droneLocationLng);
                                uploadCoordinates(droneLocationLat, droneLocationLng, droneLocationAlt);
                            }

                    handler.postDelayed(this, 10000);//Repeated delay after first one
                }
            }, 10000);//First time delay
                }
            });
        }
    }

    void uploadCoordinates(double lat, double lng, double alt){
        Log.e(TAG, "uploadCoordinates caled");


                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("coordinates").child("dronelocation");
                altitude += 10;
                reference.child("x").setValue(lng+"");
                reference.child("y").setValue(altitude+"");
                reference.child("z").setValue(lat+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
//                            Toast.makeText(ConnectionActivity.this, "Success: Data saved", Toast.LENGTH_SHORT).show();
//                            setResultToToast("Lat: "+droneLocationLat+"\nLong: "+droneLocationLng+"\nAltitude: "+droneLocationAlt);
//                            setTextToViews(droneLocationLat, droneLocationLng, droneLocationAlt);
                            uploadJangoCoordinates(lat, lng, alt);

                        }
                        if (!task.isSuccessful())
                        {
                            Toast.makeText(MainActivity.this, "Error saving data\n"+task.getException(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    public void setTextToViews(String lat, String lng, String alt){
        latT.setText("Latitude:  "+ lat);
        lngT.setText("Longitude: "+ lng);
        altT.setText("Altitude:  " +alt);

        if(toggleTextColor == 1){
            latT.setTextColor(Color.parseColor("#01411C"));
            lngT.setTextColor(Color.parseColor("#01411C"));
            altT.setTextColor(Color.parseColor("#01411C"));
            toggleTextColor = 0;
        }
        else {
            latT.setTextColor(Color.parseColor("#FFF"));
            lngT.setTextColor(Color.parseColor("#FFF"));
            altT.setTextColor(Color.parseColor("#FFF"));
            toggleTextColor = 1;
        }

    }

    public double getTrimmedLatZ(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(8) + "" + b.charAt(9) + "" + b.charAt(10);
        double d = Double.valueOf(c);
        return d;
    }

    public double getTrimmedLngX(double coordinate){
        String b = String.valueOf(coordinate);
        String c = b.charAt(7) + "" + b.charAt(8) + "" + b.charAt(9);
        double d = Double.valueOf(c);
        return d;
    }

    public void uploadJangoCoordinates(double lat, double lng, double alt){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("jango").child("coordinates").push();
        altitude += 10;
        reference.child("x").setValue(lng+"");
        reference.child("y").setValue(altitude+"");
        reference.child("z").setValue(lat+"").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
//                            Toast.makeText(ConnectionActivity.this, "Success: Data saved", Toast.LENGTH_SHORT).show();
//                            setResultToToast("Lat: "+droneLocationLat+"\nLong: "+droneLocationLng+"\nAltitude: "+droneLocationAlt);
//                            setTextToViews(droneLocationLat, droneLocationLng, droneLocationAlt);

                }
                if (!task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Error saving data\n"+task.getException(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


}
