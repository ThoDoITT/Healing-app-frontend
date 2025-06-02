package com.example.healingapp.interfaces;

public interface IStepListener {

    void onStepsCounted(int stepsSinceLastStart);


    void onSensorNotAvailable();
}
