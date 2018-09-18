package tw.org.cic.morsensor_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TrackingMainViewActivity extends Activity /*implements LocationListener*/ {
    Button btnTrackStart;
    Button btnTrackStop;
    private LocationManager locationMgr;
    private String prov;
    private Criteria criteria = new Criteria();
    HandlerThread mLocationHandlerThread = null;
    Looper mLocationHandlerLooper = null;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_main_view);

//        Intent intent = new Intent(this, TrackingService.class);
//        this.startService(intent);
        Log.v("MainActivity", "into");

        /*btnTrackStart = (Button)findViewById(R.id.trackStart);
        btnTrackStart.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Log.d("0912",editTextName.getText().toString());
                    }
                }
        );*/

        if(!runtime_permissions())
            enable_buttons();
        /*this.locationMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationHandlerThread = new HandlerThread("locationHandlerThread");
        this.prov = LocationManager.NETWORK_PROVIDER;//this.locationMgr.getBestProvider(criteria, true);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // No one provider activated: prompt GPS
            if (this.prov == null || this.prov.equals("")) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                this.prov = this.locationMgr.getBestProvider(criteria, true);
            }

            this.locationMgr.requestLocationUpdates(this.prov, 1000, 0, this);
            Location location = this.locationMgr.getLastKnownLocation(this.prov);
            Log.v("main", Double.toString(location.getLatitude()));
            ((TextView)findViewById(R.id.tv_lng)).setText(Double.toString(location.getLongitude()));
            ((TextView)findViewById(R.id.tv_lat)).setText(Double.toString(location.getLatitude()));

            // One or both permissions are denied.
        } else {

            // The ACCESS_COARSE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        11);
            }
            // The ACCESS_FINE_LOCATION is denied, then I request it and manage the result in
            // onRequestPermissionsResult() using the constant MY_PERMISSION_ACCESS_FINE_LOCATION
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        11);
            }

        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.v("onchange", intent.getExtras().get("coordinates").toString());
                    ((TextView)findViewById(R.id.tv_lng)).setText(intent.getExtras().get("coordinates").toString());
//                    ((TextView)findViewById(R.id.tv_lat)).setText(Double.toString(location.getLatitude()));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private void enable_buttons() {
        btnTrackStart = (Button)findViewById(R.id.trackStart);
        btnTrackStart.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), TrackingService.class);
                        startService(i);
                    }
                }
        );

        btnTrackStop = (Button)findViewById(R.id.trackStop);
        btnTrackStop.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), TrackingService.class);
                        stopService(i);
                    }
                }
        );
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions();
            }
        }
    }

   /* @Override
    protected void onResume() {
        super.onResume();

        mLocationHandlerThread.start();
        mLocationHandlerLooper = mLocationHandlerThread.getLooper();

        try {
            Log.v("onchange", "Try resume");
            this.locationMgr.requestLocationUpdates(this.prov, 500, 0, this, mLocationHandlerLooper);
        }
        catch(SecurityException e){
            // The app doesn't have the correct permissions
        }
    }

    @Override
    protected void onPause() {
        try{
            Log.v("onpause", "onPause in");
            this.locationMgr.removeUpdates(this);
        }
        catch (SecurityException e){
            // The app doesn't have the correct permissions
        }

        mLocationHandlerLooper = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            mLocationHandlerThread.quitSafely();
        else
            mLocationHandlerThread.quit();

        mLocationHandlerThread = null;

        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v("onchange", Double.toString(location.getLatitude()));
        ((TextView)findViewById(R.id.tv_lng)).setText(Double.toString(location.getLongitude()));
        ((TextView)findViewById(R.id.tv_lat)).setText(Double.toString(location.getLatitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extra) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/
}
