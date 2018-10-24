package tw.org.cic.morsensor_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import tw.org.cic.tracking_mobile.TrackingMainViewActivity;

public class MainActivity extends Activity {
    Button btnMorSensor, btnTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        Log.v("MainActivity", "into");

        btnMorSensor = (Button)findViewById(R.id.MorSensor);
        btnMorSensor.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, MainViewActivity.class);
                        startActivity(intent);
                    }
                }
        );

        btnTracking = (Button)findViewById(R.id.Tracking);
        btnTracking.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, TrackingMainViewActivity.class);
                        startActivity(intent);
                    }
                }
        );


    }

    public void enterToMainViewActivity(View view)
    {
        Intent intent = new Intent(MainActivity.this, MainViewActivity.class);
        startActivity(intent);
    }

    public void enterToTrackingMainViewActivity(View view)
    {
        Intent intent = new Intent(MainActivity.this, TrackingMainViewActivity.class);
        startActivity(intent);
    }

}


