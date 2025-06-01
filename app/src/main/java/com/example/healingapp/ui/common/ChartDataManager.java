package com.example.healingapp.ui.common;

import android.graphics.Color;

import com.example.healingapp.data.dao.DailySummaryRun;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartDataManager {
    private LineChart lineChart;

    public ChartDataManager(LineChart chart) {
        this.lineChart = chart;
    }

    public void displayWeeklyRunDuration(List<DailySummaryRun> weeklySummaries) {
        // 1. Chuyển đổi DailySummary sang List<Entry>
        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>(); // Dùng cho nhãn trục X

        // Sắp xếp lại dữ liệu theo thứ tự từ Thứ Hai đến Chủ Nhật nếu cần
        // (đảm bảo weeklySummaries đã được xử lý bởi DataProcessor và sắp xếp đúng thứ tự)

        for (int i = 0; i < weeklySummaries.size(); i++) {
            DailySummaryRun summary = weeklySummaries.get(i);

            // Giá trị x là chỉ số của ngày (0 cho Thứ Hai, 1 cho Thứ Ba, v.v.)
            // Giá trị y là tổng thời gian chạy (ví dụ: chia cho 60000 để chuyển từ ms sang phút)
            entries.add(new Entry(i, summary.getTotalDuration() / 60000f)); // Giả sử hiển thị theo phút

            // Lấy tên ngày cho nhãn trục X
            xLabels.add(summary.getDayOfWeekName());
        }

        // 2. Tạo LineDataSet từ các Entry
        LineDataSet dataSet = new LineDataSet(entries, "Tổng thời gian chạy (phút)");
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);
        legend.setTextColor(Color.WHITE);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER); // Bật đường cong Cubic

        dataSet.setColor(Color.parseColor("#42A5F5")); // Màu xanh dương
        dataSet.setLineWidth(2.5f); // Độ dày của đường

        dataSet.setCircleColor(Color.parseColor("#1976D2")); // Màu điểm tròn
        dataSet.setCircleRadius(5f); // Bán kính điểm tròn
        dataSet.setDrawCircleHole(false); // Không vẽ lỗ ở giữa điểm
        dataSet.setValueTextSize(10f); // Kích thước chữ hiển thị giá trị
        dataSet.setValueTextColor(Color.WHITE); // Màu chữ giá trị
        dataSet.setDrawValues(true); // Hiển thị giá trị trên các điểm

        // Tùy chọn: Tô màu vùng dưới đường
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#90CAF9")); // Màu nền nhạt hơn
        dataSet.setFillAlpha(100);

        // 3. Chuẩn bị LineData
        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);
        LineData lineData = new LineData(dataSets);

        // 4. Cấu hình trục X với nhãn tùy chỉnh
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));
        xAxis.setLabelCount(xLabels.size(), false); // Đảm bảo hiển thị đủ nhãn, false để không auto-scale
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f); // Đảm bảo các nhãn cách nhau 1 đơn vị

        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setTextColor(Color.WHITE);
        // 5. Gán dữ liệu vào Chart và làm mới
        lineChart.setData(lineData);
        lineChart.animateY(1500); // Thêm hiệu ứng animation khi load
        lineChart.invalidate(); // Bắt buộc phải gọi để chart vẽ lại
    }
}
