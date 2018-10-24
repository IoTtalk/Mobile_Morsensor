package tw.org.cic.tracking_mobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TrackingBroadcastReceiver extends BroadcastReceiver {
   @Override
   public void onReceive(Context context, Intent intent) {
       Log.v("TrackingBroadcast", "TrackingBroadcastReceiver in");
       Intent i = new Intent(context, TrackingService.class);
       context.stopService(i);
   }

}
