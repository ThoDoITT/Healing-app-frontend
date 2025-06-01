package com.example.healingapp.ui.workout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.healingapp.R;
import com.example.healingapp.data.models.GridArrayList;
import com.example.healingapp.ui.common.CirleProgressView;

public class SelectWorkoutActivity extends AppCompatActivity {

    private Button btnShowHistoryRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_select_workout);

        init();
    }

    private void init () {

        btnShowHistoryRun = findViewById(R.id.btnShowHistoryRun);
        btnShowHistoryRun.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListWorkoutActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.recycle_view_list);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        SelectWorkoutAdapter adapter = new SelectWorkoutAdapter(this, new GridArrayList().dataList());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }
}