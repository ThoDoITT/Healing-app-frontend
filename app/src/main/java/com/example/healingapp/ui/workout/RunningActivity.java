package com.example.healingapp.ui.workout;

import static com.example.healingapp.common.Consts.EXTRA_DATA_ID;
import static com.example.healingapp.common.Consts.EXTRA_DATA_PAYLOAD;
import static com.example.healingapp.common.Consts.EXTRA_TARGET_ACTIVITY_CLASS_NAME;
import static com.example.healingapp.common.Consts.EXTRA_TASK_TYPE;
import static com.example.healingapp.common.Consts.MAP_ZOOM_DEFAULT;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat; // Thêm import này

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.healingapp.R;
import com.example.healingapp.common.TaskType;
import com.example.healingapp.data.models.LocationPoint;
import com.example.healingapp.interfaces.IStepListener;
import com.example.healingapp.ui.HomeActivity;
import com.example.healingapp.ui.common.LoadingActivity;
import com.example.healingapp.utils.LocationHelper;
import com.example.healingapp.utils.StepCounterHelper;
import com.example.healingapp.utils.TrackingUtils;



public class RunningActivity extends AppCompatActivity implements IStepListener {
    private MapView mapView;
    private Polyline runPath;
    private List<LocationPoint> pathPoints = new ArrayList<>();
    private long startTime;
    private long pauseTime = 0;
    private long totalPausedDuration = 0;
    private Handler timeHandler = new Handler();
    private Handler blinkHandler = new Handler();
    private TextView tvTime, tvDistance, tvPace, tvCalories, tvStepCount;

    private Button btnPauseResumeRun;
    private Button btnFinishRun;
    private Button btnStartRun;
    private ObjectAnimator blinkAnimator;

