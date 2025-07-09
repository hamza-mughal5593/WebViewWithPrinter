package net.nyx.printerclient.fcm;

import static net.nyx.printerclient.WebviewMain.Config.BASE_URL_API;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.nyx.printerclient.MainActivity;
import net.nyx.printerclient.R;
import net.nyx.printerclient.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "fcm_default_channel";

    // Called when a new token is generated
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New token: " + token);

        // TODO: Send token to your server if needed

      String res_id =   Utils.getString(this, "res_id");

      if (!res_id.isEmpty()){
          send_token(token,res_id);
      }


    }
    private void send_token(String token, String res_id) {

        String url = BASE_URL_API + "restaurant/firbase-token";

        // JSON payload
        JSONObject payload = new JSONObject();
        try {
            payload.put("restaurant_id", res_id);
            payload.put("fcmTokens", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("43543534", "rest_id  "+payload );

        // RequestQueue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        // JsonObjectRequest for POST
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle response
                        Log.d("VolleyResponse", response.toString());

//                        try {
//                            boolean status = response.getBoolean("status");
//
//                            if (status) {
//
//                                Toast.makeText(MainActivity.this, "Token Send", Toast.LENGTH_SHORT).show();
//                            } else {
//                                Toast.makeText(MainActivity.this, "Token error", Toast.LENGTH_SHORT).show();
//
//
//                            }

//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        Log.e("VolleyError", error.toString());
                        Toast.makeText(MyFirebaseMessagingService.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // Optional: Add headers if needed
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        // Add request to the queue
        requestQueue.add(jsonObjectRequest);
    }
    // Called when a push notification is received
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Log full payload
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Handle data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: " + remoteMessage.getData());
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            showNotification(title, body);
        }

        // Handle notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body);
        }
    }

    // Display a notification manually
    private void showNotification(String title, String message) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.new_order);


        // Create channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "New Order",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Click to see");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(soundUri, audioAttributes); // ✅ Set custom sound

            manager.createNotificationChannel(channel);
        }

        // Intent to open MainActivity
        Intent intent = new Intent(this, MainActivity.class); // your desired activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon) // replace with your app icon
                .setContentTitle(title)
                .setContentText(message)
                .setOngoing(true) // cannot be swiped away
                .setAutoCancel(false)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent);

        Notification notification = builder.build();
        manager.notify(999, notification); // use consistent ID

    }
}