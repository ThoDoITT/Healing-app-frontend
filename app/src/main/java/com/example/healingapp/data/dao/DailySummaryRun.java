package com.example.healingapp.data.dao;

import java.util.Calendar;

public class DailySummaryRun {
    public String date; // Định dạng YYYY-MM-DD
    public int dayOfWeek;
    public long totalDuration; // Tổng thời gian chạy trong ngày đó (tính bằng milliseconds)

    public DailySummaryRun( int dayOfWeek, long totalDuration) {
        this.dayOfWeek = dayOfWeek;
        this.totalDuration = totalDuration;
    }
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    // Getter methods (tùy chọn nhưng nên có)
    public String getDate() {
        return date;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public String getDayOfWeekName() {
        switch (dayOfWeek) {
            case java.util.Calendar.MONDAY:
                return "Thứ Hai";
            case java.util.Calendar.TUESDAY:
                return "Thứ Ba";
            case java.util.Calendar.WEDNESDAY:
                return "Thứ Tư";
            case java.util.Calendar.THURSDAY:
                return "Thứ Năm";
            case java.util.Calendar.FRIDAY:
                return "Thứ Sáu";
            case java.util.Calendar.SATURDAY:
                return "Thứ Bảy";
            case java.util.Calendar.SUNDAY:
                return "Chủ Nhật";
            default:
                return "";
        }
    }

    public static int convertSqliteDayOfWeekToCalendar(int sqliteDayOfWeek) {
        switch (sqliteDayOfWeek) {
            case 0:
                return Calendar.MONDAY;    // Nếu 0 là Thứ Hai
            case 1:
                return Calendar.TUESDAY;   // Nếu 1 là Thứ Ba
            case 2:
                return Calendar.WEDNESDAY; // Nếu 2 là Thứ Tư
            case 3:
                return Calendar.THURSDAY;  // Nếu 3 là Thứ Năm
            case 4:
                return Calendar.FRIDAY;    // Nếu 4 là Thứ Sáu
            case 5:
                return Calendar.SATURDAY;  // Nếu 5 là Thứ Bảy
            case 6:
                return Calendar.SUNDAY;    // Nếu 6 là Chủ Nhật
            default:
                return -1; // Giá trị không hợp lệ
        }
    }
}
