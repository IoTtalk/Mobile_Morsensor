package tw.org.cic.morsensor_mobile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class TrackingMainViewActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_main_view);
        Log.v("MainActivity", "into");


    }

}
