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

    /**
     * Lấy tổng thời gian chạy (duration) của các buổi chạy đã hoàn thành trong một ngày cụ thể.
     * Buổi chạy được coi là hoàn thành nếu duration > 0.
     * Sử dụng 'startTime' để lọc theo ngày.
     * @param dayStartMillis Thời gian bắt đầu của ngày (00:00:00).
     * @param dayEndMillis Thời gian kết thúc của ngày (23:59:59.999) hoặc bắt đầu ngày tiếp theo.
     * @return LiveData chứa tổng thời gian chạy bằng mili giây, hoặc null nếu không có.
     */
    @Query("SELECT SUM(duration) FROM RunningSession WHERE startTime >= :dayStartMillis AND startTime < :dayEndMillis AND duration > 0")
    LiveData<Long> getTotalRunningDurationForDay(long dayStartMillis, long dayEndMillis);

    /**
     * Lấy danh sách các buổi chạy trong một khoảng thời gian nhất định (ví dụ: một tuần).
     * Sử dụng 'startTime' để lọc và sắp xếp.
     * @param rangeStartMillis Thời gian bắt đầu của khoảng.
     * @param rangeEndMillis Thời gian kết thúc của khoảng.
     * @return LiveData chứa danh sách các RunningSession.
     */
    @Query("SELECT * FROM RunningSession WHERE startTime >= :rangeStartMillis AND startTime <= :rangeEndMillis AND duration > 0 ORDER BY startTime ASC")
    LiveData<List<RunningSession>> getRunningSessionsForDateRange(long rangeStartMillis, long rangeEndMillis);

    @Query("SELECT SUM(calories) FROM RunningSession WHERE startTime >= :dayStartMillis AND startTime < :dayEndMillis AND calories > 0")
    LiveData<Long> getTotalCaloriesBurnedForDay(long dayStartMillis, long dayEndMillis);

    @Query("SELECT STRFTIME('%w', datetime(timestamp / 1000, 'unixepoch')) AS dayOfWeek," +
            " SUM(duration) AS totalDuration " +
            "FROM RunningSession " +
            "WHERE timestamp BETWEEN :startDateInMillis AND :endDateInMillis " +
            "GROUP BY dayOfWeek " + // Nhóm theo thứ trong tuần
            "ORDER BY dayOfWeek ASC") // Sắp xếp theo thứ trong tuần
    List<DailySummaryRun> getTotalDurationPerDay(long startDateInMillis, long endDateInMillis);

    @Query("DELETE FROM RunningSession")
    void deleteAllRunningSessions();
}
