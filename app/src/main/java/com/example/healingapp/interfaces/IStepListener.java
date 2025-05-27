package com.example.healingapp.interfaces;

public interface IStepListener {
    /**
     * Được gọi khi có dữ liệu bước chân mới.
     * @param stepsSinceLastStart Số bước được đếm kể từ khi StepCounterHelper.startListening() được gọi lần cuối.
     */
    void onStepsCounted(int stepsSinceLastStart);

    /**
     * Được gọi nếu cảm biến đếm bước không khả dụng trên thiết bị.
     */
    void onSensorNotAvailable();
}
