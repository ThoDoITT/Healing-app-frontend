package com.example.healingapp.ui;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.network.ApiClient;
import com.example.healingapp.network.ApiResponseListener;
import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.LoginRequest;
import com.example.healingapp.network.models.LoginResponse;
import com.example.healingapp.network.models.UserData;
import com.example.healingapp.ui.profile.ProfileActivity;
import com.example.healingapp.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private LinearLayout btnLogin;
    private LinearLayout btnRegisterSigin;
    private ImageButton btnShowPass;
    private ApiClient apiClient;
    private boolean isPasswordVisible = false;
    private ProgressDialog progressDialog;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signin);
        init();
        setupListeners();
    }

    private void init() {
        apiClient = new ApiClient();
        sessionManager = new SessionManager(getApplicationContext());

        etEmail = findViewById(R.id.editEmailLogin);
        etPassword = findViewById(R.id.editPasswordLogin);
        btnLogin = findViewById(R.id.btnSubmitLogin);
        btnShowPass = findViewById(R.id.btnShowPassSigin);
        btnRegisterSigin = findViewById(R.id.btnRegisterSigin);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(v -> handleSubmitLogin());
        }

        if (btnShowPass != null) {
            btnShowPass.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible; // Đảo trạng thái
                togglePasswordVisibility(etPassword, isPasswordVisible);
                updatePasswordToggleIcon(etPassword, btnShowPass, isPasswordVisible);
            });
        }

        btnRegisterSigin.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });



    }

    private void handleSubmitLogin() {
        String username = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etEmail.setError("Vui lòng nhập username");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        LoginRequest loginRequest = new LoginRequest(username, password);
        apiClient.loginUser(loginRequest, new ApiResponseListener<LoginResponse, ErrorResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                progressDialog.dismiss();
                // response.isSuccess() đã kiểm tra code "200" và data != null
                UserData userData = response.getData();

                Log.d(TAG, "Đăng nhập thành công. Username: " + userData.getUsername() + ", UserID: " + userData.getUserId());

                // TODO: Lưu token (ví dụ: SharedPreferences) và chuyển sang màn hình chính
                sessionManager.createLoginSession(userData.getToken(), userData.getUserId(), userData.getUsername(), userData.isConfigProfile());

                Intent intent;
                if (userData.isConfigProfile()) {
                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    intent = new Intent(LoginActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                }

            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                progressDialog.dismiss();
                String errorMessage = errorResponse.getDetailedMessage() != null ? errorResponse.getDetailedMessage() : "Lỗi không xác định";

                Log.e(TAG, "Lỗi đăng nhập: " + errorMessage + " (Code: " + errorResponse.getStatusCode() + ")");
            }
        });
    }

    private void togglePasswordVisibility(EditText editText, boolean isVisible) {
        if (editText == null) return;

        if (isVisible) {
            // Hiển thị mật khẩu
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            // Ẩn mật khẩu
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        editText.setSelection(editText.getText().length()); // Di chuyển con trỏ về cuối
    }

    private void updatePasswordToggleIcon(EditText editText, ImageButton toggleButton, boolean isVisible) {
        if (toggleButton == null || editText == null) return;

        if (isVisible) {
            // Mật khẩu đang hiển thị, icon nên là "mắt đang mở" hoặc "mắt bị gạch chéo" (ngụ ý click để ẩn)
            // Giả sử bạn có icon tên là ic_visibility_on.xml (mắt mở)
            toggleButton.setImageResource(R.drawable.ic_eye); // THAY THẾ BẰNG ICON CỦA BẠN
        } else {
            // Mật khẩu đang ẩn, icon nên là "mắt đang đóng" (ngụ ý click để hiện)
            // Giả sử bạn có icon tên là ic_visibility_off.xml (mắt đóng/gạch)
            toggleButton.setImageResource(R.drawable.ic_phone); // THAY THẾ BẰNG ICON CỦA BẠN // off
        }
    }
}
