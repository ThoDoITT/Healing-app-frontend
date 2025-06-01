package com.example.healingapp.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.healingapp.data.models.sleep.SleepSession;

import java.util.List;

@Dao
public interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(SleepSession session);

    @Update
    void updateSession(SleepSession session);

    @Query("SELECT * FROM SleepSessions WHERE id = :sessionId")
    SleepSession getSessionById(int sessionId);

    @Query("SELECT * FROM SleepSessions WHERE endTimeMillis = 0 ORDER BY startTimeMillis DESC LIMIT 1")
    SleepSession getActiveSleepSession(); // Lấy phiên đang hoạt động (chưa có endTime)

    @Query("SELECT * FROM SleepSessions ORDER BY startTimeMillis DESC")
    LiveData<List<SleepSession>> getAllSessionsLiveData(); // Sử dụng LiveData để UI tự cập nhật

    @Query("SELECT * FROM SleepSessions ORDER BY startTimeMillis DESC")
    List<SleepSession> getAllSessionsList(); // Để lấy danh sách đồng bộ nếu cần

    /**
     * Lấy tổng thời gian ngủ (durationMillis) của các phiên đã hoàn thành trong một ngày cụ thể.
     * Một phiên được coi là hoàn thành nếu durationMillis > 0.
     * @param dayStartMillis Thời gian bắt đầu của ngày (00:00:00).
     * @param dayEndMillis Thời gian kết thúc của ngày (23:59:59.999) hoặc bắt đầu ngày tiếp theo.
     * @return LiveData chứa tổng thời gian ngủ bằng mili giây, hoặc null nếu không có.
     */
    @Query("SELECT SUM(durationMillis) FROM SleepSessions WHERE startTimeMillis >= :dayStartMillis AND startTimeMillis < :dayEndMillis AND durationMillis > 0")
    LiveData<Long> getTotalSleepDurationForDay(long dayStartMillis, long dayEndMillis);

    /**
     * Lấy danh sách các phiên ngủ trong một khoảng thời gian nhất định (ví dụ: một tuần).
     * @param rangeStartMillis Thời gian bắt đầu của khoảng.
     * @param rangeEndMillis Thời gian kết thúc của khoảng.
     * @return LiveData chứa danh sách các SleepSession.
     */
    @Query("SELECT * FROM SleepSessions WHERE startTimeMillis >= :rangeStartMillis AND startTimeMillis <= :rangeEndMillis AND durationMillis > 0 ORDER BY startTimeMillis ASC")
    LiveData<List<SleepSession>> getSleepSessionsForDateRange(long rangeStartMillis, long rangeEndMillis);

    @Query("DELETE FROM SleepSessions")
    void deleteAllSleepSessions();

}
