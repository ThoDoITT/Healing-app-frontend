package com.example.healingapp.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.healingapp.data.models.workout.RunningSession;
import com.example.healingapp.data.repository.RunningRepository;

import java.util.Calendar;
import java.util.List;

public class RunningViewModel extends AndroidViewModel {
    private RunningRepository repository;
    private LiveData<List<RunningSession>> allRuns;

    public RunningViewModel(Application application) {
        super(application);
        repository = new RunningRepository(application); // Repository được khởi tạo ở đây
        allRuns = repository.getAllRuns();
    }

    public LiveData<List<RunningSession>> getAllRuns() {
        return allRuns;
    }

    public void insert(RunningSession run) {
        repository.insert(run);
    }

    public LiveData<RunningSession> getRunById(int runId) {
        return repository.getRunById(runId);
    }

    // Lấy phần trăm thời gian chạy cho ngày hôm nay
    public LiveData<Float> getTodaysRunningPercentage() {
        return repository.getDailyRunningPercentage(Calendar.getInstance());
    }

    // Lấy phần trăm thời gian chạy cho một ngày cụ thể
    public LiveData<Float> getRunningPercentageForDay(Calendar day) {
        return repository.getDailyRunningPercentage(day);
    }

    // Lấy danh sách các buổi chạy của tuần hiện tại
    public LiveData<List<RunningSession>> getCurrentWeekRunningSessions() {
        return repository.getCurrentWeekRunningSessions();
    }
    // Lấy phần trăm calo đã đốt cho một ngày cụ thể
    // Lấy phần trăm calo đã đốt cho ngày hôm nay
    public LiveData<Float> getTodaysCaloriesBurnedPercentage() {
        return repository.getDailyCaloriesBurnedPercentage(Calendar.getInstance());
    }

    // Lấy phần trăm calo đã đốt cho một ngày cụ thể
    public LiveData<Float> getCaloriesBurnedPercentageForDay(Calendar day) {
        return repository.getDailyCaloriesBurnedPercentage(day);
    }
}
