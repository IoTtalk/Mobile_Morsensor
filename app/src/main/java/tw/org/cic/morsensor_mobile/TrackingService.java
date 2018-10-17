package tw.org.cic.morsensor_mobile;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;

import tw.org.cic.morsensor_mobile.TrackingMainViewActivity;

public class TrackingService extends Service {
    private String trackingName;
    private String trackingTime;
    private int trackingId;
    private int trackingApp;
    private boolean isWebOpen = false;

    private LocationListener listener;
    private LocationManager locationManager;
    private NotificationManager notificationManager;
    private NotificationChannel channelLove;
    private String ANDROID_CHANNEL_ID = "1";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


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

        trackingName = intent.getExtras().getString("trackingName");
        trackingId = Integer.parseInt(intent.getExtras().getString("trackingId"));
        trackingApp = Integer.parseInt(intent.getExtras().getString("trackingApp"));
        trackingTime = intent.getExtras().getString("trackingTime");
        Log.v("trackingTime", "trackingTime1 "+ isNumeric(trackingTime));
        setTrackingTime();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("ServiceonCreate", "ServiceonCreate");
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.v("getLastLocation", String.valueOf(location));
                            setCurrentLocation(location);
                        }
                    }
                });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    reStartLocationUpdate();
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    Log.v("LocationCallback", String.valueOf(location) + "\n" + location.getAccuracy());
                    if(location.getAccuracy() >= 50) {
                        reStartLocationUpdate();
                        return;
                    }
                    setCurrentLocation(location);
                }
            }

            ;
        };

        startLocationUpdates();



        /*listener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                Intent i = new Intent("location_update");
                i.putExtra("coordinates", location.getProvider()+" "+location.getLatitude() + " " + location.getLongitude());
                sendBroadcast(i);
                Log.v("onchange", location.getProvider()+" "+Double.toString(location.getLatitude())+" "+Double.toString(location.getLongitude()));
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        //code to do the HTTP request
                         TrackingDAI.push(location.getLatitude(), location.getLongitude(), trackingName, trackingId, getCurrentLocalDateTimeStamp());
                        Log.v("Thread", "TrackingDAI.GeoData finish");
                    }
                });
                thread.start();

                if(!isWebOpen) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://iot.iottalk.tw/map/?name="+trackingName+"&app="+trackingApp));
                    startActivity(browserIntent);
                    isWebOpen = true;
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                System.out.println("onStatusChanged");
                System.out.println("privider:" + provider);
                System.out.println("status:" + status);
                System.out.println("extras:" + extras);
            }

            @Override
            public void onProviderEnabled(String provider) {
                System.out.println("onProviderEnabled");
                System.out.println("privider:" + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                System.out.println("onProviderDisabled");
                System.out.println("privider:" + provider);
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
        }
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.v("locationManager", "GPS_PROVIDER");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        }

        Log.v("locationManager", String.valueOf(locationManager));*/

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
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
        stopLocationUpdates();
        Log.v("onDestroy", "onDestroy");
        /*if (locationManager != null) {
            locationManager.removeUpdates(listener);
            Log.v("onDestroy", "onDestroy");
        }*/
    }

    private void setTrackingTime() {
        if(isNumeric(trackingTime)) {
            final int timer = Integer.parseInt(trackingTime);
            Thread timeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //code to do the HTTP request
                try{
                    Log.v("timeThread", "timeThread start " + timer);
                    Thread.sleep(timer*1000);

                    

                    stopSelf();

                    /*Log.v("timeThread", "timeThread end " + timer);
                    SharedPreferences settings;
                    Boolean isTrackingStart = false;
                    settings = getSharedPreferences(TrackingMainViewActivity.data,MODE_PRIVATE);
                    settings.edit()
                            .putBoolean(TrackingMainViewActivity.STATE_TRACK, isTrackingStart)
                            .commit();*/



                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        });
        timeThread.start();
        }
    }

    private void setCurrentLocation(final Location location) {
        Intent i = new Intent("location_update");
        i.putExtra("coordinates", location.getLatitude() + " " + location.getLongitude());
        sendBroadcast(i);
        Log.v("onchange", location.getProvider()+" "+Double.toString(location.getLatitude())+" "+Double.toString(location.getLongitude()));
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run(){
                //code to do the HTTP request
                TrackingDAI.push(location.getLatitude(), location.getLongitude(), trackingName, trackingId, getCurrentLocalDateTimeStamp());
                Log.v("Thread", "TrackingDAI.GeoData finish");
            }
        });
        thread.start();

        if(!isWebOpen) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://"+TrackingConfig.trackingHost+"/map/?name="+trackingName+"&app="+trackingApp));
            startActivity(browserIntent);
            isWebOpen = true;
        }
    }

    public String getCurrentLocalDateTimeStamp() {
        String date = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        return date;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(TrackingMainViewActivity.mLocationRequest,
                mLocationCallback,
                null /* Looper */);

        Log.v("startLocationUpdates", "startLocationUpdates");
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        Log.v("stopLocationUpdates", "stopLocationUpdates");
    }

    private void reStartLocationUpdate() {
        stopLocationUpdates();
        startLocationUpdates();
        Log.v("reStartLocationUpdate", "reStartLocationUpdate***************************************************");
    }
}
