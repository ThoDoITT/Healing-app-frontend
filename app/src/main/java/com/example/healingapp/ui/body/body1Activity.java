package com.example.healingapp.ui.body;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.healingapp.R;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.dao.RunningSessionDao;
import com.example.healingapp.network.ApiClient;
import com.example.healingapp.network.ApiResponseListener;
import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.UserProfileData;
import com.example.healingapp.network.models.response.GetUserProfileResponse;
import com.example.healingapp.ui.common.ChartDataManager;
import com.example.healingapp.utils.DataProcessorHelper;
import com.example.healingapp.utils.SessionManager;
import com.example.healingapp.viewModel.ChartViewModel;
import com.github.mikephil.charting.charts.LineChart;


public class body1Activity extends AppCompatActivity {

    private TextView tvWeight;
    private TextView tvHeight;
    LineChart lineChart;

    private TextView tvCaloBody1;
    private TextView tvTimeSleep;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    private ChartViewModel chartViewModel;
    private ChartDataManager chartDataManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_body1);

        init();

        chartViewModel.weeklySummaries.observe(this, dailySummaries -> {
            chartDataManager.displayWeeklyRunDuration(dailySummaries);
        });

        loadUserProfile();

    }

    private void init() {

        tvWeight = findViewById(R.id.tvWeight);
        tvHeight = findViewById(R.id.tvHeight);
        tvCaloBody1 = findViewById(R.id.tvCaloBody1);
        tvTimeSleep = findViewById(R.id.tvTimeSleep);
        lineChart = findViewById(R.id.lcTimeRunOfWeek);
        chartViewModel = new ViewModelProvider(this).get(ChartViewModel.class);
        // Khởi tạo ChartDataManager
        chartDataManager = new ChartDataManager(lineChart);
        setupChartBasicConfig();

        apiClient = new ApiClient();
        sessionManager = new SessionManager(getApplicationContext());

    }

    private void setupChartBasicConfig() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
    }

    private void loadUserProfile() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            // TODO: Optionally navigate to LoginActivity
            // Intent intent = new Intent(this, SigninActivity.class);
            // startActivity(intent);
            // finish();
            return;
        }

        String authToken = sessionManager.getAuthToken();
        String userId = sessionManager.getUserId(); // API endpoint uses user ID in path

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

//        progressDialog.show();

        apiClient.getUserProfile(authToken, userId, new ApiResponseListener<GetUserProfileResponse, ErrorResponse>() {
            @Override
            public void onSuccess(GetUserProfileResponse response) {
//                progressDialog.dismiss();
                UserProfileData userData = response.getData();

                if (userData != null) {
                    Log.d(TAG, "Lấy thông tin người dùng thành công: " + userData.getFullName());

                    // Cập nhật TextViews
                    tvWeight.setText(String.valueOf(userData.getWeight()) + " kg"); // Thêm đơn vị
                    tvHeight.setText(String.valueOf(userData.getHeight()) + " cm"); // Thêm đơn vị

                } else {
                    Log.e(TAG, "Dữ liệu người dùng rỗng dù API thành công.");
                    Toast.makeText(body1Activity.this, "Không thể lấy dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
//                progressDialog.dismiss();
                String errorMessage = errorResponse.getDetailedMessage() != null ? errorResponse.getDetailedMessage() : "Lỗi không xác định";
                Toast.makeText(body1Activity.this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi khi lấy thông tin người dùng: " + errorMessage + " (Code: " + errorResponse.getStatusCode() + ")");
            }
        });
    }

    // private void observeProgressView() {
    // Your existing method
    // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
    }
}
