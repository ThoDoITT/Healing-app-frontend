package com.example.healingapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;
import androidx.core.content.ContextCompat;

import com.example.healingapp.interfaces.IStepListener;

public class StepCounterHelper implements SensorEventListener {
    private static final String TAG = "StepCounterHelper";
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private IStepListener listener;

    // Số bước của cảm biến khi bắt đầu lắng nghe cho đoạn hiện tại
    private long initialSensorSteps = -1;
    private boolean isListening = false;

    public StepCounterHelper(Context context, IStepListener listener) {
        this.listener = listener;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (this.sensorManager != null) {
            // Sử dụng TYPE_STEP_COUNTER để nhận tổng số bước kể từ lần khởi động cuối cùng
            // mà cảm biến này được kích hoạt.
            this.stepSensor = this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    /**
     * Bắt đầu lắng nghe sự kiện từ cảm biến đếm bước.
     * Mỗi lần gọi hàm này sẽ bắt đầu một "phân đoạn" đếm mới.
     */
    public void startListening() {
        if (!isSensorAvailable()) {
            Log.w(TAG, "Cảm biến đếm bước không khả dụng trên thiết bị này.");
            if (listener != null) {
                listener.onSensorNotAvailable();
            }
            return;
        }

        if (!isListening) {
            // Đặt lại mốc ban đầu cho phân đoạn lắng nghe mới
            initialSensorSteps = -1; // Sẽ được đặt bởi sự kiện cảm biến đầu tiên
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            isListening = true;
            Log.d(TAG, "Bắt đầu lắng nghe cảm biến bước cho một phân đoạn mới.");
        }
    }

    /**
     * Dừng lắng nghe sự kiện từ cảm biến đếm bước.
     */
    public void stopListening() {
        if (isListening && sensorManager != null) {
            sensorManager.unregisterListener(this);
            isListening = false;
            Log.d(TAG, "Đã dừng lắng nghe cảm biến bước.");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            long currentTotalSensorSteps = (long) event.values[0];

            if (initialSensorSteps == -1) {
                // Sự kiện đầu tiên cho phân đoạn lắng nghe này. Đặt mốc ban đầu cho phân đoạn này.
                initialSensorSteps = currentTotalSensorSteps;
                Log.d(TAG, "Mốc bước ban đầu của cảm biến cho phân đoạn: " + initialSensorSteps);
                if (listener != null) {
                    // Báo cáo 0 bước cho phân đoạn mới này ban đầu
                    listener.onStepsCounted(0);
                }
                return;
            }

            // Số bước đã thực hiện trong phân đoạn này
            long stepsThisSegment = currentTotalSensorSteps - initialSensorSteps;

            if (stepsThisSegment < 0) {
                // Điều này có thể xảy ra nếu cảm biến bị reset (ví dụ: khởi động lại thiết bị)
                // hoặc nếu initialSensorSteps giữ một giá trị cũ không hợp lệ.
                // Đặt lại mốc cho phân đoạn hiện tại.
                Log.w(TAG, "Số bước của cảm biến (" + currentTotalSensorSteps +
                        ") nhỏ hơn mốc của phân đoạn (" + initialSensorSteps + "). Đặt lại mốc phân đoạn.");
                initialSensorSteps = currentTotalSensorSteps;
                stepsThisSegment = 0;
            }

            if (listener != null) {
                listener.onStepsCounted((int) stepsThisSegment);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không quan trọng đối với bộ đếm bước
    }

    /**
     * Kiểm tra xem cảm biến đếm bước có khả dụng không.
     * @return true nếu có, false nếu không.
     */
    public boolean isSensorAvailable() {
        return stepSensor != null;
    }

    /**
     * Kiểm tra xem helper có đang lắng nghe không.
     * @return true nếu đang lắng nghe, false nếu không.
     */
    public boolean isListening() {
        return isListening;
    }
}