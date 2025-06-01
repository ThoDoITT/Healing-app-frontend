package com.example.healingapp.data.dao;

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
}
