package tw.org.cic.morsensor_mobile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tw.org.cic.morsensor_mobile.TrackingDAI;

public class TrackingService extends Service {

    private LocationListener listener;
    private LocationManager locationManager;
    private NotificationManager notificationManager;
    private NotificationChannel channelLove;
    private String ANDROID_CHANNEL_ID = "1";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("test")
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(1, notification);


        } else {

            Notification.Builder builder = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("test")
                    .setPriority(Notification.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ServiceonCreate", "ServiceonCreate");

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getLatitude() + " " + location.getLongitude());
                sendBroadcast(i);
                Log.v("onchange", Double.toString(location.getLatitude())+" "+Double.toString(location.getLongitude()));
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        //code to do the HTTP request
                         TrackingDAI.push(location.getLatitude(), location.getLongitude(), "APP_TEST", 14, getCurrentLocalDateTimeStamp());
                        Log.v("Thread", "TrackingDAI.GeoData finish");
                    }
                });
                thread.start();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.v("ServiceOnCreate", "Permission checker");
            return;
        }
//        locationManager.getBestProvider(new Criteria(), true)
        Log.v("locationManager", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
        Log.v("locationManager", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.v("locationManager", "NETWORK_PROVIDER");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        } else if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.v("locationManager", "GPS_PROVIDER");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }
        /*Log.v("GPS_PROVIDER", String.valueOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)));
        Log.v("NETWORK_PROVIDER", String.valueOf(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)));
        locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), 0, 0, listener);*/
//        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        Log.v("locationManager", String.valueOf(locationManager));

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                TrackingDAI.main();
                Log.v("Thread", "TrackingDAI.main finish");
            }
        });
        thread.start();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(listener);
            Log.v("onDestroy", "onDestroy");
        }
    }

    public String getCurrentLocalDateTimeStamp() {
        String date = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        return date;
    }
}
