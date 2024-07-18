package net.nyx.printerclient.WebviewMain;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import net.nyx.printerclient.AppClass;
import net.nyx.printerclient.MainActivity;

public class MyWorker extends Worker {

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Your specific code here
        performTask();
        return Result.success();
    }

    private void performTask() {
        AppClass ctx = (AppClass) getApplicationContext();

        wakeUpDevice(ctx);
        Log.e("343434", "performTask: " );
//        Toast.makeText(this, "Toast here", Toast.LENGTH_SHORT).show();
        // Add your task code here

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