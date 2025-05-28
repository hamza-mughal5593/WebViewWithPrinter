package net.nyx.printerclient.WebviewMain;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class CacheClearScheduler {

    public static void scheduleDailyCacheClearance(Context context) {
        long currentTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long initialDelay = calendar.getTimeInMillis() > currentTime
                ? calendar.getTimeInMillis() - currentTime
                : calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) - currentTime;

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(ClearCacheWorker.class, 1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "DailyCacheClearance",
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
        );
    }
}