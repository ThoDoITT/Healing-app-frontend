package com.example.healingapp.network;

import android.util.Log;

import com.example.healingapp.network.models.ErrorResponse;
import com.example.healingapp.network.models.LoginRequest;
import com.example.healingapp.network.models.LoginResponse;
import com.example.healingapp.network.models.RegisterRequest;
import com.example.healingapp.network.models.RegisterResponse;
import com.example.healingapp.network.models.UpdateProfileRequest;
import com.example.healingapp.network.models.response.GetUserProfileResponse;
import com.example.healingapp.network.models.response.UpdateProfileResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiClient {
    private static final String TAG = "ApiClientRetrofit";
    private ApiService apiService;
    public ApiClient() {
        this.apiService = RetrofitClient.getApiService();
    }

    public void registerUser(RegisterRequest requestData, final ApiResponseListener<RegisterResponse, ErrorResponse> listener) {
        Call<RegisterResponse> call = apiService.registerUser(requestData);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listener.onSuccess(response.body());
                } else {
                    // Xử lý lỗi từ API (vd: 4xx, 5xx)
                    ErrorResponse errorResponse = parseErrorBody(response);
                    listener.onError(errorResponse);
                    Log.e(TAG, "Lỗi API: " + errorResponse.getDetailedMessage() + " | Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                // Xử lý lỗi mạng hoặc các lỗi khác khi thực hiện request
                Log.e(TAG, "Lỗi mạng hoặc thực thi: ", t);
                listener.onError(ErrorResponse.fromException(t, 0)); // 0 for non-HTTP errors
            }
        });
    }

    // Phương thức đăng nhập (MỚI)
    public void loginUser(LoginRequest requestData, final ApiResponseListener<LoginResponse, ErrorResponse> listener) {
        Call<LoginResponse> call = apiService.loginUser(requestData);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Kiểm tra logic code từ API
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        listener.onSuccess(loginResponse);
                    } else {
                        // API trả về success (HTTP 200) nhưng có lỗi logic (code != "200")
                        String apiErrorMessage = "Lỗi không xác định từ API";
                        if (loginResponse.getError() != null) {
                            apiErrorMessage = loginResponse.getError().toString(); // Hoặc xử lý cụ thể hơn
                        } else if (!"200".equals(loginResponse.getCode()) && loginResponse.getData() == null) {
                            apiErrorMessage = "Đăng nhập thất bại (Code: " + loginResponse.getCode() + ")";
                        }
                        // Tạo ErrorResponse từ lỗi logic của API
                        listener.onError(new ErrorResponse(apiErrorMessage, apiErrorMessage, Integer.parseInt(loginResponse.getCode())));
                    }
                } else {
                    // Lỗi HTTP (4xx, 5xx)
                    ErrorResponse errorResponse = parseErrorBody(response);
                    listener.onError(errorResponse);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng (Login): ", t);
                listener.onError(ErrorResponse.fromException(t, 0));
            }
        });
    }

    private ErrorResponse parseErrorBody(Response<?> response) {
        ErrorResponse error;
        if (response.errorBody() != null) {
            try {
                // Thử parse errorBody thành ErrorResponse POJO bằng Gson
                Gson gson = new Gson();
                error = gson.fromJson(response.errorBody().charStream(), ErrorResponse.class);
                if (error == null) { // Nếu Gson trả về null (có thể do JSON không khớp)
                    error = new ErrorResponse("Lỗi không xác định từ server", response.errorBody().string(), response.code());
                } else {
                    error.setStatusCode(response.code()); // Gán mã HTTP
                }
            } catch (IOException | JsonSyntaxException | IllegalStateException e) {
                Log.e(TAG, "Lỗi parse error body: ", e);
                error = new ErrorResponse("Lỗi parse phản hồi lỗi", e.getMessage(), response.code());
            }
        } else {
            // Không có errorBody, có thể là lỗi không mong muốn
            error = new ErrorResponse("Lỗi không xác định", "Mã lỗi: " + response.code(), response.code());
        }
        return error;
    }

    public void updateUserProfile(String authToken, UpdateProfileRequest requestData, final ApiResponseListener<UpdateProfileResponse, ErrorResponse> listener) {
        Call<UpdateProfileResponse> call = apiService.updateUserProfile("Bearer " + authToken, requestData); // Thêm "Bearer " vào token

        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Giả sử API trả về code 200 và có body, hoặc bạn có thể check logic isSuccessFromApi()
                    UpdateProfileResponse profileResponse = response.body();
                    // if (profileResponse.isSuccessFromApi()) { // Nếu bạn có logic code trong response
                    listener.onSuccess(profileResponse);
                    // } else {
                    //     listener.onError(new ErrorResponse("Lỗi cập nhật từ API", profileResponse.getMessage(), Integer.parseInt(profileResponse.getCode())));
                    // }
                } else if (response.isSuccessful() && response.body() == null && response.code() == 204) { // No Content success
                    listener.onSuccess(null); // Hoặc tạo một UpdateProfileResponse rỗng
                }
                else {
                    ErrorResponse errorResponse = parseErrorBody(response); // parseErrorBody giữ nguyên
                    listener.onError(errorResponse);
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng (UpdateProfile): ", t);
                listener.onError(ErrorResponse.fromException(t, 0));
            }
        });
    }

    public void getUserProfile(String authToken, String userId, final ApiResponseListener<GetUserProfileResponse, ErrorResponse> listener) {
        if (authToken == null || userId == null || authToken.isEmpty() || userId.isEmpty()) {
            Log.e(TAG, "AuthToken hoặc UserId không hợp lệ để lấy thông tin người dùng.");
            listener.onError(new ErrorResponse("Lỗi Client", "Token hoặc User ID không hợp lệ.", 0));
            return;
        }

        Call<GetUserProfileResponse> call = apiService.getUserProfile("Bearer " + authToken, userId);

        call.enqueue(new Callback<GetUserProfileResponse>() {
            @Override
            public void onResponse(Call<GetUserProfileResponse> call, Response<GetUserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetUserProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess()) {
                        listener.onSuccess(profileResponse);
                    } else {
                        String apiErrorMessage = "Lỗi không xác định từ API";
                        if (profileResponse.getError() != null) {
                            apiErrorMessage = profileResponse.getError().toString();
                        } else if (profileResponse.getData() == null) {
                            apiErrorMessage = "Không nhận được dữ liệu người dùng (Code: " + profileResponse.getCode() + ")";
                        }
                        Log.e(TAG, "Lỗi logic từ API GetUserProfile: " + apiErrorMessage);
                        listener.onError(new ErrorResponse(apiErrorMessage, apiErrorMessage, Integer.parseInt(profileResponse.getCode())));
                    }
                } else {
                    // HTTP error (4xx, 5xx)
                    ErrorResponse errorResponse = parseErrorBody(response); // Reuse your existing parseErrorBody
                    listener.onError(errorResponse);
                    Log.e(TAG, "Lỗi HTTP khi GetUserProfile: " + errorResponse.getDetailedMessage() + " | Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GetUserProfileResponse> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng (GetUserProfile): ", t);
                listener.onError(ErrorResponse.fromException(t, 0));
            }
        });
    }
    // parseErrorBody (giữ nguyên hoặc điều chỉnh nếu cần) ...
}


