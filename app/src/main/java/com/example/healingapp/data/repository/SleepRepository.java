package com.example.healingapp.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.dao.SleepDao;
import com.example.healingapp.data.models.sleep.SleepSession;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SleepRepository {
    private SleepDao sleepDao;
    private LiveData<List<SleepSession>> allSessionsLiveData;
    private static final long EIGHT_HOURS_MILLIS = TimeUnit.HOURS.toMillis(8);

    // Giả sử bạn có một lớp SleepDatabase extends RoomDatabase
    public SleepRepository(Application application) {
        // Thay thế SleepDatabase.getDatabase(application) bằng cách khởi tạo database của bạn
        AppDatabase db = AppDatabase.getDatabase(application);
        sleepDao = db.sleepDao();
        allSessionsLiveData = sleepDao.getAllSessionsLiveData();
    }

    // Các phương thức DAO cơ bản đã có
    public void insert(SleepSession session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            sleepDao.insertSession(session);
        });
    }

    public void update(SleepSession session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            sleepDao.updateSession(session);
        });
    }

    public LiveData<List<SleepSession>> getAllSessionsLiveData() {
        return allSessionsLiveData;
    }

    public SleepSession getActiveSleepSession() {
        // Cần chạy trên một thread khác nếu không phải LiveData
        // Ví dụ: return SleepDatabase.databaseWriteExecutor.submit(() -> sleepDao.getActiveSleepSession()).get();
        // Hoặc sửa DAO để trả về LiveData<SleepSession>
        // Để đơn giản, tạm thời gọi trực tiếp (không khuyến khích cho UI thread)
        // Tốt nhất là DAO trả về LiveData hoặc bạn dùng Executor.
        // Trong ví dụ này, giả sử bạn gọi nó từ một background thread hoặc ViewModel coroutine.
        return sleepDao.getActiveSleepSession(); // Cân nhắc sửa DAO trả về LiveData
    }


    // 1. Lấy phần trăm ngủ trong ngày (so với 8 tiếng)
    public LiveData<Float> getDailySleepPercentage(Calendar day) {
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
        // Hoặc an toàn hơn:
        // Calendar nextDayStart = (Calendar) dayStart.clone();
        // nextDayStart.add(Calendar.DAY_OF_YEAR, 1);
        // long dayEndMillis = nextDayStart.getTimeInMillis(); // Dùng cho query < dayEndMillis


        LiveData<Long> totalSleepDurationLiveData = sleepDao.getTotalSleepDurationForDay(dayStartMillis, dayEndMillis);

        return Transformations.map(totalSleepDurationLiveData, totalDuration -> {
            if (totalDuration == null || totalDuration <= 0) {
                return 0f;
            }
            float percentage = ((float) totalDuration / EIGHT_HOURS_MILLIS) * 100f;
            return Math.min(percentage, 100f); // Giới hạn tối đa là 100%
        });
    }

    // 2. Lấy danh sách giấc ngủ theo tuần hiện tại (Thứ 2 - Chủ Nhật)
    public LiveData<List<SleepSession>> getCurrentWeekSleepSessions() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault()); // Sử dụng TimeZone của thiết bị

        // Đặt ngày là Thứ Hai của tuần hiện tại
        calendar.setFirstDayOfWeek(Calendar.MONDAY); // Đảm bảo tuần bắt đầu từ Thứ Hai
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long weekStartMillis = calendar.getTimeInMillis();

        // Đặt ngày là Chủ Nhật của tuần hiện tại
        calendar.add(Calendar.DAY_OF_WEEK, 6); // Thứ Hai + 6 ngày = Chủ Nhật
        // Hoặc calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); rồi +7 ngày nếu FirstDayOfWeek là Sunday
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long weekEndMillis = calendar.getTimeInMillis();

        return sleepDao.getSleepSessionsForDateRange(weekStartMillis, weekEndMillis);
    }
}
