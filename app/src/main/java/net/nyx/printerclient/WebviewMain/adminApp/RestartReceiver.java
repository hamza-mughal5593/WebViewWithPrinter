package net.nyx.printerclient.WebviewMain.adminApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.nyx.printerclient.MainActivity;

public class RestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("343434", "onReceive: " );
        Intent restartIntent = new Intent(context, MainActivity.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(restartIntent);
    }
}