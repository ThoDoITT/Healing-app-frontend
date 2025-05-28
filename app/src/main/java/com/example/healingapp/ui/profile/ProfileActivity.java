package com.example.healingapp.ui.profile;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healingapp.R;
import com.example.healingapp.network.ApiClient;
import com.example.healingapp.network.ApiResponseListener;
import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.UpdateProfileRequest;
import com.example.healingapp.network.models.response.UpdateProfileResponse;
import com.example.healingapp.ui.HomeActivity;
import com.example.healingapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etHeight;
    private EditText etWeight;
    private EditText etBirthDate;
    private CheckBox checkboxMale;
    private CheckBox checkboxFemale;
    private CheckBox checkboxOther;
    private Button btnSubmitProfile;

    private static final int GENDER_MALE = 0;
    private static final int GENDER_FEMALE = 1;
    private static final int GENDER_OTHER = 2;

    private ApiClient apiClient;
    private SessionManager sessionManager;
    private ProgressDialog progressDialog;
    private Calendar myCalendar;

    private static final String TAG = "FillProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fill_proflie);

        init();
        setupListeners();
    }

    private void init() {
        apiClient = new ApiClient();
        sessionManager = new SessionManager(getApplicationContext());
        myCalendar = Calendar.getInstance();

        etName = findViewById(R.id.etName);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnSubmitProfile = findViewById(R.id.btnSubmitProfile);
        checkboxMale = findViewById(R.id.checkboxMale);
        checkboxFemale = findViewById(R.id.checkboxFemale);
        checkboxOther = findViewById(R.id.checkboxOther);
        etBirthDate = findViewById(R.id.etBirthDate);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang cập nhật thông tin...");
        progressDialog.setCancelable(false);
    }


    private void setupListeners() {
        if (btnSubmitProfile != null) {
            btnSubmitProfile.setOnClickListener(v -> handleSubmitProfile());
        }

        // Listener cho EditText ngày sinh để mở DatePickerDialog
        if (etBirthDate != null) {
            DatePickerDialog.OnDateSetListener dateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            };

            etBirthDate.setOnClickListener(v -> new DatePickerDialog(ProfileActivity.this, dateSetListener,
                    myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show());
            etBirthDate.setFocusable(false); // Không cho phép nhập trực tiếp, chỉ chọn từ dialog
        }

        // Xử lý lựa chọn duy nhất cho CheckBox giới tính
        if (checkboxMale != null) {
            checkboxMale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkboxFemale.setChecked(false);
                    checkboxOther.setChecked(false);
                }
            });
        }

        if (checkboxFemale != null) {
            checkboxFemale.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkboxMale.setChecked(false);
                    checkboxOther.setChecked(false);
                }
            });
        }

        if (checkboxOther != null) {
            checkboxOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    checkboxMale.setChecked(false);
                    checkboxFemale.setChecked(false);
                }
            });
        }

    }

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd"; // Định dạng theo API yêu cầu "YYYY/MM/DD"
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        etBirthDate.setText(sdf.format(myCalendar.getTime()));
    }


    private void handleSubmitProfile() {
        String fullName = etName.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        // Lấy User ID và Token từ SessionManager
        String userId = sessionManager.getUserId();
        String authToken = sessionManager.getAuthToken();

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(authToken)) {
            Toast.makeText(this, "Lỗi xác thực người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            // Có thể điều hướng về màn hình đăng nhập
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            etName.setError("Vui lòng nhập họ tên");
            etName.requestFocus();
            return;
        }
        // ... (Thêm validation cho height, weight, gender, birthDate nếu cần) ...
        int height = 0;
        try {
            if (!TextUtils.isEmpty(heightStr)) height = Integer.parseInt(heightStr);
        } catch (NumberFormatException e) {
            etHeight.setError("Chiều cao không hợp lệ");
            etHeight.requestFocus();
            return;
        }

        int weight = 0;
        try {
            if (!TextUtils.isEmpty(weightStr)) weight = Integer.parseInt(weightStr);
        } catch (NumberFormatException e) {
            etWeight.setError("Cân nặng không hợp lệ");
            etWeight.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthDate)) {
            etBirthDate.setError("Vui lòng chọn ngày sinh");
            // Mở DatePicker nếu chưa chọn
            // etBirthDate.callOnClick(); // Hoặc thông báo
            Toast.makeText(this, "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy giá trị gender từ CheckBox
        int gender = -1; // Giá trị mặc định nếu không có gì được chọn (sẽ báo lỗi)

        if (checkboxMale.isChecked()) {
            gender = GENDER_MALE; // Ví dụ: 0
        } else if (checkboxFemale.isChecked()) {
            gender = GENDER_FEMALE; // Ví dụ: 1 (Nếu API của bạn mong muốn "1" cho Female)
        } else if (checkboxOther.isChecked()) {
            gender = GENDER_OTHER; // Ví dụ: 2
        }

        // Kiểm tra xem người dùng đã chọn giới tính chưa
        if (gender == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            // Có thể focus vào checkbox đầu tiên hoặc hiển thị lỗi rõ ràng hơn
            if (checkboxMale != null) checkboxMale.setError("Chọn một"); // Ví dụ đơn giản
            return;
        } else {
            if (checkboxMale != null) checkboxMale.setError(null); // Xóa lỗi nếu có
        }

        progressDialog.show();

        UpdateProfileRequest profileRequest = new UpdateProfileRequest(userId, gender, fullName, height, weight, birthDate);

        apiClient.updateUserProfile(authToken, profileRequest, new ApiResponseListener<UpdateProfileResponse, ErrorResponse>() {
            @Override
            public void onSuccess(UpdateProfileResponse response) {
                progressDialog.dismiss();
                String successMessage = "Cập nhật thông tin thành công!";
                if (response != null && response.getData() != null && !response.getData().toString().isEmpty()) {
                    successMessage = response.getData().toString();
                } else if (response != null && response.isSuccessFromApi()){ // Nếu có logic code trong response
                    successMessage = "Cập nhật thành công (API code: " + response.getCode() + ")";
                } else if (response == null) { // Trường hợp Call<Void> hoặc HTTP 204
                    // successMessage giữ nguyên
                }

                boolean currientIsconfigProfile = true;

                // cập nhật trạng thái
                if (sessionManager != null && !sessionManager.isConfigProfile()) {
                    currientIsconfigProfile = false;
                    sessionManager.setConfigProfileStatus(true); // Đặt thành true
                }

                Toast.makeText(ProfileActivity.this, successMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Cập nhật profile thành công.");
                // TODO: Điều hướng đi đâu đó sau khi cập nhật thành công (ví dụ: về màn hình Profile)
                if (!currientIsconfigProfile) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                }else {
                    finish();
                }
            }

            @Override
            public void onError(ErrorResponse errorResponse) {
                progressDialog.dismiss();
                String errorMessage = errorResponse.getDetailedMessage() != null ? errorResponse.getDetailedMessage() : "Lỗi không xác định";
                Toast.makeText(ProfileActivity.this, "Lỗi cập nhật: " + errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi cập nhật profile: " + errorMessage + " (Code: " + errorResponse.getStatusCode() + ")");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
