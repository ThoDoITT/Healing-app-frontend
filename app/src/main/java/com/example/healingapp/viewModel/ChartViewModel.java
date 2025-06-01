package com.example.healingapp.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.dao.DailySummaryRun;
import com.example.healingapp.data.dao.RunningSessionDao;
import com.example.healingapp.data.repository.RunningRepository;
import com.example.healingapp.utils.DataProcessorHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChartViewModel extends AndroidViewModel {
    private RunningRepository dataRepository;
    private DataProcessorHelper dataProcessor;
    private final MutableLiveData<List<DailySummaryRun>> _weeklySummaries = new MutableLiveData<>();
    public LiveData<List<DailySummaryRun>> weeklySummaries = _weeklySummaries;

    private ExecutorService executorService;

    // Constructor của AndroidViewModel nhận Application
    public ChartViewModel(Application application) {
        super(application);
        this.dataRepository = new RunningRepository(application);
        this.dataProcessor = new DataProcessorHelper();
        this.executorService = Executors.newSingleThreadExecutor();

        loadWeeklyRunData(); // Tải dữ liệu ban đầu
    }

    public void loadWeeklyRunData() {
        executorService.execute(() -> {
            List<DailySummaryRun> rawSummaries = dataRepository.getWeeklySummaryByDayOfWeek();
            List<DailySummaryRun> processedSummaries = dataProcessor.prepareWeeklyDisplayData(rawSummaries);
            _weeklySummaries.postValue(processedSummaries);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
