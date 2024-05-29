package net.nyx.printerclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    private static final String PREF_KIOSK_MODE = "pref_kiosk_mode";

    @Override
    public void onReceive(Context context, Intent intent) {


        Toast.makeText(context, "Please unlock", Toast.LENGTH_SHORT).show();
        if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
            AppClass ctx = (AppClass) context.getApplicationContext();
            // is Kiosk Mode active?
//                wakeUpDevice(ctx);

        }
    }

    private void wakeUpDevice(AppClass context) {

        PowerManager.WakeLock wakeLock = context.getWakeLock(); // get WakeLock reference via AppContext
        if (wakeLock.isHeld()) {
            wakeLock.release(); // release old wake lock
        }

        // create a new wake lock...
        wakeLock.acquire();

        // ... and release again
        wakeLock.release();
    }

}
