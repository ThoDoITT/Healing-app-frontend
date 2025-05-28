package com.example.healingapp.ui.sleep;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.common.Consts;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.models.sleep.SleepSession;
import com.example.healingapp.service.AlarmReceiver;
import com.example.healingapp.service.AlarmService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StartSleepActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnStartSleep, btnStopSleepManually;
    private TextView tvStatus;
    private RecyclerView recyclerViewHistory;
    private SleepSessionAdapter adapter;
    private AppDatabase db;
    private SharedPreferences sharedPreferences;
    private AlarmManager alarmManager;

    private long tempAlarmTimeMillis = 0; // Lưu trữ tạm thời thời gian báo thức đã chọn

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_sleep);

        db = AppDatabase.getDatabase(getApplicationContext());
        sharedPreferences = getSharedPreferences(Consts.PREFS_NAME, MODE_PRIVATE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        tvStatus = findViewById(R.id.tvStatus);
        btnStartSleep = findViewById(R.id.btnStartSleep);
        btnStopSleepManually = findViewById(R.id.btnStopSleepManually);
        recyclerViewHistory = findViewById(R.id.recyclerViewHistory);

        setupRecyclerView();
        observeSleepHistory();

        btnStartSleep.setOnClickListener(v -> {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, Consts.REQUEST_CODE_POST_NOTIFICATIONS);
                } else {
                    proceedToTimePicker(); // Nếu đã có quyền thì tiếp tục
                }
            } else {
                proceedToTimePicker(); // Với các phiên bản cũ hơn, không cần quyền này
            }
        });

        btnStopSleepManually.setOnClickListener(v -> stopSleepSessionManually());

        updateUIBasedOnSessionState();
    }

    private void proceedToTimePicker() {
        int activeSessionId = sharedPreferences.getInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, -1);
        if (activeSessionId != -1) {
            Toast.makeText(this, "Đang có một phiên ngủ hoạt động!", Toast.LENGTH_LONG).show();
        } else {
            showTimePickerDialog();
        }
    }

    private void setupRecyclerView() {
        adapter = new SleepSessionAdapter();
        recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHistory.setAdapter(adapter);
    }

    private void observeSleepHistory() {
        db.sleepDao().getAllSessionsLiveData().observe(this, sessions -> {
            adapter.submitList(sessions);
            if (sessions != null && !sessions.isEmpty()) {
                SleepSession latestCompleted = null;
                for (SleepSession s : sessions) {
                    if (s.getEndTimeMillis() > 0) {
                        latestCompleted = s;
                        break;
                    }
                }
                if (latestCompleted != null) {
                    // tvStatus được cập nhật trong updateUIBasedOnSessionState
                }
            }
            updateUIBasedOnSessionState(); // Cập nhật lại UI sau khi dữ liệu thay đổi
        });
    }

    private void updateUIBasedOnSessionState() {
        int activeSessionId = sharedPreferences.getInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, -1);
        if (activeSessionId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                SleepSession currentSession = db.sleepDao().getSessionById(activeSessionId);
                runOnUiThread(() -> {
                    if (currentSession != null && currentSession.getEndTimeMillis() == 0) {
                        tvStatus.setText("Đang ngủ từ: " + currentSession.getFormattedStartTime());
                        btnStartSleep.setVisibility(View.GONE);
                        btnStopSleepManually.setVisibility(View.VISIBLE);
                    } else {
                        // Phiên active trong SharedPreferences nhưng không hợp lệ trong DB, reset
                        sharedPreferences.edit().remove(Consts.KEY_ACTIVE_SLEEP_SESSION_ID).apply();
                        tvStatus.setText("Chào mừng!");
                        btnStartSleep.setVisibility(View.VISIBLE);
                        btnStopSleepManually.setVisibility(View.GONE);
                    }
                });
            });
        } else {
            tvStatus.setText("Chào mừng! Hãy bắt đầu theo dõi giấc ngủ.");
            btnStartSleep.setVisibility(View.VISIBLE);
            btnStopSleepManually.setVisibility(View.GONE);
            // Hiển thị thời gian ngủ cuối cùng nếu có
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.sleepDao().getAllSessionsList().stream()
                        .filter(s -> s.getEndTimeMillis() > 0)
                        .findFirst()
                        .ifPresent(lastSession -> runOnUiThread(() -> tvStatus.setText("Lần ngủ trước: " + lastSession.getFormattedDuration())));
            });
        }
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    tempAlarmTimeMillis = getMillisFromTime(hourOfDay, minute);
                    // Bước 2: Kiểm tra quyền SCHEDULE_EXACT_ALARM
                    checkAndRequestExactAlarmPermission();
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true); // true for 24-hour format
        timePickerDialog.setTitle("Chọn giờ thức dậy");
        timePickerDialog.show();
    }

    private long getMillisFromTime(int hourOfDay, int minute) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);

        if (alarmTime.getTimeInMillis() <= System.currentTimeMillis()) {
            alarmTime.add(Calendar.DAY_OF_MONTH, 1); // Nếu giờ đã qua, đặt cho ngày hôm sau
        }
        return alarmTime.getTimeInMillis();
    }


    private void checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                // intent.setData(Uri.parse("package:" + getPackageName())); // Không bắt buộc nhưng có thể thêm
                startActivityForResult(intent, Consts.REQUEST_CODE_SCHEDULE_EXACT_ALARM);
                Toast.makeText(this, "Vui lòng cấp quyền đặt báo thức chính xác.", Toast.LENGTH_LONG).show();
                return; // Chờ người dùng cấp quyền
            }
        }
        // Nếu đã có quyền (hoặc API < S), tiếp tục tạo phiên ngủ và đặt báo thức
        startNewSleepSessionAndSetAlarm(tempAlarmTimeMillis);
    }


    private void startNewSleepSessionAndSetAlarm(long alarmTimeMillis) {
        if (alarmTimeMillis == 0) {
            Log.e(TAG, "Alarm time is not set. Aborting.");
            Toast.makeText(this, "Lỗi: Thời gian báo thức chưa được đặt.", Toast.LENGTH_SHORT).show();
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SleepSession newSession = new SleepSession();
            newSession.setStartTimeMillis(System.currentTimeMillis());
            long currentTime = System.currentTimeMillis();
            // endTimeMillis và durationMillis sẽ là 0 (mặc định)
            newSession.setCreationDateMillis(currentTime); // GÁN NGÀY TẠO
            newSession.setStartTimeMillis(currentTime);    // startTimeMillis cũng là thời điểm này
            // endTimeMillis và durationMillis sẽ là 0 (mặc định)

            long newSessionIdLong = db.sleepDao().insertSession(newSession);
            int newSessionId = (int) newSessionIdLong;

            if (newSessionId > 0) {
                sharedPreferences.edit().putInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, newSessionId).apply();
                setAlarm(alarmTimeMillis, newSessionId); // Đặt báo thức
                runOnUiThread(() -> {
                    Log.i(TAG, "Phiên ngủ mới bắt đầu ID: " + newSessionId + ". Báo thức đặt lúc: " + formatTime(alarmTimeMillis));
                    Toast.makeText(StartSleepActivity.this, "Đã bắt đầu ngủ. Báo thức đặt lúc " +
                            formatTime(alarmTimeMillis), Toast.LENGTH_LONG).show();
                    updateUIBasedOnSessionState();
                });


            } else {
                runOnUiThread(() -> Toast.makeText(StartSleepActivity.this, "Lỗi khi tạo phiên ngủ trong DB!", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setAlarm(long alarmTimeMillis, int sessionId) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(Consts.EXTRA_SESSION_ID, sessionId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                sessionId, // Sử dụng sessionId làm requestCode duy nhất
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
                    Log.i(TAG, "Báo thức chính xác đã đặt cho phiên ID: " + sessionId);
                } else {
                    // Should have been caught by checkAndRequestExactAlarmPermission
                    Log.w(TAG, "Không thể đặt báo thức chính xác: Quyền chưa được cấp.");
                    Toast.makeText(this, "Không thể đặt báo thức. Vui lòng cấp quyền.", Toast.LENGTH_SHORT).show();
                }
            } else { // Pre-API 31
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
                Log.i(TAG, "Báo thức chính xác (pre-S) đã đặt cho phiên ID: " + sessionId);
            }
        } catch (SecurityException se) {
            Log.e(TAG, "Lỗi bảo mật khi đặt báo thức: ", se);
            Toast.makeText(this, "Lỗi bảo mật khi đặt báo thức.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopSleepSessionManually() {
        int activeSessionId = sharedPreferences.getInt(Consts.KEY_ACTIVE_SLEEP_SESSION_ID, -1);
        if (activeSessionId == -1) {
            Toast.makeText(this, "Không có phiên ngủ nào đang hoạt động.", Toast.LENGTH_SHORT).show();
            updateUIBasedOnSessionState(); // Đồng bộ lại UI
            return;
        }

        // 1. Hủy báo thức đã đặt
        Intent intent = new Intent(this, AlarmReceiver.class); // Intent phải giống hệt lúc đặt
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                activeSessionId, // Cùng requestCode
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE // FLAG_NO_CREATE để kiểm tra, FLAG_UPDATE_CURRENT nếu cần tạo để hủy
        );

        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // Hủy cả PendingIntent
            Log.i(TAG, "Báo thức cho phiên ID " + activeSessionId + " đã được hủy.");
        } else {
            Log.w(TAG, "Không tìm thấy PendingIntent để hủy cho phiên ID " + activeSessionId + " hoặc AlarmManager null.");
        }
        // Dừng AlarmService nếu nó đang chạy (trường hợp hiếm khi dừng thủ công)
        Intent serviceIntent = new Intent(this, AlarmService.class);
        stopService(serviceIntent);


        // 2. Cập nhật Room và SharedPreferences
        final int finalActiveSessionId = activeSessionId; // Cho lambda
        AppDatabase.databaseWriteExecutor.execute(() -> {
            SleepSession session = db.sleepDao().getSessionById(finalActiveSessionId);
            if (session != null) {
                if (session.getEndTimeMillis() == 0) { // Chỉ cập nhật nếu chưa kết thúc
                    session.setEndTimeMillis(System.currentTimeMillis());
                    session.setDurationMillis(session.getEndTimeMillis() - session.getStartTimeMillis());
                    db.sleepDao().updateSession(session);
                    Log.i(TAG, "Phiên ngủ ID " + finalActiveSessionId + " đã dừng thủ công. Thời gian: " + session.getFormattedDuration());
                }
            } else {
                Log.e(TAG, "Không tìm thấy phiên ID " + finalActiveSessionId + " trong DB để dừng thủ công.");
            }
            sharedPreferences.edit().remove(Consts.KEY_ACTIVE_SLEEP_SESSION_ID).apply();
            runOnUiThread(() -> {
                Toast.makeText(StartSleepActivity.this, "Đã dừng theo dõi giấc ngủ.", Toast.LENGTH_SHORT).show();
                updateUIBasedOnSessionState();
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Consts.REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Đã cấp quyền thông báo.", Toast.LENGTH_SHORT).show();
                proceedToTimePicker(); // Tiếp tục sau khi có quyền
            } else {
                Toast.makeText(this, "Cần quyền thông báo để hoạt động.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.REQUEST_CODE_SCHEDULE_EXACT_ALARM) {
            // Người dùng đã quay lại từ màn hình cài đặt quyền. Kiểm tra lại quyền.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Đã cấp quyền đặt báo thức chính xác.", Toast.LENGTH_SHORT).show();
                    if (tempAlarmTimeMillis > 0) { // Nếu có thời gian đã chọn trước đó
                        startNewSleepSessionAndSetAlarm(tempAlarmTimeMillis);
                    }
                } else {
                    Toast.makeText(this, "Quyền đặt báo thức chính xác chưa được cấp.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIBasedOnSessionState(); // Luôn cập nhật UI khi quay lại
        // Kiểm tra lại quyền đặt báo thức chính xác nếu cần thiết
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            Log.i(TAG, "onResume: Quyền đặt báo thức chính xác vẫn chưa được cấp.");
            // Có thể hiển thị một thông báo nhắc nhở người dùng
        }
    }

    private String formatTime(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }
}