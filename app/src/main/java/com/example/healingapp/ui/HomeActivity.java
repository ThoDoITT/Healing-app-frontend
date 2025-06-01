package com.example.healingapp.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.example.healingapp.R;
import com.example.healingapp.ui.body.body1Activity;
import com.example.healingapp.ui.common.CirleProgressView;
import com.example.healingapp.ui.workout.RunningActivity;
import com.example.healingapp.viewModel.RunningViewModel;
import com.example.healingapp.viewModel.SleepViewModel;

public class HomeActivity extends AppCompatActivity {

    private CirleProgressView pvSleep;
    private SleepViewModel sleepViewModel;
    private RunningViewModel runningViewModel;
    private CirleProgressView pvCalo;
    private CirleProgressView pvRun;
    private TextView tvTotalHome;
    private TextView tvDayInHome;
    private ConstraintLayout btnRunInHome;
    private ConstraintLayout btnBodyInHome;

    // LiveData để lưu trữ các giá trị phần trăm mới nhất
    private Float latestSleepPercentage = 0f;
    private Float latestRunPercentage = 0f;
    private Float latestCaloriePercentage = 0f;

    // MediatorLiveData cho phần trăm tổng
    private MediatorLiveData<Float> totalActivityPercentageLiveData = new MediatorLiveData<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_test2);

        init();
        addEvent();
        observeProgressView();
    }

    private void init() {
        sleepViewModel = new ViewModelProvider(this).get(SleepViewModel.class);
        runningViewModel = new ViewModelProvider(this).get(RunningViewModel.class);

        tvTotalHome = findViewById(R.id.tvTotalHome);

        // set style
        pvSleep = findViewById(R.id.pvSleep);
        pvSleep.setTextColor(Color.WHITE);
        pvSleep.setBackgroundColor(Color.parseColor("#0E3329"));
        pvSleep.setProgressColor(Color.parseColor("#00BA88"));
        tvDayInHome = findViewById(R.id.tvDayInHome);
        btnRunInHome = findViewById(R.id.btnRunInHome);
        btnBodyInHome = findViewById(R.id.btnBodyInHome);

        pvRun = findViewById(R.id.pvRun);
        pvRun.setTextColor(Color.WHITE);
        pvRun.setBackgroundColor(Color.parseColor("#0E3329"));
        pvRun.setProgressColor(Color.parseColor("#00BA88"));

        pvCalo = findViewById(R.id.pvCalo);
        pvCalo.setTextColor(Color.WHITE);
        pvCalo.setBackgroundColor(Color.parseColor("#0E3329"));
        pvCalo.setProgressColor(Color.parseColor("#00BA88"));

    }

    private void addEvent() {
        btnRunInHome.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, RunningActivity.class));
        });

        btnBodyInHome.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, body1Activity.class));
        });

    }

    private void observeProgressView() {
        // 1. Observe Sleep Percentage
        totalActivityPercentageLiveData.addSource(sleepViewModel.getTodaysSleepPercentage(), sleepPercentage -> {
            if (sleepPercentage != null) {
                pvSleep.setProgress(sleepPercentage.intValue());
                latestSleepPercentage = sleepPercentage;
            } else {
                pvSleep.setProgress(0);
                latestSleepPercentage = 0f;
            }
            updateTotalActivityPercentage();
        });

        // 2. Observe Running Duration Percentage
        totalActivityPercentageLiveData.addSource(runningViewModel.getTodaysRunningPercentage(), runPercentage -> {
            if (runPercentage != null) {
                pvRun.setProgress(runPercentage.intValue());
                latestRunPercentage = runPercentage;
                System.out.println("Phần trăm chạy hôm nay: " + String.format(java.util.Locale.getDefault(), "%.1f%%", runPercentage));
            } else {
                pvRun.setProgress(0);
                latestRunPercentage = 0f;
                System.out.println("Không có dữ liệu chạy hôm nay.");
            }
            updateTotalActivityPercentage();
        });

        // 3. Observe Calories Burned Percentage
        totalActivityPercentageLiveData.addSource(runningViewModel.getTodaysCaloriesBurnedPercentage(), caloriePercentage -> {
            if (caloriePercentage != null) {
                pvCalo.setProgress(caloriePercentage.intValue());
                latestCaloriePercentage = caloriePercentage;
            } else {
                pvCalo.setProgress(0);
                latestCaloriePercentage = 0f;
                System.out.println("Không có dữ liệu calo hôm nay.");
            }
            updateTotalActivityPercentage();
        });

        // 4. Observe Total Activity Percentage LiveData (được tính toán bởi MediatorLiveData)
        totalActivityPercentageLiveData.observe(this, totalPercentage -> {
            if (totalPercentage != null) {
               tvTotalHome.setText(String.format(java.util.Locale.getDefault(), "%.0f%%", totalPercentage));

            } else {
                tvTotalHome.setText("0%");
            }
        });
    }


private void updateTotalActivityPercentage() {
    // Tính trung bình cộng của 3 tỷ lệ phần trăm
    // Đảm bảo rằng các giá trị latest... đã được khởi tạo (ví dụ: 0f)
    float averagePercentage = (latestSleepPercentage + latestRunPercentage + latestCaloriePercentage) / 3.0f;
    totalActivityPercentageLiveData.setValue(averagePercentage);
}

}