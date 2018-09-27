package tw.org.cic.morsensor_mobile;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Callable;

public class TrackingMainViewActivity extends Activity /*implements LocationListener*/ {
    Button btnTrackStart;
    Button btnTrackStop;
    private LocationManager locationMgr;
    private String prov;
    private Criteria criteria = new Criteria();
    HandlerThread mLocationHandlerThread = null;
    Looper mLocationHandlerLooper = null;
    private BroadcastReceiver broadcastReceiver;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;


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

//        if(!runtime_permissions())
            runtime_permissions(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return enable_buttons();
                }
            });
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

    private Boolean enable_buttons() {
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
        return true;
    }



    protected void runtime_permissions(final Callable<Boolean> SuccessCB) {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);


        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        Log.v("GPS", "Comein");
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                Log.v("GPS", "Success");
                try {
                    SuccessCB.call();
                }
                catch (Exception e) {}
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(TrackingMainViewActivity.this,
                                REQUEST_CHECK_SETTINGS);

                        SuccessCB.call();
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                    catch (Exception err) {

                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                enable_buttons();
            } else {
                runtime_permissions(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return enable_buttons();
                    }
                });
            }
        }
    }

}
