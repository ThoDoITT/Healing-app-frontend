package com.example.healingapp.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.network.ApiClient;
import com.example.healingapp.network.ApiResponseListener;
import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.RegisterRequest;
import com.example.healingapp.network.models.RegisterResponse;

public class SignupActivity extends AppCompatActivity {
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private LinearLayout btnSubmitRegister;
    private ImageButton btnShowPassSignup;
    private ImageButton btnShowPassConfirm;
    private ApiClient apiClient; // Khai báo ApiClient
    private ProgressDialog progressDialog;

    private static final String TAG = "SignupActivity";

    // Biến để theo dõi trạng thái hiển thị mật khẩu
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_signup);


        init();
        setupListeners();
    }

    private void init() {
        apiClient = new ApiClient();
        etEmail = findViewById(R.id.txtEmailSignup);
        etPassword = findViewById(R.id.txtPassRegister);
        etConfirmPassword = findViewById(R.id.txtConfirmPass);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        btnShowPassSignup = findViewById(R.id.btnShowPassSignup);
        btnShowPassConfirm = findViewById(R.id.btnShowPassConfirm);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);
    }

    private void setupListeners() {
        if (btnSubmitRegister != null) {
            btnSubmitRegister.setOnClickListener(v -> handleSubmitRegister());
        }

        if (btnShowPassSignup != null) {
            btnShowPassSignup.setOnClickListener(v -> {
                isPasswordVisible = !isPasswordVisible; // Đảo trạng thái
                togglePasswordVisibility(etPassword, isPasswordVisible);
                updatePasswordToggleIcon(etPassword, btnShowPassSignup, isPasswordVisible);
            });
        }

        if (btnShowPassConfirm != null) {
            btnShowPassConfirm.setOnClickListener(v -> {
                isConfirmPasswordVisible = !isConfirmPasswordVisible; // Đảo trạng thái
                togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible);
                updatePasswordToggleIcon(etConfirmPassword, btnShowPassConfirm, isConfirmPasswordVisible);
            });
        }
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

    private void handleSubmitRegister() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();


        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 8) {
            etPassword.setError("Mật khẩu phải có ít nhất 8 ký tự");
            etPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        progressDialog.show();

        RegisterRequest request = new RegisterRequest(email, email, password);
        apiClient.registerUser(request, new ApiResponseListener<RegisterResponse, ErrorResponse>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                progressDialog.dismiss();

                Log.d(TAG, "Đăng ký thành công (Retrofit): " );
                // TODO: Chuyển màn hình
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                progressDialog.dismiss();
                String errorMessage = errorResponse.getDetailedMessage() != null ? errorResponse.getDetailedMessage() : "Lỗi không xác định";
                Toast.makeText(SignupActivity.this, "Lỗi: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi đăng ký (Retrofit): " + errorMessage + " (Code: " + errorResponse.getStatusCode() + ")");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Không cần gọi apiClient.shutdown() nữa vì Retrofit/OkHttp tự quản lý.
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
