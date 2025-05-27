package com.example.healingapp.utils;

import android.location.Location;
import android.util.Log;

import com.example.healingapp.data.models.LocationPoint;

import java.util.List;

public class TrackingUtils {
    public static float getTotalDistance(List<LocationPoint> points) {
        float distance = 0f;
        for (int i = 1; i < points.size(); i++) {
            Location loc1 = new Location("");
            loc1.setLatitude(points.get(i - 1).lat);
            loc1.setLongitude(points.get(i - 1).lng);

            Location loc2 = new Location("");
            loc2.setLatitude(points.get(i).lat);
            loc2.setLongitude(points.get(i).lng);

            distance += loc1.distanceTo(loc2);
        }
        return distance;
    }

    /**
     * Tính toán tốc độ trung bình (pace) theo phút trên mỗi kilomet (min/km).
     * @param durationMillis Tổng thời gian đã trôi qua (mili giây).
     * @param distanceMeters Tổng khoảng cách đã di chuyển (mét).
     * @return Tốc độ trung bình theo phút/km. Trả về 0 nếu khoảng cách hoặc thời gian là 0.
     */
    public static float calculatePace(long durationMillis, float distanceMeters) {
        if (distanceMeters <= 0 || durationMillis <= 0) { // Đảm bảo khoảng cách và thời gian đều dương
            return 0f;
        }

        // Bước 1: Chuyển đổi thời gian từ mili giây sang phút
        float totalMinutes = durationMillis / 1000.0f / 60.0f; // millis -> seconds -> minutes

        // Bước 2: Chuyển đổi khoảng cách từ mét sang kilomet
        float totalKilometers = distanceMeters / 1000.0f;

        // Bước 3: Tính pace (phút/km)
        return totalMinutes / totalKilometers;
    }

    public static String formatDuration(long millis) {
        long secs = millis / 1000;
        long mins = secs / 60;
        secs = secs % 60;
        return String.format("%02d:%02d", mins, secs);
    }

    public static String formatPace(float pace) {
        int mins = (int) pace;
        int secs = (int) ((pace - mins) * 60);
        return String.format("%d:%02d min/km", mins, secs);
    }

    // --- CÁC HÀM MỚI BỔ SUNG ---

    // Ngưỡng khoảng cách tối thiểu để coi là cập nhật vị trí mới (ví dụ: 1 mét)
    private static final float MIN_DISTANCE_CHANGE_THRESHOLD = 1.0f; // mét
    // Ngưỡng tốc độ tối đa hợp lý cho người chạy bộ (ví dụ: 10 m/s = 36 km/h)
    // Tốc độ của Bolt là khoảng 10.4 m/s, nên 10 m/s là một ngưỡng khá cao.
    private static final float MAX_SPEED_THRESHOLD_MPS = 10.0f; // mét/giây

    /**
     * Kiểm tra xem một cập nhật vị trí mới có hợp lệ hay không.
     * Giúp lọc bỏ các điểm GPS bị nhảy hoặc dữ liệu bất thường.
     * @param lastPoint Điểm vị trí cuối cùng đã biết.
     * @param newPoint Điểm vị trí mới được nhận.
     * @return true nếu điểm mới được coi là hợp lệ, false nếu không.
     */
    public static boolean isValidLocationUpdate(LocationPoint lastPoint, LocationPoint newPoint) {
        // Nếu chưa có điểm nào hoặc điểm mới là null, coi là hợp lệ để bắt đầu theo dõi.
        if (lastPoint == null || newPoint == null) {
            return true;
        }

        // Tạo đối tượng Location tạm thời để tính khoảng cách và thời gian
        Location lastLoc = new Location("");
        lastLoc.setLatitude(lastPoint.lat);
        lastLoc.setLongitude(lastPoint.lng);
        lastLoc.setTime(lastPoint.timestamp);

        Location newLoc = new Location("");
        newLoc.setLatitude(newPoint.lat);
        newLoc.setLongitude(newPoint.lng);
        newLoc.setTime(newPoint.timestamp);

        float distance = lastLoc.distanceTo(newLoc); // Khoảng cách giữa hai điểm (mét)
        long timeDiff = newPoint.timestamp - lastPoint.timestamp; // Chênh lệch thời gian (mili giây)

        // Bỏ qua nếu khoảng cách quá nhỏ (có thể là lỗi cảm biến hoặc người dùng đứng yên)
        if (distance < MIN_DISTANCE_CHANGE_THRESHOLD) {
            Log.d("TrackingUtils", "Location update ignored: Distance too small (" + distance + "m)");
            return false;
        }

        // Bỏ qua nếu thời gian chênh lệch quá ngắn hoặc âm (tránh chia cho 0 và lỗi timestamp)
        if (timeDiff <= 0) {
            Log.d("TrackingUtils", "Location update ignored: Invalid time difference (" + timeDiff + "ms)");
            return false;
        }

        // Tính tốc độ (mét/giây) và kiểm tra xem có quá nhanh một cách bất thường không
        float speed = distance / (timeDiff / 1000.0f); // đổi ms sang giây
        if (speed > MAX_SPEED_THRESHOLD_MPS) {
            Log.w("TrackingUtils", "Location update ignored: Suspiciously high speed (" + String.format("%.2f", speed) + " m/s)");
            return false;
        }

        return true;
    }

