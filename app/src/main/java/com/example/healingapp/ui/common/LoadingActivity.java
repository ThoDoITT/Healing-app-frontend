package com.example.healingapp.ui.common;

import static com.example.healingapp.common.Consts.EXTRA_DATA_PAYLOAD;
import static com.example.healingapp.common.Consts.EXTRA_RESULT_MESSAGE;
import static com.example.healingapp.common.Consts.EXTRA_TARGET_ACTIVITY_CLASS_NAME;
import static com.example.healingapp.common.Consts.EXTRA_TASK_TYPE;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.healingapp.R;
import com.example.healingapp.common.TaskType;

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

                switch (taskType) {
                    case SAVE_RUN_DATA:
                        // Giả định 'data' là một String JSON của dữ liệu chạy hoặc một đối tượng tùy chỉnh
                        String runData = (String) data;
                        try {
                            // --- Logic lưu dữ liệu chạy vào Database ---
                            Thread.sleep(3000); // Giả lập thời gian lưu
                            // Thực hiện lưu runData vào database
                            resultMessage = "Lưu dữ liệu chạy thành công: " + runData;
                            success = true;
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt(); // Quan trọng: reset trạng thái interrupt
                            resultMessage = "Lỗi khi lưu dữ liệu chạy: " + e.getMessage();
                            success = false;
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
                                    startActivity(intent);
                                    finish(); // Kết thúc LoadingActivity
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