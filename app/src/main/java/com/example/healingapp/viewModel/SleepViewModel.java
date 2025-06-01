package com.example.healingapp.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healingapp.data.models.sleep.SleepSession;
import com.example.healingapp.data.repository.SleepRepository;

import java.util.Calendar;
import java.util.List;

public class SleepViewModel extends AndroidViewModel {
    private SleepRepository repository;
    private LiveData<List<SleepSession>> allSessions;

    public SleepViewModel(Application application) {
        super(application);
        repository = new SleepRepository(application);
        allSessions = repository.getAllSessionsLiveData();
    }

    public LiveData<List<SleepSession>> getAllSessions() {
        return allSessions;
    }

    public void insert(SleepSession session) {
        repository.insert(session);
    }

    public void update(SleepSession session) {
        repository.update(session);
    }

    // Lấy phần trăm ngủ cho ngày hôm nay
    public LiveData<Float> getTodaysSleepPercentage() {
        return repository.getDailySleepPercentage(Calendar.getInstance());
    }

    // Lấy phần trăm ngủ cho một ngày cụ thể
    public LiveData<Float> getSleepPercentageForDay(Calendar day) {
        return repository.getDailySleepPercentage(day);
    }

    // Lấy danh sách giấc ngủ của tuần hiện tại
    public LiveData<List<SleepSession>> getCurrentWeekSleepSessions() {
        return repository.getCurrentWeekSleepSessions();
    }
}
