package net.nyx.printerclient;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.google.firebase.FirebaseApp;

import net.nyx.printerclient.WebviewMain.CacheClearScheduler;
import net.nyx.printerclient.WebviewMain.adminApp.NotificationUtils;

public class AppClass extends Application {

    public static Boolean isInBackground = true;
    private PowerManager.WakeLock wakeLock;
    private BootReceiver onScreenOffReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);


        CacheClearScheduler.scheduleDailyCacheClearance(this);
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        onScreenOffReceiver = new BootReceiver();
        registerReceiver(onScreenOffReceiver, filter);
    }
    public PowerManager.WakeLock getWakeLock() {
        if(wakeLock == null) {
            // lazy loading: first call, create wakeLock via PowerManager.
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "wakeup");
        }
        return wakeLock;
    }


}
