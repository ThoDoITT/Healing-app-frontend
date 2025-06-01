package com.example.healingapp.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log; // Thêm để debug

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;


public class LocationHelper {
    private static FusedLocationProviderClient client;
    private static LocationCallback callback;
    private static LocationUpdateListener sLocationUpdateListener; // Interface callback riêng
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "LocationHelper"; // Thẻ để debug

    // Interface callback để nhận cập nhật vị trí
    public interface LocationUpdateListener {
        void onLocationChanged(Location location);
        void onLocationError(String errorMessage);
    }

    // Hàm yêu cầu quyền truy cập vị trí
    public static void requestPermissions(Activity activity) {
        // Kiểm tra quyền ACCESS_FINE_LOCATION
        boolean fineLocationGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        // Kiểm tra quyền ACCESS_COARSE_LOCATION
        boolean coarseLocationGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!fineLocationGranted || !coarseLocationGranted) {
            // Yêu cầu cả hai quyền nếu chưa được cấp
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    // Hàm kiểm tra xem quyền đã được cấp hay chưa
    public static boolean hasLocationPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // Hàm yêu cầu cập nhật vị trí
    @SuppressLint("MissingPermission")
    public static void requestLocationUpdates(Context context, LocationUpdateListener listener) {
        sLocationUpdateListener = listener; // Gán listener
        client = LocationServices.getFusedLocationProviderClient(context);

        // **KIỂM TRA QUYỀN TRỰC TIẾP TRƯỚC KHI GỌI API NHẠY CẢM**
        if (!hasLocationPermissions(context)) {
            Log.e(TAG, "Location permissions not granted. Cannot request location updates.");
            if (sLocationUpdateListener != null) {
                sLocationUpdateListener.onLocationError("Location permissions not granted.");
            }
            // Không tiếp tục nếu không có quyền
            return;
        }

        // Tạo LocationRequest sử dụng LocationRequest.Builder (cách mới)
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
                .setMinUpdateIntervalMillis(2000) // Tần suất nhanh nhất
                .build();

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    Log.e(TAG, "LocationResult is null.");
                    if (sLocationUpdateListener != null) {
                        sLocationUpdateListener.onLocationError("Location result is null.");
                    }
                    return;
                }
                for (Location loc : locationResult.getLocations()) {
                    if (loc != null) {
                        if (sLocationUpdateListener != null) {
                            sLocationUpdateListener.onLocationChanged(loc);
                        }
                    }
                }
            }
        };


        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Location updates requested successfully.");
                    } else {
                        Log.e(TAG, "Failed to request location updates: " + task.getException().getMessage());
                        if (sLocationUpdateListener != null) {
                            sLocationUpdateListener.onLocationError("Failed to request location updates: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Hàm dừng cập nhật vị trí
    public static void stopLocationUpdates() {
        if (client != null && callback != null) {
            client.removeLocationUpdates(callback)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Location updates stopped successfully.");
                        } else {
                            Log.e(TAG, "Failed to stop location updates: " + task.getException().getMessage());
                        }
                    });
        }
        sLocationUpdateListener = null; // Xóa listener khi dừng
    }
}