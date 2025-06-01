package com.example.healingapp.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.healingapp.common.Consts;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.models.sleep.SleepSession;
import com.example.healingapp.service.AlarmService;

public class AlarmDismissHelper {
    private static final String TAG = "AlarmDismissHelper";

    public static void dismissAlarmAndFinalizeSession(Context context, int sessionId) {
        Log.d(TAG, "Cố gắng tắt báo thức và hoàn tất phiên ID: " + sessionId);

        Intent serviceIntent = new Intent(context, AlarmService.class);
        context.stopService(serviceIntent); // Dừng service nhạc chuông/rung

        if (sessionId == -1) {
            Log.e(TAG, "ID phiên không hợp lệ (-1), không thể hoàn tất.");
            // Cập nhật SharedPreferences nếu có thể
            SharedPreferences sharedPreferences = context.getSharedPreferences(Consts.PREFS_NAME, Context.MODE_PRIVATE);
            int activeId = sharedPreferences.getInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, -1);
            if(activeId != -1) { //Nếu có activeId mà không khớp, hoặc sessionId là -1
                Log.w(TAG, "Đã xóa activeSessionId không hợp lệ khỏi SharedPreferences do sessionId là -1");
                sharedPreferences.edit().remove(Consts.KEY_ACTIVE_SLEEP_SESSION_ID).apply();
            }
            return;
        }

        AppDatabase db = AppDatabase.getDatabase(context.getApplicationContext());
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SleepSession session = db.sleepDao().getSessionById(sessionId);
            if (session != null) {
                if (session.getEndTimeMillis() == 0) {
                    session.setEndTimeMillis(System.currentTimeMillis());
                    session.setDurationMillis(session.getEndTimeMillis() - session.getStartTimeMillis());
                    db.sleepDao().updateSession(session);
                    Log.i(TAG, "Phiên ID " + sessionId + " đã hoàn tất. Thời gian: " + session.getFormattedDuration());
                } else {
                    Log.w(TAG, "Phiên ID " + sessionId + " đã được hoàn tất trước đó.");
                }
            } else {
                Log.e(TAG, "Không tìm thấy phiên ID " + sessionId + " trong database.");
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences(Consts.PREFS_NAME, Context.MODE_PRIVATE);
            int currentActiveId = sharedPreferences.getInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, -1);
            if (currentActiveId == sessionId) { // Chỉ xóa nếu nó thực sự là active session hiện tại
                sharedPreferences.edit().remove(Consts.KEY_ACTIVE_SLEEP_SESSION_ID).apply();
                Log.i(TAG, "Đã xóa active session ID " + sessionId + " khỏi SharedPreferences.");
            } else {
                Log.w(TAG, "Phiên ID "+sessionId+" không phải là active session hiện tại trong SharedPreferences (hiện tại là "+currentActiveId+").");
            }
        });
    }
}
