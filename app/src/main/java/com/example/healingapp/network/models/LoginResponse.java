package com.example.healingapp.network.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("code")
    private String code; // API trả về "200" là String

    @SerializedName("error")
    private Object error; // Có thể là String hoặc null, dùng Object cho linh hoạt

    @SerializedName("data")
    private UserData data;

    // Getters
    public String getCode() { return code; }
    public Object getError() { return error; } // Hoặc String getErrorMessage() nếu error luôn là string
    public UserData getData() { return data; }

    // Helper để kiểm tra thành công logic nghiệp vụ
    public boolean isSuccess() {
        return "200".equals(code) && data != null;
    }
}
