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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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

    private Spinner spnTimeSelector;
    String[] Mins = new String[] {"10", "20", "30", "40", "50", "60", "Unlimited"};
    private Spinner.OnItemSelectedListener spnTimeSelectorListener;
    String sel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_main_view);

        spnTimeSelector = (Spinner) findViewById(R.id.timeSelector);
        ArrayAdapter<String> adapterMins = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Mins);
        adapterMins.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTimeSelector.setAdapter(adapterMins);


        spnTimeSelectorListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sel = parent.getSelectedItem().toString();
                Log.v("spnTimeSelectorListener", sel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                sel = Mins[0];
                Log.v("onNothingSelected", sel);
            }
        };
        spnTimeSelector.setOnItemSelectedListener(spnTimeSelectorListener);
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
                    Log.v("onReceive", intent.getExtras().get("coordinates").toString());
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
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(i);
                        } else {
                            startService(i);
                        }
//                        startService(i);
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

}
