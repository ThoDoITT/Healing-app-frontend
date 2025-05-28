package com.example.healingapp.network;

import static com.example.healingapp.network.ConstRoute.BASE_URL;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofitInstance = null;
    private static ApiService apiServiceInstance = null;

    private static Retrofit getRetrofitInstance() {
        if (retrofitInstance == null) {
            // HttpLoggingInterceptor để log request/response (rất hữu ích khi debug)
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // Xem toàn bộ body

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor) // Thêm interceptor log
                    .connectTimeout(30, TimeUnit.SECONDS) // Thời gian chờ kết nối
                    .readTimeout(30, TimeUnit.SECONDS)    // Thời gian chờ đọc
                    .writeTimeout(30, TimeUnit.SECONDS)   // Thời gian chờ ghi
                    .build();

            retrofitInstance = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // Sử dụng OkHttpClient đã tùy chỉnh
                    .addConverterFactory(GsonConverterFactory.create()) // Sử dụng Gson
                    .build();
        }
        return retrofitInstance;
    }

    public static ApiService getApiService() {
        if (apiServiceInstance == null) {
            apiServiceInstance = getRetrofitInstance().create(ApiService.class);
        }
        return apiServiceInstance;
    }
}
