package com.example.healingapp.data.models.workout;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.healingapp.data.dao.RunningSessionDao;

import lombok.Data;

@Entity(tableName = "RunningSession")

public class RunningSession {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long startTime;
    public long endTime;
    public float distance;
    public float pace;
    public long duration;
    public long calories;
    public long steps;
    public long timestamp;

    public RunningSession() {}
    public RunningSession(long startTime, long endTime, float distance, float pace, long duration, long calories, long steps, long timestamp) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.distance = distance;
        this.pace = pace;
        this.duration = duration;
        this.calories = calories;
        this.steps = steps;
        this.timestamp = timestamp;
    }

    public int getId() { return id;}
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public float getDistance() { return distance; }
    public float getPace() { return pace; }
    public long getDuration() { return duration; }
    public long getCalories() { return calories; }
    public long getSteps() { return steps; }

    public long getTimestamp() {
        return timestamp;
    }


}
