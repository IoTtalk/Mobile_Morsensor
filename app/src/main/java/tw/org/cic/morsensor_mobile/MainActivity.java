package tw.org.cic.morsensor_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        Log.v("MainActivity", "into");


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


