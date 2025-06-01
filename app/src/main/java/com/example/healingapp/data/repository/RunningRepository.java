package com.example.healingapp.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.dao.DailySummaryRun;
import com.example.healingapp.data.dao.RunningSessionDao;
import com.example.healingapp.data.models.workout.RunningSession;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class RunningRepository {
    private RunningSessionDao runningSessionDao;
    private LiveData<List<RunningSession>> allRuns;
    private static final long THIRTY_MINUTES_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final long TARGET_DAILY_CALORIES_BURNED = 1600L;

    public RunningRepository(Application application) {
        // Thay thế AppDatabase.getDatabase(application) bằng cách khởi tạo database của bạn
        // Ví dụ: AppDatabase db = AppDatabase.getDatabase(application);
        // runningSessionDao = db.runningSessionDao();
        // allRuns = runningSessionDao.getAllRuns();

        // Giả lập khởi tạo DAO nếu chưa có AppDatabase hoàn chỉnh
        // Trong dự án thực tế, bạn sẽ lấy DAO từ Room Database instance
        // Để code này có thể chạy, bạn cần đảm bảo runningSessionDao được khởi tạo đúng cách
        // từ một Room Database.
        // Ví dụ:
        AppDatabase db = AppDatabase.getDatabase(application); // Đảm bảo bạn có class này
        runningSessionDao = db.runDao();
        allRuns = runningSessionDao.getAllRuns();
    }


    // Các phương thức DAO cơ bản
    public void insert(RunningSession run) {
        // AppDatabase.databaseWriteExecutor.execute(() -> { // Sử dụng executor của bạn
        // runningSessionDao.insertRun(run);
        // });
        // Giả lập nếu chưa có executor:
        new Thread(() -> runningSessionDao.insertRun(run)).start();
    }

    public LiveData<List<RunningSession>> getAllRuns() {
        return allRuns;
    }

    public LiveData<RunningSession> getRunById(int runId) {
        return runningSessionDao.getRunById(runId);
    }

    // 1. Lấy phần trăm thời gian chạy trong ngày (so với 30 phút)
    public LiveData<Float> getDailyRunningPercentage(Calendar day) {
        Calendar dayStart = (Calendar) day.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        long dayStartMillis = dayStart.getTimeInMillis();

        Calendar dayEnd = (Calendar) day.clone();
        dayEnd.set(Calendar.HOUR_OF_DAY, 23);
        dayEnd.set(Calendar.MINUTE, 59);
        dayEnd.set(Calendar.SECOND, 59);
        dayEnd.set(Calendar.MILLISECOND, 999);
        long dayEndMillis = dayEnd.getTimeInMillis();
        // Hoặc:
        // Calendar nextDayStart = (Calendar) dayStart.clone();
        // nextDayStart.add(Calendar.DAY_OF_YEAR, 1);
        // long dayEndMillis = nextDayStart.getTimeInMillis(); // dùng cho query với < dayEndMillis

        LiveData<Long> totalRunningDurationLiveData = runningSessionDao.getTotalRunningDurationForDay(dayStartMillis, dayEndMillis);

        return Transformations.map(totalRunningDurationLiveData, totalDuration -> {
            if (totalDuration == null || totalDuration <= 0) {
                return 0f;
            }
            float percentage = ((float) totalDuration / THIRTY_MINUTES_MILLIS) * 100f;
            // Bạn có thể muốn giới hạn ở 100% hoặc cho phép vượt qua 100% tùy theo logic
            return Math.min(percentage, 100f); // Ví dụ: giới hạn ở 100%
            // return percentage; // Nếu muốn hiển thị > 100%
        });
    }

    // 3. Lấy phần trăm Calo đã đốt trong ngày (so với TARGET_DAILY_CALORIES_BURNED)
    public LiveData<Float> getDailyCaloriesBurnedPercentage(Calendar day) {
        Calendar dayStart = (Calendar) day.clone();
        dayStart.set(Calendar.HOUR_OF_DAY, 0);
        dayStart.set(Calendar.MINUTE, 0);
        dayStart.set(Calendar.SECOND, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        long dayStartMillis = dayStart.getTimeInMillis();

        Calendar dayEnd = (Calendar) day.clone();
        dayEnd.set(Calendar.HOUR_OF_DAY, 23);
        dayEnd.set(Calendar.MINUTE, 59);
        dayEnd.set(Calendar.SECOND, 59);
        dayEnd.set(Calendar.MILLISECOND, 999);
        long dayEndMillis = dayEnd.getTimeInMillis();

        LiveData<Long> totalCaloriesBurnedLiveData = runningSessionDao.getTotalCaloriesBurnedForDay(dayStartMillis, dayEndMillis);

        return Transformations.map(totalCaloriesBurnedLiveData, totalCalories -> {
            if (totalCalories == null || totalCalories <= 0 || TARGET_DAILY_CALORIES_BURNED <= 0) {
                return 0f;
            }
            float percentage = ((float) totalCalories / TARGET_DAILY_CALORIES_BURNED) * 100f;
            // Giới hạn ở 100% hoặc cho phép vượt qua tùy logic của bạn
            return Math.min(percentage, 100f);
            // return percentage; // Nếu muốn hiển thị > 100%
        });
    }

    // 2. Lấy danh sách các buổi chạy trong tuần hiện tại (Thứ 2 - Chủ Nhật)
    public LiveData<List<RunningSession>> getCurrentWeekRunningSessions() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekStartMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Thứ Hai + 6 ngày = Chủ Nhật
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long weekEndMillis = calendar.getTimeInMillis();

        return runningSessionDao.getRunningSessionsForDateRange(weekStartMillis, weekEndMillis);
    }

    public List<DailySummaryRun> getWeeklySummaryByDayOfWeek() {
        // (Giữ nguyên logic tính startDateInMillis và endDateInMillis cho tuần hiện tại)
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());

        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startDateInMillis = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_YEAR, 6);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endDateInMillis = calendar.getTimeInMillis();

        // Gọi phương thức DAO mới
        return runningSessionDao.getTotalDurationPerDay(startDateInMillis, endDateInMillis);
    }


}
