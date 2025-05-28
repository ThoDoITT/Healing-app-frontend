package com.example.healingapp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.healingapp.common.Consts;
import com.example.healingapp.utils.AlarmDismissHelper;

public class AlarmDismissReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmDismissNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && "DISMISS_ALARM_ACTION".equals(intent.getAction())) {
            int sessionId = intent.getIntExtra(Consts.EXTRA_SESSION_ID, -1);
            Log.i(TAG, "Dismiss action received from notification for session ID: " + sessionId);
            AlarmDismissHelper.dismissAlarmAndFinalizeSession(context.getApplicationContext(), sessionId);


        }
    }
}
