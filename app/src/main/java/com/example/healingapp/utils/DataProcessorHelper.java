package com.example.healingapp.utils;

import com.example.healingapp.data.dao.DailySummaryRun;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataProcessorHelper {
    public List<DailySummaryRun> prepareWeeklyDisplayData(List<DailySummaryRun> rawData) {
        // Tạo một Map để dễ dàng truy cập tổng thời gian theo thứ
        Map<Integer, Long> dailyDurationsMap = new HashMap<>();
        for (DailySummaryRun summary : rawData) {
            // Chuyển đổi dayOfWeek từ SQLite (0=CN, 1=T2...) sang Calendar (1=CN, 2=T2...)
            int calendarDayOfWeek = (summary.getDayOfWeek() == 0) ? Calendar.SUNDAY : (summary.getDayOfWeek() + 1);
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
                Calendar.SUNDAY // Đảm bảo Chủ Nhật được thêm vào rõ ràng
        };

        for (int calendarDayValue : orderedCalendarDays) {
            long totalDuration = dailyDurationsMap.getOrDefault(calendarDayValue, 0L);
            displayData.add(new DailySummaryRun(calendarDayValue, totalDuration));
        }
        // Điền đầy đủ 7 ngày trong tuần, bắt đầu từ Thứ Hai
//        for (int i = 0; i < 7; i++) {
//            // Calendar.MONDAY là 2, Calendar.TUESDAY là 3, ..., Calendar.SUNDAY là 1
//            int currentDayCalendarValue = Calendar.MONDAY + i; // Lấy giá trị Calendar cho Thứ Hai, Thứ Ba...
//
//            long totalDuration = dailyDurationsMap.getOrDefault(currentDayCalendarValue, 0L);
//            displayData.add(new DailySummaryRun(currentDayCalendarValue, totalDuration));
//        }

        // Sắp xếp lại danh sách theo thứ tự mong muốn (ví dụ: Thứ Hai -> Chủ Nhật)
        Collections.sort(displayData, new Comparator<DailySummaryRun>() {
            @Override
            public int compare(DailySummaryRun s1, DailySummaryRun s2) {
                // Xử lý để Thứ Hai (Calendar.MONDAY) đứng đầu
                int day1 = (s1.getDayOfWeek() == Calendar.SUNDAY) ? 8 : s1.getDayOfWeek(); // Đẩy CN về cuối
                int day2 = (s2.getDayOfWeek() == Calendar.SUNDAY) ? 8 : s2.getDayOfWeek();

                return Integer.compare(day1, day2);
            }
        });

        return displayData;
    }
}
