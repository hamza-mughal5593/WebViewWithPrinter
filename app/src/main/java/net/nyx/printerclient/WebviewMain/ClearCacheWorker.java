package net.nyx.printerclient.WebviewMain;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;

public class ClearCacheWorker extends Worker {

    public ClearCacheWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("Heartbeat", "App is alive at: ${System.currentTimeMillis()}");

        try {
            File cacheDir = getApplicationContext().getCacheDir();
            if (cacheDir != null && cacheDir.isDirectory()) {
                deleteDir(cacheDir);
            }
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] children = dir.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteDir(child)) {
                        return false;
                    }
                }
            }
            return dir.delete();
        } else {
            return dir != null && dir.delete();
        }
    }
}