    private StepCounterHelper stepCounterHelper;
    private int totalAccumulatedSteps = 0;
    private int currentSegmentSteps = 0;
    private boolean stepSensorAvailable = true;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private boolean isTrackingActive = false;
    private boolean isPaused = false;
    private boolean isBlinking = false;
    private Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            if (isTrackingActive && startTime != 0) {
                long duration = SystemClock.elapsedRealtime() - startTime;
                tvTime.setText(TrackingUtils.formatDuration(duration));

                timeHandler.postDelayed(this, 1000);
            }
        }
    };

    private Runnable blinkRunnable = new Runnable() {
        @Override
        public void run() {
            if (isBlinking) {

                if (btnPauseResumeRun.getAlpha() == 1.0f) {
                    btnPauseResumeRun.setAlpha(0.5f);
                } else {
                    btnPauseResumeRun.setAlpha(1.0f);
                }

                blinkHandler.postDelayed(this, 500);
            }
        }
    };

    public enum RunState {
        INITIAL,
        RUNNING,
        PAUSED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_running);

        mapView = findViewById(R.id.mapRunning);
        tvTime = findViewById(R.id.tvTime);
        tvDistance = findViewById(R.id.tvDistance);
        tvPace = findViewById(R.id.tvPace);
        tvCalories = findViewById(R.id.txtCalories);
        tvStepCount = findViewById(R.id.txtStepCounter);

        btnPauseResumeRun = findViewById(R.id.btnPause);
        btnFinishRun = findViewById(R.id.btnFinish);
        btnStartRun = findViewById(R.id.btnStart);

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(MAP_ZOOM_DEFAULT);
        mapView.getController().setCenter(new GeoPoint(10.762622, 106.660172));

        runPath = new Polyline();
        runPath.setColor(0xFF0000FF);
        runPath.setWidth(8.0f);
        mapView.getOverlayManager().add(runPath);

        // Khởi tạo ObjectAnimator cho hiệu ứng nhấp nháy
        blinkAnimator = ObjectAnimator.ofFloat(btnPauseResumeRun, "alpha", 1.0f, 0.2f); // Chuyển từ mờ hoàn toàn đến hơi mờ
        blinkAnimator.setDuration(800); // Thời gian của một chu kỳ (ví dụ: 800ms cho mờ dần và sáng lên)
        blinkAnimator.setRepeatMode(ObjectAnimator.REVERSE); // Đảo ngược hiệu ứng khi kết thúc một chu kỳ
        blinkAnimator.setRepeatCount(ObjectAnimator.INFINITE); // Lặp lại vô hạn

        // Khởi tạo StepCounterHelper
        stepCounterHelper = new StepCounterHelper(this, this);
        if (!stepCounterHelper.isSensorAvailable()) {
            stepSensorAvailable = false;
            tvStepCount.setText("Bước: N/A");
            Toast.makeText(this, "Cảm biến đếm bước không khả dụng.", Toast.LENGTH_LONG).show();
        }


        // set event for btnstart
        btnStartRun.setOnClickListener(view -> {
            if (!isTrackingActive) {
                if (LocationHelper.hasLocationPermissions(this)) {
                    startTracking();
                } else {
                    requestMissingPermissions();

                    LocationHelper.requestPermissions(this);
                }
            } else {
                Toast.makeText(this, "Tracking is already active.", Toast.LENGTH_SHORT).show();
            }
        });

        btnPauseResumeRun.setOnClickListener(v -> {
            if (isTrackingActive) { // Chỉ xử lý khi đang chạy hoặc tạm dừng
                if (isPaused) {
                    resumeTracking();
                } else {
                    pauseTracking();
                }
            }
        });

        btnFinishRun.setOnClickListener(v -> {
            if (isTrackingActive) { // Chỉ xử lý khi đang chạy hoặc tạm dừng
                finishTracking();
            } else {
                Toast.makeText(this, "No active run to finish.", Toast.LENGTH_SHORT).show();
            }
        });

        updateButtonStates(RunState.INITIAL);

    }

    private boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
        }
        return true; // Quyền này không cần cho API < 29
    }

    private void requestMissingPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        if (!LocationHelper.hasLocationPermissions(this)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            } else {
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        if (!hasActivityRecognitionPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), LOCATION_PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (mapView != null) {
            mapView.onResume();
        }


        if (LocationHelper.hasLocationPermissions(this)) {

            if (!isTrackingActive) {
                Log.d("RunningActivity", "onResume: Permissions already granted. Starting tracking.");

            } else {

                Log.d("RunningActivity", "onResume: Tracking already active.");
                timeHandler.post(updateTimeRunnable);
            }
        } else {
            // Nếu chưa có quyền, yêu cầu quyền.
            requestMissingPermissions();
            Log.d("RunningActivity", "onResume: Permissions not granted. Requesting permissions.");
        }
        if (isTrackingActive && !isPaused) {
            timeHandler.post(updateTimeRunnable);
            if (stepSensorAvailable && stepCounterHelper != null && !stepCounterHelper.isListening()) {
                stepCounterHelper.startListening();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (LocationHelper.hasLocationPermissions(this)) {
                Log.d("RunningActivity", "Location permissions granted via onRequestPermissionsResult.");
                if (!isTrackingActive) { // Đảm bảo chỉ gọi startTracking một lần

                    Toast.makeText(this, "Đã cấp quyền vị trí. Bạn có thể nhấn Start.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Location permission denied. Cannot track your run.", Toast.LENGTH_LONG).show();
                Log.e("RunningActivity", "Location permissions denied.");

            }
        }
    }

    private void startTracking() {
        // Kiểm tra lại quyền là một thói quen tốt, mặc dù logic onResume() đã kiểm tra
        if (!LocationHelper.hasLocationPermissions(this)) {
            Toast.makeText(this, "Location permission not granted. Please enable it in settings.", Toast.LENGTH_LONG).show();
            Log.e("RunningActivity", "Attempted to start tracking without permissions.");
            return;
        }

        // Reset dữ liệu cũ
        pathPoints.clear();
        runPath.setPoints(new ArrayList<>());
        mapView.invalidate();
        tvDistance.setText("0.00 km");
        tvPace.setText("0:00 min/km");
        tvCalories.setText("0 kcal");
        tvStepCount.setText("0 bước"); // Reset hiển thị bước

        // Reset trạng thái đếm bước
        totalAccumulatedSteps = 0;
        currentSegmentSteps = 0;

        if (stepSensorAvailable && stepCounterHelper != null) {
            stepCounterHelper.startListening();
        }

        startTime = SystemClock.elapsedRealtime();
        totalPausedDuration = 0;
        isTrackingActive = true;
        isPaused = false;
        Log.d("RunningActivity", "Tracking started. startTime: " + startTime);
        timeHandler.post(updateTimeRunnable); // Bắt đầu cập nhật thời gian

        LocationHelper.requestLocationUpdates(this, new LocationHelper.LocationUpdateListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null && !isPaused) { // Chỉ xử lý khi không tạm dừng
                    LocationPoint point = new LocationPoint(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());

                    if (pathPoints.isEmpty() || TrackingUtils.isValidLocationUpdate(pathPoints.get(pathPoints.size() - 1), point)) {
                        pathPoints.add(point);
                        runPath.addPoint(new GeoPoint(point.lat, point.lng));

                        if (pathPoints.size() > 1) {
                            long currentDuration = SystemClock.elapsedRealtime() - startTime - totalPausedDuration;
                            float totalDistance = TrackingUtils.getTotalDistance(pathPoints);
                            float currentPace = TrackingUtils.calculatePace(currentDuration, totalDistance);

                            float currentSpeedKmh = 0f;
                            if (currentPace > 0) {
                                currentSpeedKmh = 60f / currentPace;
                            }


                            float userWeightKg = 65f;

                            float caloriesBurned = TrackingUtils.calculateCaloriesBurned(userWeightKg, currentSpeedKmh, currentDuration);

                            tvDistance.setText(String.format("%.2f km", totalDistance / 1000.0f));
                            tvPace.setText(TrackingUtils.formatPace(currentPace));
                            tvCalories.setText(String.format("%.0f kcal", caloriesBurned));
                        }

                        mapView.getController().animateTo(new GeoPoint(point.lat, point.lng));
                        mapView.invalidate();
                        Log.d("RunningActivity", "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.w("RunningActivity", "onLocationChanged: Invalid or duplicate location update ignored.");
                    }

                } else if (location == null) {
                    Log.w("RunningActivity", "onLocationChanged: Location is null.");
                }
            }

            @Override
            public void onLocationError(String errorMessage) {
                Toast.makeText(RunningActivity.this, "Location error: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("RunningActivity", "Location error: " + errorMessage);
            }
        });

        updateButtonStates(RunState.RUNNING);
    }

    private void pauseTracking() {
        if (isTrackingActive && !isPaused) {
            LocationHelper.stopLocationUpdates();
            timeHandler.removeCallbacks(updateTimeRunnable);
            pauseTime = SystemClock.elapsedRealtime();
            isPaused = true;

            if (stepSensorAvailable && stepCounterHelper != null) {
                stepCounterHelper.stopListening();
                totalAccumulatedSteps += currentSegmentSteps;
                currentSegmentSteps = 0;
            }

            Log.d("RunningActivity", "Đã tạm dừng theo dõi. Tổng bước tích lũy: " + totalAccumulatedSteps);

            Log.d("RunningActivity", "Tracking paused.");
            updateButtonStates(RunState.PAUSED); // Cập nhật trạng thái nút
            startBlinking();
        }
    }

    private void resumeTracking() {
        if (isTrackingActive && isPaused) {
            stopBlinking();
            totalPausedDuration += (SystemClock.elapsedRealtime() - pauseTime);
            timeHandler.post(updateTimeRunnable);
            LocationHelper.requestLocationUpdates(this, new LocationHelper.LocationUpdateListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (location != null && !isPaused) {
                        LocationPoint point = new LocationPoint(location.getLatitude(), location.getLongitude(), System.currentTimeMillis());

                        if (pathPoints.isEmpty() || TrackingUtils.isValidLocationUpdate(pathPoints.get(pathPoints.size() - 1), point)) {
                            pathPoints.add(point);
                            runPath.addPoint(new GeoPoint(point.lat, point.lng));

                            if (pathPoints.size() > 1) {
                                long currentDuration = SystemClock.elapsedRealtime() - startTime - totalPausedDuration;
                                float totalDistance = TrackingUtils.getTotalDistance(pathPoints);
                                float currentPace = TrackingUtils.calculatePace(currentDuration, totalDistance);

                                float currentSpeedKmh = 0f;
                                if (currentPace > 0) {
                                    currentSpeedKmh = 60f / currentPace;
                                }

                                float userWeightKg = 65f;
                                float caloriesBurned = TrackingUtils.calculateCaloriesBurned(userWeightKg, currentSpeedKmh, currentDuration);

                                tvDistance.setText(String.format("%.2f km", totalDistance / 1000.0f));
                                tvPace.setText(TrackingUtils.formatPace(currentPace));
                                tvCalories.setText(String.format("%.0f kcal", caloriesBurned));
                            }

                            mapView.getController().animateTo(new GeoPoint(point.lat, point.lng));
                            mapView.invalidate();
                            Log.d("RunningActivity", "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                        } else {
                            Log.w("RunningActivity", "onLocationChanged: Invalid or duplicate location update ignored.");
                        }

                    } else if (location == null) {
                        Log.w("RunningActivity", "onLocationChanged: Location is null.");
                    }
                }

                @Override
                public void onLocationError(String errorMessage) {
                    Toast.makeText(RunningActivity.this, "Location error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("RunningActivity", "Location error: " + errorMessage);
                }
            });

            if (stepSensorAvailable && stepCounterHelper != null) {
                if (hasActivityRecognitionPermission()) {
                    stepCounterHelper.startListening();

                    totalPausedDuration += (SystemClock.elapsedRealtime() - pauseTime);
                    timeHandler.post(updateTimeRunnable);
                    isPaused = false;
                    Log.d("RunningActivity", "Đã tiếp tục theo dõi (với quyền nhận dạng hoạt động).");
                    updateButtonStates(RunState.RUNNING);
                } else {
                    Log.w("RunningActivity", "Quyền Nhận dạng Hoạt động không được cấp khi resume. Yêu cầu quyền.");
                    Toast.makeText(this, "Cần quyền nhận dạng hoạt động để đếm bước.", Toast.LENGTH_LONG).show();
                    requestMissingPermissions();

                }
            } else {
                // Cảm biến không khả dụng hoặc helper là null, vẫn resume các phần khác của tracking
                totalPausedDuration += (SystemClock.elapsedRealtime() - pauseTime);
                timeHandler.post(updateTimeRunnable);
                isPaused = false;
                Log.d("RunningActivity", "Đã tiếp tục theo dõi (cảm biến bước không khả dụng/helper null).");
                updateButtonStates(RunState.RUNNING);
            }
        }
    }


    // Bạn có thể cần một hàm stopTracking() nếu có nút dừng
    private void stopTracking() {

        if (stepSensorAvailable && stepCounterHelper != null && stepCounterHelper.isListening()) {
            stepCounterHelper.stopListening();
        }
        LocationHelper.stopLocationUpdates();
        timeHandler.removeCallbacks(updateTimeRunnable);
        isTrackingActive = false; // Đảm bảo cờ này được đặt
        // Không reset startTime ở đây nếu finishTracking chịu trách nhiệm
        Log.d("RunningActivity", "Đã dừng các dịch vụ theo dõi cơ bản.");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Configuration.getInstance().save(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (mapView != null) {
            mapView.onPause();
        }

        timeHandler.removeCallbacks(updateTimeRunnable);
        stopBlinking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTracking();
        if (mapView != null) {
            mapView.onDetach();
        }
        timeHandler.removeCallbacksAndMessages(null);
        blinkHandler.removeCallbacksAndMessages(null);
        Log.d("RunningActivity", "RunningActivity destroyed.");
    }

    private void finishTracking() {
        if (isTrackingActive) {
            stopBlinking();
            LocationHelper.stopLocationUpdates();
            timeHandler.removeCallbacks(updateTimeRunnable);

            int finalTotalSteps = totalAccumulatedSteps + currentSegmentSteps;
            if (stepSensorAvailable && stepCounterHelper != null && stepCounterHelper.isListening()) {
                stepCounterHelper.stopListening();

            }
            Log.d("RunningActivity", "Kết thúc theo dõi. Tổng số bước cuối cùng: " + finalTotalSteps);

            // set data save
            long finalDurationMillis = SystemClock.elapsedRealtime() - startTime - totalPausedDuration;
            String finalDurationFormatted = TrackingUtils.formatDuration(finalDurationMillis);
            float finalDistanceMeters = TrackingUtils.getTotalDistance(pathPoints);
            String finalDistanceKmFormatted = String.format(Locale.US, "%.2f", finalDistanceMeters / 1000.0f);
            float finalPaceMinPerKm = TrackingUtils.calculatePace(finalDurationMillis, finalDistanceMeters);
            String finalPaceFormatted = TrackingUtils.formatPace(finalPaceMinPerKm);
            float userWeightKg = 65f; // TODO
            float finalSpeedKmh = (finalPaceMinPerKm > 0) ? (60f / finalPaceMinPerKm) : 0f;
            float finalCalories = TrackingUtils.calculateCaloriesBurned(userWeightKg, finalSpeedKmh, finalDurationMillis);
            String finalCaloriesFormatted = String.format(Locale.US, "%.0f", finalCalories);
            long actualRunStartTime = startTime;
            long actualRunEndTime = actualRunStartTime + finalDurationMillis;

            // Tạo JSON payload
            JSONObject runDataJson = new JSONObject();
            try {
                runDataJson.put("startTime", actualRunStartTime);
                runDataJson.put("endTime", actualRunEndTime);
                runDataJson.put("distanceMeters", finalDistanceMeters);
                runDataJson.put("paceMinPerKm", finalPaceMinPerKm);
                runDataJson.put("durationMillis", finalDurationMillis);
                runDataJson.put("calories", Math.round(finalCalories));
                runDataJson.put("steps", finalTotalSteps);
            } catch (JSONException e) {
                Log.e("RunningActivity", "Lỗi tạo JSON cho dữ liệu chạy: " + e.getMessage());
            }

            String runDataPayload = runDataJson.toString();

            Log.d("RunningActivity", runDataPayload);


            Intent intent = new Intent(RunningActivity.this, LoadingActivity.class);
            intent.putExtra(EXTRA_TASK_TYPE, TaskType.SAVE_RUN_DATA);
            intent.putExtra(EXTRA_DATA_PAYLOAD, runDataPayload);

            intent.putExtra(EXTRA_TARGET_ACTIVITY_CLASS_NAME, DetailRunningActivity.class.getName());


            // Reset trạng thái và UI
            isTrackingActive = false;
            isPaused = false;
            startTime = 0;
            pauseTime = 0;
            totalPausedDuration = 0;
            totalAccumulatedSteps = 0;
            currentSegmentSteps = 0;

            pathPoints.clear();
            runPath.setPoints(new ArrayList<>());
            mapView.invalidate();

            tvTime.setText("00:00:00");
            tvDistance.setText("0.00 km");
            tvPace.setText("0:00 min/km");
            tvCalories.setText("0 kcal");
            tvStepCount.setText("0 bước");

            Log.d("RunningActivity", "Đã kết thúc theo dõi. Dữ liệu đã được xóa.");
            updateButtonStates(RunState.INITIAL);

            startActivity(intent);
            finish();
        }

    }


    private void updateButtonStates(RunState state) {
        switch (state) {
            case INITIAL:
                btnStartRun.setVisibility(View.VISIBLE);
                btnStartRun.setEnabled(true);
                btnPauseResumeRun.setVisibility(View.GONE);
                btnFinishRun.setVisibility(View.GONE);
                break;
            case RUNNING:
                btnStartRun.setVisibility(View.GONE);
                btnPauseResumeRun.setVisibility(View.VISIBLE);
                btnPauseResumeRun.setEnabled(true);
                btnPauseResumeRun.setText("Pause");
                btnFinishRun.setVisibility(View.VISIBLE);
                btnFinishRun.setEnabled(true);
                break;
            case PAUSED:
                btnStartRun.setVisibility(View.GONE);
                btnPauseResumeRun.setVisibility(View.VISIBLE);
                btnPauseResumeRun.setEnabled(true);
                btnPauseResumeRun.setText("Resume");
                btnFinishRun.setVisibility(View.VISIBLE);
                btnFinishRun.setEnabled(true);
                break;
        }
    }

    private void startBlinking() {
        if (blinkAnimator != null && !blinkAnimator.isStarted()) { // Kiểm tra để tránh khởi động nhiều lần
            blinkAnimator.start();
            Log.d("RunningActivity", "Blinking animation started.");
        }
    }

    private void stopBlinking() {
        if (blinkAnimator != null && blinkAnimator.isStarted()) {
            blinkAnimator.cancel(); // Dừng animation
            btnPauseResumeRun.setAlpha(1.0f); // Đảm bảo nút trở lại bình thường khi dừng nhấp nháy
            Log.d("RunningActivity", "Blinking animation stopped.");
        }
    }

    // --- Implement IStepListener ---
    @Override
    public void onStepsCounted(final int stepsSinceLastStart) {
        currentSegmentSteps = stepsSinceLastStart;
        final int displayedSteps = totalAccumulatedSteps + currentSegmentSteps;
        runOnUiThread(() -> {
            if (tvStepCount != null) {
                // Chỉ cập nhật UI nếu tracking đang thực sự hoạt động và không tạm dừng
                // Hoặc nếu đang tạm dừng, hiển thị tổng số bước đã tích lũy
                if (isTrackingActive) {
                    tvStepCount.setText(displayedSteps + " bước");
                }
            }
            // Log.d("RunningActivity", "onStepsCounted: " + stepsSinceLastStart + ", Tổng hiển thị: " + displayedSteps);
        });
    }

    @Override
    public void onSensorNotAvailable() {
        stepSensorAvailable = false;
        runOnUiThread(() -> {
            if (tvStepCount != null) {
                tvStepCount.setText("Bước: N/A");
            }
            Toast.makeText(RunningActivity.this, "Cảm biến đếm bước không khả dụng trên thiết bị này.", Toast.LENGTH_LONG).show();
        });
    }
}