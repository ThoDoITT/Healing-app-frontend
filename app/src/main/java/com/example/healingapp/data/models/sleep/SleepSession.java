package com.example.healingapp.data.models.sleep;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Entity(tableName = "SleepSessions")
public class SleepSession {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public long creationDateMillis;
    public long startTimeMillis;
    public long endTimeMillis;
    public long durationMillis;

    public SleepSession() {} // Constructor trống cần thiết cho Room

    // Getters (Setters có thể không cần nếu bạn chỉ cập nhật qua DAO hoặc constructor)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public long getCreationDateMillis() { return creationDateMillis; } // Getter cho trường mới
    public void setCreationDateMillis(long creationDateMillis) { this.creationDateMillis = creationDateMillis; } // Setter

    public long getStartTimeMillis() { return startTimeMillis; }
    public void setStartTimeMillis(long startTimeMillis) { this.startTimeMillis = startTimeMillis; }

    public long getEndTimeMillis() { return endTimeMillis; }
    public void setEndTimeMillis(long endTimeMillis) { this.endTimeMillis = endTimeMillis; }

    public long getDurationMillis() { return durationMillis; }
    public void setDurationMillis(long durationMillis) { this.durationMillis = durationMillis; }

    // Phương thức định dạng (Helper methods)
    public String getFormattedCreationDate() {
        if (creationDateMillis == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date(creationDateMillis));
    }

    public String getFormattedStartTime() {
        if (startTimeMillis == 0) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date(startTimeMillis));
    }

    public String getFormattedEndTime() {
        if (endTimeMillis == 0) return "Đang ngủ...";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault());
        return sdf.format(new Date(endTimeMillis));
    }

    public String getFormattedDuration() {
        if (endTimeMillis == 0 || startTimeMillis == 0 || durationMillis <= 0) return "0 phút";
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d giờ %d phút", hours, minutes);
        } else {
            return String.format(Locale.getDefault(), "%d phút", minutes);
        }
    }
}
