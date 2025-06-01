package com.example.healingapp.ui.workout;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.viewModel.RunningViewModel;

public class ListWorkoutActivity extends AppCompatActivity {

    private RunningViewModel runningSessionViewModel;
    private ListWorkoutAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_workout); // Your main layout file for the activity

        RecyclerView recyclerView = findViewById(R.id.list_workout_running_app);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true); // For performance optimization

        adapter = new ListWorkoutAdapter();
        recyclerView.setAdapter(adapter);

        runningSessionViewModel = new ViewModelProvider(this).get(RunningViewModel.class);
        runningSessionViewModel.getAllRuns().observe(this, runningSessions -> {
            // Update the cached copy of the running sessions in the adapter.
            adapter.setRunningSessions(runningSessions);
        });


    }
}
