package com.example.healingapp.utils;

import android.util.Log;

import com.example.healingapp.data.dao.DailySummaryRun;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessorHelper {

    private static final String TAG = "DataProcessorHelper";

    public List<DailySummaryRun> prepareWeeklyDisplayData(List<DailySummaryRun> rawData) {
        Map<Integer, Long> dailyDurationsMap = new HashMap<>();
        for (DailySummaryRun summary : rawData) {
            // SỬA ĐỔI QUAN TRỌNG: Gọi phương thức chuyển đổi mới
            // Nếu dayOfWeek từ SQLite là 0 (theo xác nhận của bạn là Thứ Hai),
            // thì nó sẽ được chuyển thành Calendar.MONDAY (giá trị 2)
            int calendarDayOfWeek = DailySummaryRun.convertSqliteDayOfWeekToCalendar(summary.getDayOfWeek());
            dailyDurationsMap.put(calendarDayOfWeek, summary.getTotalDuration());
        }

        List<DailySummaryRun> displayData = new ArrayList<>();


        int[] orderedCalendarDays = {
                Calendar.MONDAY,
                Calendar.TUESDAY,
                Calendar.WEDNESDAY,
                Calendar.THURSDAY,
                Calendar.FRIDAY,
                Calendar.SATURDAY,
                Calendar.SUNDAY
        };

        Log.d(TAG, "Starting to fill displayData with ordered days:");
        for (int calendarDayValue : orderedCalendarDays) {
            long totalDuration = dailyDurationsMap.getOrDefault(calendarDayValue, 0L);
            displayData.add(new DailySummaryRun(calendarDayValue, totalDuration));
            Log.d(TAG, "Added day: " + calendarDayValue + " (as " + new DailySummaryRun(calendarDayValue, 0).getDayOfWeekName() + "), Duration: " + totalDuration);
        }
        Log.d(TAG, "Finished filling displayData. Size: " + displayData.size());


        Log.d(TAG, "Final displayData (after all processing - should be ordered correctly):");
        for (DailySummaryRun summary : displayData) {
            Log.d(TAG, "Day: " + summary.getDayOfWeekName() + " (Calendar value: " + summary.getDayOfWeek() + "), Total Duration: " + summary.getTotalDuration());
        }

        return displayData;
    }
}