    /**
     * Ước tính lượng calo đốt cháy dựa trên tốc độ và trọng lượng cơ thể.
     * Sử dụng công thức MET (Metabolic Equivalent of Task).
     * @param weightKg Trọng lượng cơ thể của người chạy (kg).
     * @param speedKmh Tốc độ chạy hiện tại (km/h).
     * @param durationMillis Thời gian hoạt động (mili giây).
     * @return Lượng calo đốt cháy ước tính (kcal).
     */
    public static float calculateCaloriesBurned(float weightKg, float speedKmh, long durationMillis) {
        if (weightKg <= 0 || speedKmh <= 0 || durationMillis <= 0) {
            return 0f;
        }

        double metValue;
        // Ước tính giá trị MET dựa trên tốc độ (km/h)
        // Đây là các giá trị xấp xỉ, bạn có thể điều chỉnh hoặc dùng một bảng tra cứu chi tiết hơn.
        if (speedKmh < 6.0) { // Đi bộ nhanh
            metValue = 3.5;
        } else if (speedKmh < 8.0) { // Chạy chậm
            metValue = 8.0;
        } else if (speedKmh < 9.7) { // Chạy vừa (6 mph)
            metValue = 10.0;
        } else if (speedKmh < 11.3) { // Chạy khá nhanh (7 mph)
            metValue = 11.5;
        } else if (speedKmh < 12.9) { // Chạy nhanh (8 mph)
            metValue = 12.5;
        } else if (speedKmh < 14.5) { // Chạy rất nhanh (9 mph)
            metValue = 14.0;
        } else { // Chạy nước rút (10+ mph)
            metValue = 16.0;
        }

        // Công thức tính calo: METs * trọng lượng (kg) * thời gian (giờ)
        // Lưu ý: METs thường được định nghĩa là ml O2 / kg / phút.
        // Công thức quy đổi: (METs * trọng lượng (kg) * 3.5) / 200 * thời gian (phút)
        // 3.5 là hằng số để chuyển đổi từ ml O2/kg/phút sang kcal/kg/phút
        // 200 là hằng số để chuyển từ ml O2 sang kcal
        float durationMinutes = durationMillis / 60000f; // Chuyển mili giây sang phút

        float calories = (float) (metValue * weightKg * 3.5 / 200 * durationMinutes);
        return calories;
    }

    /**
     * Ước tính lượng calo đốt cháy dựa trên tổng quãng đường và trọng lượng cơ thể.
     * Công thức xấp xỉ: 1 kcal/kg/km
     * Đây là một ước tính đơn giản hơn, không phụ thuộc vào tốc độ.
     * @param weightKg Trọng lượng cơ thể của người chạy (kg).
     * @param totalDistanceMeters Tổng quãng đường chạy (mét).
     * @return Lượng calo đốt cháy ước tính (kcal).
     */
    public static float calculateCaloriesBurnedByDistance(float weightKg, float totalDistanceMeters) {
        if (weightKg <= 0 || totalDistanceMeters <= 0) {
            return 0f;
        }
        // Công thức xấp xỉ: khoảng 1 kcal cho mỗi kg trọng lượng cơ thể cho mỗi km chạy.
        // totalDistanceMeters / 1000f để chuyển từ mét sang kilomet
        return weightKg * (totalDistanceMeters / 1000f);
    }


}

