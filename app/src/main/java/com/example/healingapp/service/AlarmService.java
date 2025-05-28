package com.example.healingapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.healingapp.R;
import com.example.healingapp.common.Consts;
import com.example.healingapp.ui.sleep.AlarmDismissActivity;

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private int currentSessionId = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
        } else {
            Log.e(TAG, "Failed to create MediaPlayer from default alarm URI.");
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            currentSessionId = intent.getIntExtra(Consts.EXTRA_SESSION_ID, -1);
        }
        Log.i(TAG, "AlarmService started. Session ID: " + currentSessionId);

        if (currentSessionId == -1) {
            Log.e(TAG, "Invalid session ID in AlarmService. Stopping self.");
            stopSelf();
            return START_NOT_STICKY;
        }

        createNotificationChannel();

        // Intent để mở AlarmDismissActivity khi nhấn vào thông báo
        Intent notificationTapIntent = new Intent(this, AlarmDismissActivity.class);
        notificationTapIntent.putExtra(Consts.EXTRA_SESSION_ID, currentSessionId);
        notificationTapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Quan trọng
        PendingIntent contentPendingIntent = PendingIntent.getActivity(
                this,
                currentSessionId, // requestCode
                notificationTapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Intent cho action "Tắt" trên thông báo
        Intent dismissBroadcastIntent = new Intent(this, AlarmDismissReceiver.class); // Sẽ tạo receiver này
        dismissBroadcastIntent.setAction("DISMISS_ALARM_ACTION"); // Để phân biệt
        dismissBroadcastIntent.putExtra(Consts.EXTRA_SESSION_ID, currentSessionId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                this,
                currentSessionId + 1000, // requestCode khác
                dismissBroadcastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, Consts.ALARM_SERVICE_CHANNEL_ID)
                .setContentTitle("Báo thức!")
                .setContentText("Đã đến giờ dậy rồi! (Phiên: " + currentSessionId + ")")
                .setSmallIcon(R.drawable.ic_alarm) // Hãy thêm icon này vào res/drawable
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(contentPendingIntent, true) // Quan trọng để hiển thị khi màn hình khóa
                .setContentIntent(contentPendingIntent)
                .addAction(R.drawable.ic_dismiss, "Tắt", dismissPendingIntent) // Thêm icon ic_dismiss
                .setOngoing(true) // Không cho người dùng vuốt bỏ
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        startForeground(Consts.ALARM_NOTIFICATION_ID, notification);

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }

        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 1000, 1000}; // Rung 1s, nghỉ 1s
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)); // 0 để lặp lại
            } else {
                vibrator.vibrate(pattern, 0);
            }
        }
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    Consts.ALARM_SERVICE_CHANNEL_ID,
                    "Kênh Dịch Vụ Báo Thức",
                    NotificationManager.IMPORTANCE_HIGH
            );
            serviceChannel.setDescription("Kênh cho thông báo báo thức đang kêu");
            serviceChannel.setSound(null, null); // Âm thanh được quản lý bởi MediaPlayer
            serviceChannel.enableVibration(false); // Rung được quản lý bởi Vibrator

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        Log.i(TAG, "AlarmService destroyed. Resources released.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
