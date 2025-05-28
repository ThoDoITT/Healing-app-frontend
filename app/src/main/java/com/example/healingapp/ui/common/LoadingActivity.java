package com.example.healingapp.ui.common;

import static android.content.ContentValues.TAG;
import static com.example.healingapp.common.Consts.EXTRA_DATA_ID;
import static com.example.healingapp.common.Consts.EXTRA_DATA_PAYLOAD;
import static com.example.healingapp.common.Consts.EXTRA_RESULT_MESSAGE;
import static com.example.healingapp.common.Consts.EXTRA_TARGET_ACTIVITY_CLASS_NAME;
import static com.example.healingapp.common.Consts.EXTRA_TASK_TYPE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healingapp.R;
import com.example.healingapp.common.TaskType;
import com.example.healingapp.data.AppDatabase;
import com.example.healingapp.data.dao.RunningSessionDao;
import com.example.healingapp.data.models.workout.RunningSession;
import com.example.healingapp.ui.workout.DetailRunningActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadingActivity extends AppCompatActivity {

    private TaskType currentTaskType;
    private Serializable dataPayload; // Dữ liệu đầu vào, có thể là bất kỳ object nào implements Serializable
    private String targetActivityClassName;
    private Handler mainHandler;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading); // Sử dụng layout loading đã tạo

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor(); // Sử dụng một thread duy nhất cho các tác vụ tuần tự

        // Lấy thông tin từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            try {
                currentTaskType = (TaskType) intent.getSerializableExtra(EXTRA_TASK_TYPE);
                dataPayload = intent.getSerializableExtra(EXTRA_DATA_PAYLOAD);
                targetActivityClassName = intent.getStringExtra(EXTRA_TARGET_ACTIVITY_CLASS_NAME);
            } catch (ClassCastException e) {
                Toast.makeText(this, "Lỗi định dạng TaskType.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (currentTaskType != null) {
            processTaskInBackground(currentTaskType, dataPayload);
        } else {
            Toast.makeText(this, "Không có tác vụ nào được chỉ định.", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void processTaskInBackground(TaskType taskType, Serializable data) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                String resultMessage = null;
                boolean success = false;
                long idNewRowAdd = -1;
                switch (taskType) {
                    case SAVE_RUN_DATA:
                        if (data instanceof String) {
                            String runDataJsonString = (String) data;
                            Log.d(TAG, "Nhận được JSON payload để lưu: " + runDataJsonString);
                            try {
                                // 1. Parse chuỗi JSON thành JSONObject
                                JSONObject runDataJson = new JSONObject(runDataJsonString);

                                // 2. Trích xuất dữ liệu thô từ JSONObject
                                long startTime = runDataJson.optLong("startTime", System.currentTimeMillis() - runDataJson.optLong("durationMillis", 0)); // Giá trị mặc định nếu thiếu
                                long endTime = runDataJson.optLong("endTime", System.currentTimeMillis()); // Giá trị mặc định nếu thiếu
                                float distance = (float) runDataJson.optDouble("distanceMeters", 0.0);
                                float pace = (float) runDataJson.optDouble("paceMinPerKm", 0.0);
                                long duration = runDataJson.optLong("durationMillis", 0);
                                long calories = runDataJson.optLong("calories", 0);
                                long steps = runDataJson.optLong("steps", 0);

                                // (Tùy chọn) Kiểm tra dữ liệu cơ bản
                                if (duration <= 0 && distance <= 0) {
                                    resultMessage = "Dữ liệu chạy không hợp lệ (duration hoặc distance bằng 0).";
                                    success = false;
                                    Log.w(TAG, resultMessage);
                                    break; // Thoát khỏi switch case
                                }

                                // 3. Tạo đối tượng RunData (Entity của Room)
                                RunningSession newRunToSave = new RunningSession(
                                        startTime,
                                        endTime,
                                        distance,
                                        pace,
                                        duration,
                                        calories,
                                        steps,
                                        System.currentTimeMillis()
                                );

                                // 4. Lấy instance của Database và DAO
                                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                                RunningSessionDao runDao = db.runDao();

                                // 5. Thực hiện chèn vào Room DB
                                long insertedId = runDao.insertRun(newRunToSave);
                                if (insertedId > 0) { // Chèn thành công sẽ trả về ID > 0
                                    resultMessage = "Lưu dữ liệu chạy thành công!";
                                    success = true;
                                    idNewRowAdd = insertedId; // Lưu ID mới
                                    Log.d(TAG, resultMessage + " ID mới: " + idNewRowAdd);
                                } else {
                                    resultMessage = "Lỗi khi lưu dữ liệu, không nhận được ID.";
                                    success = false;
                                    Log.e(TAG, resultMessage);
                                }

                            } catch (JSONException e) {
                                resultMessage = "Lỗi parse JSON dữ liệu chạy: " + e.getMessage();
                                success = false;
                                Log.e(TAG, resultMessage, e);
                            } catch (Exception e) { // Bắt các lỗi khác từ Room hoặc DB
                                resultMessage = "Lỗi khi lưu dữ liệu chạy vào database: " + e.getMessage();
                                success = false;
                                Log.e(TAG, resultMessage, e);
                            }
                        } else {
                            resultMessage = "Dữ liệu chạy không hợp lệ (không phải String JSON).";
                            success = false;
                            Log.w(TAG, resultMessage + " | Kiểu dữ liệu nhận được: " + (data != null ? data.getClass().getName() : "null"));
                        }
                        break;

                    case UPLOAD_PHOTOS:
                        // Giả định 'data' là một List<String> chứa đường dẫn ảnh
                        // List<String> photoPaths = (List<String>) data;
                        try {
                            // --- Logic tải ảnh lên Server ---
                            Thread.sleep(5000); // Giả lập tải ảnh
                            resultMessage = "Tải ảnh lên thành công!";
                            success = true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            resultMessage = "Lỗi khi tải ảnh: " + e.getMessage();
                            success = false;
                        }
                        break;

                    case SYNC_PROFILE:
                        // Giả định 'data' là một đối tượng Profile object
                        // Profile userProfile = (Profile) data;
                        try {
                            // --- Logic đồng bộ hồ sơ người dùng ---
                            Thread.sleep(2000); // Giả lập đồng bộ
                            resultMessage = "Đồng bộ hồ sơ thành công!";
                            success = true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            resultMessage = "Lỗi khi đồng bộ hồ sơ: " + e.getMessage();
                            success = false;
                        }
                        break;

                    default:
                        resultMessage = "Loại tác vụ không xác định.";
                        success = false;
                        break;
                }

                String finalResultMessage = resultMessage;
                boolean finalSuccess = success;

                long finalIdNewRowAdd = idNewRowAdd;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (finalSuccess) {
                            // Nếu có Activity đích, chuyển đến đó
                            if (targetActivityClassName != null && !targetActivityClassName.isEmpty()) {
                                try {
                                    Class<?> targetClass = Class.forName(targetActivityClassName);
                                    Intent intent = new Intent(LoadingActivity.this, targetClass);
                                    // Truyền thông báo kết quả sang Activity đích
                                    intent.putExtra(EXTRA_RESULT_MESSAGE, finalResultMessage);

                                    // NẾU TARGET LÀ DETAIL ACTIVITY VÀ CÓ ID MỚI, THÊM ID VÀO INTENT
                                    if (targetClass.getName().equals(DetailRunningActivity.class.getName()) && finalIdNewRowAdd > 0) {
                                        intent.putExtra(EXTRA_DATA_ID, (int) finalIdNewRowAdd); // Truyền ID mới
                                        Log.d(TAG, "Chuyển tới DetailRunningActivity với RUN_ID: " + finalIdNewRowAdd);
                                    }
                                    // (Bạn có thể cần thêm các cờ cho Intent nếu muốn, ví dụ FLAG_ACTIVITY_CLEAR_TOP)
//                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finishAffinity();
                                } catch (ClassNotFoundException e) {
                                    Toast.makeText(LoadingActivity.this, "Không tìm thấy Activity đích: " + targetActivityClassName, Toast.LENGTH_LONG).show();
                                    finish(); // Đóng LoadingActivity nếu không tìm thấy đích
                                }
                            } else {
                                // Nếu không có Activity đích, có thể đơn giản là đóng LoadingActivity
                                Toast.makeText(LoadingActivity.this, finalResultMessage, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            // Xử lý khi tác vụ thất bại
                            Toast.makeText(LoadingActivity.this, finalResultMessage, Toast.LENGTH_LONG).show();
                            // Có thể quay lại Activity trước đó hoặc hiển thị một màn hình lỗi
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Quan trọng: Đóng ExecutorService khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow(); // Cố gắng dừng tất cả các tác vụ đang chạy
        }
    }

}