package net.nyx.printerclient.WebviewMain.adminApp;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class WebViewRefreshWorker extends Worker {

    public WebViewRefreshWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Send a broadcast to refresh the WebView
        Intent refreshIntent = new Intent("REFRESH_WEBVIEW");
        getApplicationContext().sendBroadcast(refreshIntent);

        scheduleNextRun();

        return Result.success();
    }
    private void scheduleNextRun() {
        // Schedule the next worker for the next 24 hours
        OneTimeWorkRequest nextWorkRequest = new OneTimeWorkRequest.Builder(WebViewRefreshWorker.class)
                .setInitialDelay(24, TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(nextWorkRequest);
    }
}