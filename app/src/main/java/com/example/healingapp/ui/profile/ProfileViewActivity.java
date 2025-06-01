package com.example.healingapp.ui.profile;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.network.ApiClient;
import com.example.healingapp.network.ApiResponseListener;
import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.UserProfileData;
import com.example.healingapp.network.models.response.GetUserProfileResponse;
import com.example.healingapp.ui.LoginActivity;
import com.example.healingapp.ui.body.body1Activity;
import com.example.healingapp.utils.SessionManager;

import java.util.Calendar;

public class ProfileViewActivity extends AppCompatActivity {

    private ImageView btnEditTextView;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private ImageView btnBack;

    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_profile);

        init();
        setEventListeners();
    }

    private void init() {

        btnBack = findViewById(R.id.btn_back);
        btnLogout = findViewById(R.id.btn_logout);
        btnEditTextView = findViewById(R.id.btnEdit);
        apiClient = new ApiClient();
        sessionManager = new SessionManager(getApplicationContext());

    }

    private void setEventListeners() {
        btnEditTextView.setOnClickListener(v -> {
            // 3. Tạo một Intent để chuyển sang Activity mới
            // Thay thế TargetActivity.class bằng tên Activity bạn muốn chuyển đến
            Intent intent = new Intent(ProfileViewActivity.this, ProfileActivity.class);

            // (Tùy chọn) Bạn có thể truyền dữ liệu sang Activity mới nếu cần
            intent.putExtra("IS_UPDATE_PROFILE_FROM_VIEW", true);

            // 4. Khởi chạy Activity mới
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            clearRoomDatabase();
            Intent intent = new Intent(ProfileViewActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }


    private void clearRoomDatabase () {
        AppDatabase db = AppDatabase.getDatabase(this); // Get your database instance
        db.clearAllTablesData(); // Call the method to clear all tables

    }
}
