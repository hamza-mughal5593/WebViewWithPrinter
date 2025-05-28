package net.nyx.printerclient.WebviewMain;

import static net.nyx.printerclient.AppClass.isInBackground;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import net.nyx.printerclient.MainActivity;
import net.nyx.printerclient.R;
import net.nyx.printerclient.WebviewMain.adminApp.NotificationUtils;

import java.util.concurrent.TimeUnit;

public class MyForegroundService extends Service {
    private final IBinder binder = new LocalBinder();
    public class LocalBinder extends Binder {
        public MyForegroundService getService() {
            // Return this instance of MyService so clients can call public methods
            return MyForegroundService.this;
        }
    }
    private static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        Intent serviceIntent = new Intent(this, MyForegroundService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent);
//        }
//        NotificationUtils.showReopenNotification(getApplicationContext());
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
        NotificationUtils.showReopenNotification(getApplicationContext());
    }

//    @Override
//    public void onTrimMemory(int level) {
//        Intent serviceIntent = new Intent(this, MyForegroundService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(serviceIntent);
//        }
//        NotificationUtils.showReopenNotification(getApplicationContext());
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Intent notificationIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Perkchops")
                .setContentText("Running...")
                .setSmallIcon(R.drawable.app_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);



        new Thread(() -> {
            while (true) {
                // Check if your app's main activity is running
                if (!isAppRunning() && !isOverlayVisible) {
                    Log.e("345452", "isAppRunning: false" );

//                    if (isInBackground){
//                        handler.post(this::showOverlay);
//                    }

//                    restartApp();
//                    scheduleAppReopen(notification);
                }else {
                    Log.e("345452", "isAppRunning: true" );
                }
                try {
                    Thread.sleep(10000); // Check every 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();



        return START_STICKY;
    }
    private Handler handler = new Handler(Looper.getMainLooper()); // Handler to run on the main thread

    private boolean isOverlayVisible = false; // Flag to track overlay visibility

    private WindowManager windowManager;
    private View overlayView;
    private void showOverlay() {
        if (Settings.canDrawOverlays(this)) {
            isOverlayVisible = true;
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            // Define layout parameters for the overlay
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                            : WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            // Inflate your custom layout
            overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

            // Set up the button in the overlay
            Button restartButton = overlayView.findViewById(R.id.try_again_button);
            restartButton.setOnClickListener(v -> {
                restartApp();
                removeOverlay(); // Remove overlay after restarting
            });

            // Add the overlay to the window
            windowManager.addView(overlayView, params);

            playBeep();

        } else {
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
        }
    }
    private MediaPlayer mediaPlayer; // Declare MediaPlayer as a class-level variable

    public void playBeep() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // Stop and release the current MediaPlayer if it's already playing
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer(); // Create a new MediaPlayer instance

        try {
            AssetFileDescriptor descriptor = getAssets().openFd("beepbeep.mp3");
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true); // Set to loop the audio
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopBeep() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop(); // Stop playback
            }
            mediaPlayer.release(); // Release resources
            mediaPlayer = null; // Reset the MediaPlayer instance
        }
    }
    private void removeOverlay() {
        stopBeep();
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
            isOverlayVisible = false; // Allow the loop to check again
        }
    }
    private void restartApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private boolean isAppRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningTaskInfo task : activityManager.getRunningTasks(Integer.MAX_VALUE)) {
            if (task.topActivity != null && task.topActivity.getPackageName().equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void restartApp1() {
        Log.e("345452", "restart app: code" );
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        Intent restartIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this, 0, restartIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        long triggerTime = System.currentTimeMillis() + 1000; // Start after 1 second
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);



//        // Create an Intent to launch MainActivity
//        Intent restartIntent = new Intent(this, MainActivity.class);
//        restartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        // Create ActivityOptions for background activity start
//        ActivityOptions activityOptions = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            activityOptions = ActivityOptions.makeBasic();
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            activityOptions.setPendingIntentBackgroundActivityStartMode(
//                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
//            );
//        }
//
//        // Create a PendingIntent with the restart Intent
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                this,
//                0,
//                restartIntent,
//                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        // Get AlarmManager service
//        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
//        if (alarmManager != null) {
//            // Schedule the restart 5 seconds from now
//            alarmManager.set(
//                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                    System.currentTimeMillis() + 5000, // Delay in milliseconds
//                    pendingIntent,
//                    activityOptions.toBundle()
//            );
//        }


    }
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
