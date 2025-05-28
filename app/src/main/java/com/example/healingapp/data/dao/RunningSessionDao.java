package com.example.healingapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.healingapp.data.models.workout.RunningSession;

import java.util.List;

@Dao
public interface RunningSessionDao {
    @Insert
    long insertRun(RunningSession runData);

    @Query("SELECT * FROM RunningSession ORDER BY Timestamp DESC")
    LiveData<List<RunningSession>> getAllRuns(); // Sử dụng LiveData để tự động cập nhật UI

    @Query("SELECT * FROM RunningSession WHERE id = :runId")
    LiveData<RunningSession> getRunById(int runId);
}
