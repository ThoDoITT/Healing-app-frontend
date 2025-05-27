package com.example.healingapp.data.models.workout;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "LocationPoint",
        foreignKeys = @ForeignKey(entity = RunningSession.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE))

public class LocationPoint {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int sessionId;
    public double lat;
    public double lng;
    public long timestamp;
}
