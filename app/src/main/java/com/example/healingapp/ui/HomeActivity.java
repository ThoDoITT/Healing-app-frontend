package com.example.healingapp.ui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.healingapp.R;
import com.example.healingapp.ui.common.CirleProgressView;
import com.example.healingapp.viewModel.SleepViewModel;

public class HomeActivity extends AppCompatActivity {

    private CirleProgressView pvSleep;
    private SleepViewModel sleepViewModel;
    private CirleProgressView pvExercise;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_test2);

        init();
        observeProgressView();
    }

    private void init() {
        sleepViewModel = new ViewModelProvider(this).get(SleepViewModel.class);
        // set style
        pvSleep = findViewById(R.id.pvSleep);
        pvSleep.setTextColor(Color.WHITE);
        pvSleep.setBackgroundColor(Color.parseColor("#0E3329"));
        pvSleep.setProgressColor(Color.parseColor("#00BA88"));

        // set style exercise
//        pvExercise = findViewById(R.id.pvExercise);
//        pvExercise.setTextColor(Color.WHITE);
//        pvExercise.setBackgroundColor(Color.parseColor("#0E3329"));
//        pvExercise.setProgressColor(Color.parseColor("#00BA88"));
    }

    private void observeProgressView() {
        sleepViewModel.getTodaysSleepPercentage().observe(this, percentage -> {
            if (percentage != null) {
                String formattedText = String.format(java.util.Locale.getDefault(), "%.1f%%", percentage);

                pvSleep.setProgress(percentage.intValue());
            } else {
                pvSleep.setProgress(0);
            }
        });
    }
}