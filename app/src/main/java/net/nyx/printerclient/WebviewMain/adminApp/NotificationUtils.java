package net.nyx.printerclient.WebviewMain.adminApp;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import net.nyx.printerclient.MainActivity;
import net.nyx.printerclient.R;

public class NotificationUtils {


    public static void createNotificationChannel(Context context) {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "App Notifications";
            String description = "Notifications to reopen the app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(
                    "reopen_channel",
                    name,
                    importance
            );
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//    public static void showReopenNotification(Context context) {
//        // Create intent to reopen main Activity
//        Intent intent = new Intent(context, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(
//                context,
//                0,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        // Build notification
//        Notification notification = new NotificationCompat.Builder(context, "reopen_channel")
//                .setContentTitle("Action Required")
//                .setContentText("Tap to reopen the app")
//                .setSmallIcon(R.drawable.app_icon)
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true)
//                .build();
//
//        // Show notification
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//        notificationManager.notify(1, notification); // Unique notification ID
//    }

    public static void showReopenNotification(Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.S) { // Android 12
            String channelId = "reopen_channel";

            Intent intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.drawable.app_icon)
                    .setContentTitle("Action Required")
                    .setContentText("Tap to reopen the app")
                    .setAutoCancel(true)      // Prevent auto-dismiss on tap
                    .setOngoing(true)          // Prevent swipe dismissal
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Reopen App Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                notificationManager.createNotificationChannel(channel);
            }

            if (notificationManager != null) {
                notificationManager.notify(1001, builder.build());
                playBeep(context);
            } else {
                Toast.makeText(context, "NotificationManager is null", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public static MediaPlayer mediaPlayer; // Declare MediaPlayer as a class-level variable

    public static void playBeep(Context context) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // Stop and release the current MediaPlayer if it's already playing
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer(); // Create a new MediaPlayer instance

        try {
            AssetFileDescriptor descriptor = context.getAssets().openFd("beepbeep.mp3");
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

}
