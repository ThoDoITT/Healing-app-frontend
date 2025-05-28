package com.example.healingapp.ui.sleep;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.common.Consts;
import com.example.healingapp.utils.AlarmDismissHelper;

public class AlarmDismissActivity extends AppCompatActivity {
    private static final String TAG = "AlarmDismissActivity";
    private int sessionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_dismiss);

        // Hiển thị trên màn hình khóa và bật màn hình
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            // KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            // if(keyguardManager!= null) keyguardManager.requestDismissKeyguard(this, null); // Yêu cầu mở khóa
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        sessionId = getIntent().getIntExtra(Consts.EXTRA_SESSION_ID, -1);
        Log.i(TAG, "AlarmDismissActivity created for session ID: " + sessionId);

        TextView tvAlarmInfo = findViewById(R.id.tvAlarmInfo);
        Button btnDismissAlarm = findViewById(R.id.btnDismissAlarm);

        if (sessionId != -1) {
            tvAlarmInfo.setText("Báo thức cho phiên ngủ: " + sessionId);
        } else {
            tvAlarmInfo.setText("Lỗi: Không tìm thấy phiên báo thức!");
            btnDismissAlarm.setEnabled(false);
        }

        btnDismissAlarm.setOnClickListener(v -> {
            Log.i(TAG, "Dismiss button clicked for session ID: " + sessionId);
            AlarmDismissHelper.dismissAlarmAndFinalizeSession(getApplicationContext(), sessionId);
            finish(); // Đóng activity này
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AlarmDismissActivity destroyed for session ID: " + sessionId);
    }
}
