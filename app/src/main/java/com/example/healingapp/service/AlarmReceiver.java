package com.example.healingapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.healingapp.common.Consts;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int sessionId = intent.getIntExtra(Consts.EXTRA_SESSION_ID, -1);
        Log.i(TAG, "Alarm triggered! Session ID: " + sessionId);

        if (sessionId == -1) {
            Log.e(TAG, "Invalid session ID received in AlarmReceiver. Aborting.");
            return;
        }

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(Consts.EXTRA_SESSION_ID, sessionId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
