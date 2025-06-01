package com.example.healingapp.ui.workout;

import static com.example.healingapp.common.Consts.EXTRA_DATA_ID;
import static com.example.healingapp.utils.TrackingUtils.formatDuration;
import static com.example.healingapp.utils.TrackingUtils.formatPace;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;

import com.example.healingapp.R;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.models.workout.RunningSession;
import com.example.healingapp.ui.HomeActivity;

import java.util.Locale;

public class DetailRunningActivity extends AppCompatActivity {

    private TextView tvDistance;
    private TextView tvPace;
    private TextView tvDuration;
    private TextView tvCalories;
    private TextView tvSteps;
    private AppDatabase db;
    private int idRunning = -1;
    private ImageView btn_back_detail_running;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_running);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

        db = AppDatabase.getDatabase(getApplicationContext()); // Khởi tạo database instance

        // Lấy runId từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_DATA_ID)) {
            idRunning = intent.getIntExtra(EXTRA_DATA_ID, -1);
        }

        if (idRunning != -1) {
            loadRunDetails(idRunning);
        } else {
            Toast.makeText(this, "Không tìm thấy ID của buổi chạy.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private void init() {
        tvDistance = findViewById(R.id.txtDistanceDT);
        tvPace = findViewById(R.id.txtPaceDT);
        tvDuration = findViewById(R.id.txtTimeDT);
        tvCalories = findViewById(R.id.txtCaloriesDT);
        tvSteps = findViewById(R.id.txtStepCounterDT);
        btn_back_detail_running = findViewById(R.id.btn_back_detail_running);

        btn_back_detail_running.setOnClickListener(v -> {
            Intent intent = new Intent(DetailRunningActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void loadRunDetails(int runId) {
        // Sử dụng LiveData để lắng nghe thay đổi
        db.runDao().getRunById(runId).observe(this, new Observer<RunningSession>() {
            @Override
            public void onChanged(RunningSession runData) {
                if (runData != null) {
                    populateUI(runData);
                } else {
                    Toast.makeText(DetailRunningActivity.this, "Không tìm thấy dữ liệu cho buổi chạy này.", Toast.LENGTH_LONG).show();
                    // Có thể đóng activity hoặc hiển thị thông báo lỗi
                }
            }
        });

        // **Cách khác nếu không dùng LiveData (cần chạy trên background thread):**
        /*
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            RunData runData = db.runDao().getRunByIdNonLiveData(runId);
            runOnUiThread(() -> {
                if (runData != null) {
                    populateUI(runData);
                } else {
                    Toast.makeText(DetailRunningActivity.this, "Không tìm thấy dữ liệu.", Toast.LENGTH_LONG).show();
                }
            });
        });
        */
    }

    private void populateUI(RunningSession runData) {
        // Giả sử RunData của bạn lưu trữ dữ liệu thô:
        // runData.distance (float, mét)
        // runData.pace (float, phút/km)
        // runData.duration (long, milliseconds)
        // runData.calories (long)
        // runData.steps (long)

        // Định dạng dữ liệu để hiển thị
        // 1. Distance (km)
        String distanceKmFormatted = String.format(Locale.US, "%.2f km", runData.distance / 1000.0f);
        tvDistance.setText(distanceKmFormatted);

        // 2. Pace (min/km)
        String paceFormatted = formatPace(runData.pace); // Sử dụng hàm helper
        tvPace.setText(paceFormatted + " /km");

        // 3. Duration (HH:MM:SS hoặc MM:SS)
        String durationFormatted = formatDuration(runData.duration); // Sử dụng hàm helper
        tvDuration.setText(durationFormatted);

        // 4. Calories (kcal)
        String caloriesFormatted = String.format(Locale.US, "%d kcal", runData.calories);
        tvCalories.setText(caloriesFormatted);

        // 5. Steps
        String stepsFormatted = String.format(Locale.US, "%d bước", runData.steps);
        tvSteps.setText(stepsFormatted);
    }

    private String formatDuration(long millis) {
        long seconds = (millis / 1000) % 60;
        long minutes = (millis / (1000 * 60)) % 60;
        long hours = (millis / (1000 * 60 * 60)) % 24;

        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    private String formatPace(float paceMinPerKm) {
        if (paceMinPerKm <= 0 || Float.isInfinite(paceMinPerKm) || Float.isNaN(paceMinPerKm)) {
            return "0:00";
        }
        int minutes = (int) paceMinPerKm;
        int seconds = (int) ((paceMinPerKm - minutes) * 60);
        return String.format(Locale.US, "%d:%02d", minutes, seconds);
    }
}