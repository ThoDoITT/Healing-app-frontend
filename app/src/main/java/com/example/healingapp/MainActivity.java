package com.example.healingapp;

import android.os.Bundle;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.data.models.GridArrayList;
import com.example.healingapp.ui.common.CirleProgressView;
import com.example.healingapp.ui.workout.SelectWorkoutAdapter;

import org.osmdroid.config.Configuration;

public class MainActivity extends AppCompatActivity {

    private CirleProgressView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_workout);

        EdgeToEdge.enable(this);

        RecyclerView recyclerView = findViewById(R.id.recycle_view_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        SelectWorkoutAdapter adapter = new SelectWorkoutAdapter(this, new GridArrayList().dataList());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }
}